import requests
import pandas as pd
from bs4 import BeautifulSoup
from bs4.element import Tag


RELEASES_URL = 'https://github.com/explosion/spacy-models/releases?page='
MODEL_VERSIONS = {'3.2.0', '3.3.0', '3.5.0'}
fetched_versions = set()

def parse_table(tag: Tag, prefix=''):
    res = {}
    for tr in tag.find('tbody').find_all('tr'):
        td = tr.find_all('td')
        res[prefix + td[0].text] = td[1].text
    return res

def parse_release(tag: Tag):
    body = tag.find('div', class_='markdown-body')
    dict = {'url': body.find('p').find('a')['href']}

    tables = body.find_all('table')
    dict = {**dict, **parse_table(tables[0]), **parse_table(tables[1], 'labels-')}
    return dict

def parse_page(html: str):
    soup = BeautifulSoup(html, features='lxml')
    result = []
    done = False

    for div in soup.findAll('div', attrs={'class':'Box-body'}):
        release = parse_release(div)

        version = release['Version']
        if(version in MODEL_VERSIONS):
            result.append(release)
            fetched_versions.add(version)
        elif(len(fetched_versions) == len(MODEL_VERSIONS)):
            done = True
            break
        else:
            pass

    return (result, done)

def query_models():
    done = False
    page = 1
    models = []

    print('Fetching models', end='')
    while(not done):
        print('.', end='')
        url = RELEASES_URL + str(page)
        page += 1

        (res, done) = parse_page(requests.get(url).text)
        models.extend(res)
    print('.')

    return pd.DataFrame(models)

def process_models(df: pd.DataFrame):
    res = pd.DataFrame()
    res['name']       = df['Name']
    res['size']       = df['Model size']
    res['version']    = df['Version']
    res['lang']       = df['Name'].map(lambda s : s.split('_')[0])
    res['url']        = df['url']
    res['components'] = df['Components'].map(lambda s : [tok.strip() for tok in s.split(',')])
    return res

if __name__ == '__main__':
    df = query_models()
    res = process_models(df)
    res.to_json('spacy-models.json', orient='records', indent=2)