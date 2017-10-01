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


def collectPic():
    for dirName, subdirList, fileList in os.walk(rootDir):
        print('Found directory: %s' % dirName)
        authorId = 0
        if 'author_ext.json' in fileList:
            print ('found author_ext.json in %s' % dirName)
            author_str = open(dirName + '/author_ext.json', 'r').read().replace('\n', '')
            author_j = json.loads(author_str)
            authorId = author_j["id"]
            fileList.remove('author.json')
            fileList.remove('author_ext.json')
            for p in author_j["pictures"]:
                if not os.path.basename(p["path"]) in fileList:
                    warn(p["path"] + " not found")
                p["author"] = author_j["id"]
                pic_out.append(p)
            for f in fileList:
                found = False;
                for p in author_j["pictures"] :
                    if os.path.basename(p["path"]) == f:
                        found = True;
                        break;
                if found == False:
                    warn("!!!! Missing file: " + dirName+f + " not in json")
            author_j.pop("pictures", None)
            for a in author_out:
                if a["id"] == authorId:
                    warn("Author has already used by " + a["name_en"])
                    raise ValueError('Author has already used by " + a["name_en"]')
            author_out.append(author_j)
        else:
            warn('author.json not found in ' + dirName)
#        for fname in fileList:
#            print('\t%s' % fname)
#            out = parsePicFile(fname, os.path.basename(dirName), authorId)
#            if out is not None:
#                pic_out.append(out)

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

def db_print_movements_stat(jdb):
    print("\nMovements statistic:")
    print("{0:30} {1:5}{2:5}{3:5}  {4:5}".format('', 'lev1', 'lev2', 'lev3', 'total'))
    total_counts = [0, 0, 0, 0]
    result = []
    raw_result = []
    for movement in jdb['content']['movements']:
        counts={1:0, 2:0, 3:0}
        for pic in jdb['content']['pictures']:
            if pic['movement_id'] == movement['id']:
                counts[pic['level']] = counts[pic['level']] + 1
        total = counts[1] + counts[2] + counts[3]
        raw_result.append({"id": movement["id"],
                           "name": movement["name_ru"],
                           "lev1": counts[1],
                           "lev2": counts[2],
                           "lev3": counts[3],
                           "total": total})
        result.append(str)
        total_counts[0] += counts[1]
        total_counts[1] += counts[2]
        total_counts[2] += counts[3]
        total_counts[3] += total

    raw_result_sorted = sorted(raw_result, key=lambda a: a["total"])
    for r in raw_result_sorted:
        print ("{0:2} {1:26}{2:5}{3:5}{4:5}  {5:5}".format(r['id'], r['name'],r['lev1'],r['lev2'],r['lev3'],r['total']))
    print('----------------------------------------------------')
    print('{0:>34}{1:5}{2:5}   {3:5}'.format(total_counts[0], total_counts[1], total_counts[2], total_counts[3]))

def db_print_authors_stat(jdb):
    print("\nAuthors statistic:")
    print("{0:30} {1:5}{2:5}{3:5}  {4:5}".format('', 'lev1', 'lev2', 'lev3', 'total'))
    total_counts = [0, 0, 0, 0]
    result = []
    raw_result = []
    for author in jdb['content']['authors']:
        counts={1:0, 2:0, 3:0}
        for pic in jdb['content']['pictures']:
            if pic['author'] == author['id']:
                counts[pic['level']] = counts[pic['level']] + 1
        total = counts[1] + counts[2] + counts[3]
        raw_result.append({"id": author["id"],
                           "name": author["name_ru"],
                           "lev1": counts[1],
                           "lev2": counts[2],
                           "lev3": counts[3],
                           "total": total})
        result.append(str)
        total_counts[0] += counts[1]
        total_counts[1] += counts[2]
        total_counts[2] += counts[3]
        total_counts[3] += total

    raw_result_sorted = sorted(raw_result, key=lambda a: a["total"])
    for r in raw_result_sorted:
        print ("{0:2} {1:26}{2:5}{3:5}{4:5}  {5:5}".format(r['id'], r['name'],r['lev1'],r['lev2'],r['lev3'],r['total']))
    print('----------------------------------------------------')
    print('{0:>34}{1:5}{2:5}   {3:5}'.format(total_counts[0], total_counts[1], total_counts[2], total_counts[3]))


rootDir = sys.argv[1]

collectPic()

author_out = sorted(author_out, key=lambda a: a["id"])

db_out = {}
db_out["content"] = {}
db_out["content"] = json.loads(open(os.path.join(rootDir, 'movements.json')).read())
db_out["content"]["authors"] = author_out
db_out["content"]["pictures"] = pic_out
print("writing...")
outFile = open("out_db.1.json",'w')
db_pretty_print (db_out)
outFile.close()
print("saved to out_db.json")
sys.stdout.write(" ".join(warn_out))
print('\n -------------- Statistics --------------\n')
db_print_movements_stat(db_out)
db_print_authors_stat(db_out)
