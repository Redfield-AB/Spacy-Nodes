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