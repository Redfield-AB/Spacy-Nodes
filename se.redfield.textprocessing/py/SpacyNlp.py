import json
import spacy
import pyarrow as pa
import numpy as np
import knime.scripting.io as knio
from tqdm import tqdm
import sys

# On Mac GPU support doesn't work yet
_use_gpu = sys.platform != "darwin" and spacy.prefer_gpu()

class SpacyNlp:
    pipeline = ['tok2vec', 'transformer', 'parser', 'senter']
    def __init__(self, model_handle) -> None:
        self.nlp = spacy.load(model_handle)
        self.assigned_tags = set()

    def process_table(self, input_table: knio.Table, column: str) -> pa.Table:
        self.setup_pipeline()
        write_table = knio.BatchOutputTable.create()
        rows_done = 0
        for batch in input_table.batches():
            batch = batch.to_pyarrow()
            docs = tqdm(self.nlp.pipe(batch[column].to_pylist()), total=input_table.num_rows, initial=rows_done)
            results = self.collect_results(docs)
            output_batch = pa.RecordBatch.from_arrays([batch[0], results], names=[batch.field(0).name, 'result'])
            write_table.append(output_batch)
            rows_done += len(batch)
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
            d = self.token_to_dict(token)
            if d is not None:
                cur_sentence.append(self.token_to_dict(token))
            if(token.is_sent_end):
                sentences.append({'words': cur_sentence})
                cur_sentence = []

        return json.dumps({'sentences': sentences})

    def token_to_dict(self, token):
        return {'text': token.text}

    @classmethod
    def run(cls, model_handle: str, input_table: knio.Table, column:str):
        nlp = cls(model_handle)
        result_table =  nlp.process_table(input_table, column)

        tags = nlp.assigned_tags.difference({'', None})
        meta_table = pa.Table.from_arrays([tags], names=['tags'])

        knio.output_tables[0] = result_table
        knio.output_tables[1] = knio.Table.from_pyarrow(meta_table)


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
            vector = doc.vector
        elif hasattr(doc._, "trf_data"):
            vector = doc._.trf_data.model_output.pooler_output[0]
        else:
            raise ValueError("The pipeline produced no vectors. Try another model.")
        return vector.get() if _use_gpu else vector

class SpacyStopWordFilter(SpacyNlp):
    
    def token_to_dict(self, token):
        return None if token.is_stop else super().token_to_dict(token)