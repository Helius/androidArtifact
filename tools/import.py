#!/usr/bin/python3

import pyrebase
import json
import sys
import requests
from multiprocessing import Pool
from multiprocessing import Manager

RED = "\033[1;31m"
BLUE = "\033[1;34m"
CYAN = "\033[1;36m"
GREEN = "\033[0;32m"
RESET = "\033[0;0m"
BOLD = "\033[;1m"
REVERSE = "\033[;7m"

oldstdout = sys.stdout

manager = Manager()
warn_out = manager.list()

config = json.loads(open('firebase_apikey.json').read())


print("hi, let's check this shit!")

def warn(s):
    sys.stdout.write(RED+s+RESET+'\n')
    warn_out.append(RED+s+RESET+'\n')



try:
    firebase = pyrebase.initialize_app(config)
except Exception as e:
    print('something going wrong while init firebase app:{0}'.format(e))

storage = firebase.storage()

def job(path):
    url = storage.child(path).get_url('')

    print("check:", path)
    try:
        resp = requests.get(url)
        size = resp.headers['content-length']
        if (int(size) > 1000):
            pass #print(GREEN + "OK!" + RESET)
        else:
            warn('failed: ' + path +'\nsize: ' + size)
            #print('len: ' + size)
            #print(RED + "FAIL!" + RESET + '\n')
    except Exception as e:
        warn('failed: ' + url + '\n' + format(e))

db = json.loads(open('out_db.json').read())

list=[]
for pic in db['content']['pictures']:
    list.append(pic['path']);

pool = Pool()
pool.map(job, list)

print('\n\n\n=== result ===')
if len(warn_out) == 0:
    print(GREEN + "OK!" + RESET + '\n')
else:
    sys.stdout.write(" ".join(warn_out))
sys.stdout.write(" ".join(warn_out))
