import json
from pandas.core.frame import DataFrame
import spacy
import pandas as pd

class SpacyNlp:
    def __init__(self, model_handle) -> None:
        self.nlp = spacy.load(model_handle)

    def process_table(self, input_table: DataFrame, column: str) -> DataFrame:
        docs = self.nlp.pipe(input_table[column].values)
        json_results = [self.doc_to_json(doc) for doc in docs]
        return pd.DataFrame(json_results, index=input_table.index)

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
        return nlp.process_table(input_table, column)


class SpacyLemmatizer(SpacyNlp):
    def token_to_dict(self, token):
        return {**super().token_to_dict(token), 'lemma': token.lemma_}


class SpacyNerTagger(SpacyNlp):
    def token_to_dict(self, token):
        return {**super().token_to_dict(token), 'entity': token.ent_type_, 'iob': token.ent_iob}


class SpacyPosTagger(SpacyNlp):
    def token_to_dict(self, token):
        return {**super().token_to_dict(token), 'tag': token.tag_}
