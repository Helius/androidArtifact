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

warn_out = []
pic_out = []
author_out = []

def warn(s):
    sys.stdout.write(RED+s+RESET+'\n')
    warn_out.append(RED+s+RESET+'\n')

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
        warn('warning!: ' + s + ' ' + str(e))


def parseAuthorId(s):
    jauthor = json.loads(s)
    try:
        return jauthor["id"]
    except:
        warn('Can\'t parse Author ID json for ' + s)

def collectPic():
    author_json = {}
    for dirName, subdirList, fileList in os.walk(rootDir):
        print('Processing directory: %s' % dirName)
        authorId = 0
        if 'author_ext.json' in fileList:
            print ('found %s' % dirName + 'author_ext.json')
            author_str = open(dirName + '/author_ext.json', 'r').read().replace('\n', '')
            author_json = json.loads(author_str)
            fileList.remove('author_ext.json')
        else:
            warn('author_ext.json not found')
        for fname in fileList:
            print('\t%s' % fname)
            out = parsePicFile(fname, os.path.basename(dirName), authorId)
            if out is not None:
                pic_out.append(out)


def collectPicFiles():
    for dirName, subdirList, fileList in os.walk(rootDir):
        print('Processing directory: %s' % dirName)
        if 'author_ext.json' in fileList:
            fileList.remove('author_ext.json')
        if 'author.json' in fileList:
            fileList.remove('author.json')
        return fileList

def collectAuthors():
    print('\nTry to load other authors')
    authors = []
    current_dir = os.path.basename(os.path.normpath(rootDir))
    for dir in os.listdir(os.path.abspath(os.path.join(rootDir, os.pardir))):
        a_dir = os.path.join(rootDir, os.pardir, dir)
        p = os.path.join(a_dir, 'author_ext.json')

        if (os.path.isdir(a_dir) and dir != current_dir):
            try:
                author_str = open(p, 'r').read().replace('\n', '')
                author_json = json.loads(author_str)
                authors.append(author_json)
            except OSError:
                warn("{} doesn't contain author_ext.json".format(dir))
    return sorted(authors, key=lambda a: a["id"])
    print ('Done')


def print_array(dic):
    cnt = 0
    for key in dic:
        cnt = cnt + 1
        try:
            if (cnt == len(dic)):
                out('        ' + json.dumps(key, ensure_ascii=False))
            else:
                out('        ' + json.dumps(key, ensure_ascii=False) + ',')
        except Exception as e:
            warn('warning! can\'t print ' + str(key) + ' '  + str(e))


def out(s):
    #print (s) # for Debug
    outFile.write(s + '\n')



# ========= start ==========

rootDir = sys.argv[1]

# obtain name from dir
author_name = os.path.basename(os.path.normpath(rootDir))
print("For [%s]" % author_name)

# get pic files

pics = collectPicFiles()
if type(pics) is list and len(pics) > 0:
    print("found pics:")
    for pic in pics:
        print('\t',pic)
else:
    warn("pics files not found in {}".format(rootDir))
    exit(1)

# get author_ext.json

author_file = os.path.join(rootDir, 'author_ext.json')
print("\nTry load ", author_file)
author_str='{}'
try:
   author_str = open(author_file).read().replace('\n', '')
except OSError as e:
    print("author_ext.json not found, will create new one")
author_json = json.loads(author_str)
print ("Loaded", author_file)

# get all author id to avoid dublicate

authors = collectAuthors()
if len(authors) == 0:
    warn ("Can't find other authors to check id is unique")
    exit(1)
#for a in authors:
#    print (a["name_ru"], a["id"])

# check id unique

if 'id' in author_json:
    for a in authors:
        if (author_json['id'] == a['id']):
            warn('This id {} already exist in {}'.format(a['id'], a['name_ru']))
            warn('Next free is {}'.format(int(authors[len(authors)-1]["id"])+1))
else:
    print("create new ID")



print("\n")

#print(json.dumps(author_json, indent=2, ensure_ascii=False))

#author_out = sorted(author_out, key=lambda a: a["id"])

#db_out = {}
#db_out["content"] = {}
#db_out["content"] = json.loads(open(os.path.join(rootDir, 'movements.json')).read())
#db_out["content"]["authors"] = author_out
#db_out["content"]["pictures"] = pic_out
#print("writing...")
#outFile = open("out_db.json",'w')
#db_pretty_print (db_out)
#outFile.close()
#print("saved to out_db.json")
sys.stdout.write(' '.join(warn_out))
#print('\n -------------- Statistics --------------\n')
#db_print_movements_stat(db_out)
#db_print_authors_stat(db_out)
