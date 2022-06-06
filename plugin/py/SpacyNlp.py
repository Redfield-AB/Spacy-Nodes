import json
from pandas.core.frame import DataFrame
import spacy
import pandas as pd

class SpacyNlp:
    pipeline = ['tok2vec', 'transformer', 'parser', 'senter']
    def __init__(self, model_handle) -> None:
        self.nlp = spacy.load(model_handle)
        self.assigned_tags = set()

    def process_table(self, input_table: DataFrame, column: str) -> DataFrame:
        self.setup_pipeline()
        docs = self.nlp.pipe(input_table[column].values)
        json_results = [self.doc_to_json(doc) for doc in docs]
        return pd.DataFrame(json_results, index=input_table.index)
    
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
    def run(cls, model_handle: str, input_table: DataFrame, column:str) -> DataFrame:
        nlp = cls(model_handle)
        result_table =  nlp.process_table(input_table, column)
        meta_table = pd.DataFrame(nlp.assigned_tags.difference({'', None}))
        return result_table, meta_table


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

        return {**super().token_to_dict(token), 'morph': token.morph.to_dict()}

class SpacyVectorizer(SpacyNlp):
    pipeline = ['tok2vec', 'transformer']

    def process_table(self, input_table: DataFrame, column: str) -> DataFrame:
        self.setup_pipeline()
        docs = self.nlp.pipe(input_table[column].values)
        vectors = [(','.join([str(num) for num in doc.vector])) for doc in docs]
        return pd.DataFrame(vectors, index=input_table.index)