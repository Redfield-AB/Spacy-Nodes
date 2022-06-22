import subprocess
import sys
import json
import spacy

def load_model(model_handle='en_core_web_sm'):
    return spacy.load(model_handle)

def to_json(doc):
    out = []
    for sent in doc.sents:
        out_sent = []
        for w in sent:
            out_sent.append(str(w))
        out.append(out_sent)
    return json.dumps(out)
def to_json2(doc):
    sentences = []
    cur_sentence = []

    for token in doc:
        cur_sentence.append({'text': token.text, 'tag': token.tag_})
        if(token.is_sent_end):
            sentences.append({'words': cur_sentence})
            cur_sentence = []

    return json.dumps({'sentences': sentences})

def tokenize(input_table, column, model_handle):
    nlp = load_model(model_handle)
    nlp.disable_pipes('ner')
    input_table[column] = input_table[column].apply(lambda x: to_json2(nlp(x)))
    return input_table