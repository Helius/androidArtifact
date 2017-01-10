#!/usr/bin/python3
import os
import sys
import re
import json

RED = "\033[1;31m"
BLUE = "\033[1;34m"
CYAN = "\033[1;36m"
GREEN = "\033[0;32m"
RESET = "\033[0;0m"
BOLD = "\033[;1m"
REVERSE = "\033[;7m"

oldstdout = sys.stdout

pic_out = []
author_out = []

def warn(s):
    sys.stdout.write(RED+s+RESET+'\n')

def parsePicFile(s, authorFolder, authorId):
    m = re.match('([0-9])_(([0-9]+)_)?', s)
    try:
        pic = {}
        pic["level"] = int(m.groups()[0])
        pic["path"] = authorFolder + '/' + s
        pic["author"] = authorId
        if m.groups()[2] != None:
            pic["movement_id"] = int(m.groups()[2])
        else:
            pic["movement_id"] = 0
        return pic
    except Exception as e:
        warn('warning!: ' + s)

def parseAuthorId(s):
    jauthor = json.loads(s)
    try:
        return jauthor["id"]
    except:
        warn('Can\'t parse Author ID json for ' + s)

def collectPic():
    for dirName, subdirList, fileList in os.walk(rootDir):
        print('Found directory: %s' % dirName)
        authorId = 0
        if 'author.json' in fileList:
            print ('found author.json in %s' % dirName)
            author_str = open(dirName + '/author.json', 'r').read().replace('\n', '')
            author_j = json.loads(author_str)
            author_out.append(author_j)
            authorId = author_j["id"]
            fileList.remove('author.json')
        else:
            warn('author.json not found')
        for fname in fileList:
            print('\t%s' % fname)
            out = parsePicFile(fname, os.path.basename(dirName), authorId)
            if out is not None:
                pic_out.append(out)

def print_array(dic):
    cnt = 0
    for key in dic:
        cnt = cnt + 1
        if (cnt == len(dic)):
            out('        ' + json.dumps(key, ensure_ascii=False))
        else:
            out('        ' + json.dumps(key, ensure_ascii=False) + ',')


def out(s):
    #print (s) # for Debug
    outFile.write(s + '\n')


def db_pretty_print(jdb):
    out('{\n    "content": {')

    out('    "authors": [')
    print_array(jdb["content"]["authors"])
    out ('    ],')

    out('    "movements": [')
    print_array(jdb["content"]["movements"])
    out ('    ],')

    out('    "pictures": [')
    print_array(jdb["content"]["pictures"])
    out ('    ]')

    out("    }")
    out("}")


rootDir = sys.argv[1]

collectPic()

author_out = sorted(author_out, key=lambda a: a["id"])

db_out = {}
db_out["content"] = {}
db_out["content"] = json.loads(open(os.path.join(rootDir, 'movements.json')).read())
db_out["content"]["authors"] = author_out
db_out["content"]["pictures"] = pic_out
print("writing...")
outFile = open("out_db.json",'w')
db_pretty_print (db_out)
outFile.close()
print("saved to out_db.json")
