#!/usr/bin/python
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
    m = re.search('([0-9])[-_](.*)', s)
    try:
        return str('{"level": ' + m.group(1) + \
            ', "path": "' + authorFolder + '/' + m.group(0) +\
            '", "author": "' + str(authorId)  +\
            '", "movement_id": "' + str(0) + '"}')
    except:
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
            author_out.append(author_str)
            authorId = parseAuthorId(author_str)
            fileList.remove('author.json')
        else:
            warn('author.json not found')
        for fname in fileList:
            print('\t%s' % fname)
            out = parsePicFile(fname, os.path.basename(dirName), authorId)
            if out is not None:
                pic_out.append(out)


rootDir = sys.argv[1]

collectPic()

db_out = "{\n    \"content\": {"

author_out = sorted(author_out, key=lambda a: parseAuthorId(a))


for a in author_out:
    print (a)
    a = "-" + a + "-"
    print (a)


#for a in pic_out:
#    print (a)
db_out += "\n    }\n}"
print ("------- db out -------")
print (db_out)
