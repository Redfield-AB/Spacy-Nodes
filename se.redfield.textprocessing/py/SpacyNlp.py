import json
import spacy
import pandas as pd

class SpacyNlp:
    def __init__(self, model_handle) -> None:
        self.nlp = spacy.load(model_handle)
    
    def ne_tag_sentence(self, sentence):
        doc = self.nlp(sentence)
        dicts = [{'text':e.text, 'label':e.label_} for e in doc.ents]
        out_table = pd.DataFrame(dicts, columns=['text', 'label'])
        return out_table
    
    def lemmatize_table(self, input_table, column):
        print(input_table)
        print('input:',input_table[column].values)
        docs = self.nlp.pipe(input_table[column].values)
        json_results = [self.doc_to_json(doc) for doc in docs]
        return pd.DataFrame(json_results, index=input_table.index)

    def doc_to_json(self, doc):
        print('doc', doc)
        sentences = []
        cur_sentence = []

        for token in doc:
            print('token', token)
            cur_sentence.append({'text': token.text, 'lemma': token.lemma_})
            if(token.is_sent_end):
                sentences.append({'words': cur_sentence})
                cur_sentence = []

        return json.dumps({'sentences': sentences})
