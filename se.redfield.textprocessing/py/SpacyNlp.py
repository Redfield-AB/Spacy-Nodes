import json
import spacy
import pyarrow as pa
import numpy as np
import knime_io as knio


class SpacyNlp:
    pipeline = ['tok2vec', 'transformer', 'parser', 'senter']
    def __init__(self, model_handle) -> None:
        self.nlp = spacy.load(model_handle)
        self.assigned_tags = set()

    def process_table(self, input_table: knio.ReadTable, column: str) -> pa.Table:
        self.setup_pipeline()
        write_table = knio.batch_write_table()
        for batch in input_table.batches():
            batch = batch.to_pyarrow()
            docs = self.nlp.pipe(batch[column].to_pylist())
            results = self.collect_results(docs)
            output_batch = pa.RecordBatch.from_arrays([batch[0], results], names=[batch.field(0).name, 'result'])
            write_table.append(output_batch)
        return write_table
    
    def collect_results(self, docs):
        return [self.doc_to_json(doc) for doc in docs]
    
    def setup_pipeline(self):
        for p in self.nlp.pipe_names:
            if(p not in self.pipeline):
                self.nlp.disable_pipe(p)

    def doc_to_json(self, doc):
        sentences = []
        cur_sentence = []

        for token in doc:
            cur_sentence.append(self.token_to_dict(token))
            if(token.is_sent_end):
                sentences.append({'words': cur_sentence})
                cur_sentence = []

        return json.dumps({'sentences': sentences})

    def token_to_dict(self, token):
        return {'text': token.text}

    @classmethod
    def run(cls, model_handle: str, input_table: knio.ReadTable, column:str):
        nlp = cls(model_handle)
        result_table =  nlp.process_table(input_table, column)

        tags = nlp.assigned_tags.difference({'', None})
        meta_table = pa.Table.from_arrays([['Row' + str(i) for i in range(len(tags))], tags], names=['RowId','tags'])

        knio.output_tables[0] = result_table
        knio.output_tables[1] = knio.write_table(meta_table)


class SpacyLemmatizer(SpacyNlp):
    pipeline = [*SpacyNlp.pipeline, 'morphologizer', 'tagger', 'attribute_ruler', 'lemmatizer']
    def token_to_dict(self, token):
        return {**super().token_to_dict(token), 'lemma': token.lemma_}


class SpacyNerTagger(SpacyNlp):
    pipeline = [*SpacyNlp.pipeline, 'ner']
    def token_to_dict(self, token):
        self.assigned_tags.add(token.ent_type_)
        return {**super().token_to_dict(token), 'entity': token.ent_type_, 'iob': token.ent_iob}


class SpacyPosTagger(SpacyNlp):
    pipeline = [*SpacyNlp.pipeline, 'morphologizer', 'tagger', 'attribute_ruler']
    def token_to_dict(self, token):
        self.assigned_tags.add(token.tag_)
        return {**super().token_to_dict(token), 'tag': token.tag_}

class SpacyMorphonogizer(SpacyNlp):
    pipeline = [*SpacyNlp.pipeline, 'morphologizer']
    def token_to_dict(self, token):
        tags = [k + ':' + v for k,v in token.morph.to_dict().items()]
        for tag in tags:
            self.assigned_tags.add(tag)

        return {**super().token_to_dict(token), 'morph': tags}

class SpacyVectorizer(SpacyNlp):
    pipeline = ['tok2vec', 'transformer']

    def collect_results(self, docs):
        return [np.array(self.get_vector(doc), dtype=np.float64) for doc in docs]

    def get_vector(self, doc):
        if len(doc.vector) > 0:
            return doc.vector
        else:
            return doc._.trf_data.model_output.pooler_output[0]