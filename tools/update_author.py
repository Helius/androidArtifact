#!/usr/bin/python3

import sys
import re
import json
import os
import imghdr

RED = "\033[1;31m"
BLUE = "\033[1;34m"
CYAN = "\033[1;36m"
GREEN = "\033[0;32m"
RESET = "\033[0;0m"
BOLD = "\033[;1m"
REVERSE = "\033[;7m"

author_file = 'author_ext.json'

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
        pic["path"] = os.path.join(authorFolder, s)
        pic["author"] = authorId
        if m.groups()[2] != None:
            pic["movement_id"] = int(m.groups()[2])
        else:
            pic["movement_id"] = 0
        return pic
    except Exception as e:
        warn('warning!: ' + s + ' ' + str(e))
    return {}


def tryFillFromName(p, p_f):
    tmp = parsePicFile(p_f, '', '')
    if 'level' in tmp:
        p['level'] = tmp['level']
    if 'movement_id' in tmp:
        p['movement_id'] = tmp['movement_id']
    return p


def findPictureFiles(dir):
    res = []
    for f in os.listdir(dir):
        if imghdr.what(os.path.join(dir, f)) is not None:
            res.append(f)
            print('ok: \t{}'.format(f))
        else:
            if f != 'author_ext.json':
                warn('skip:\t{}'.format(f))
    return res


def collectAuthors():
    print('\nTry to load other authors')
    authors = []
    current_dir = os.path.basename(os.path.normpath(rootDir))
    for dir in os.listdir(os.path.abspath(os.path.join(rootDir, os.pardir))):
        a_dir = os.path.join(rootDir, os.pardir, dir)
        p = os.path.join(a_dir, 'author_ext.json')

        if (os.path.isdir(a_dir) and dir != current_dir):
            try:
                author_str = open(p, 'r', encoding="utf8").read().replace('\n', '')
                author_json = json.loads(author_str)
                authors.append(author_json)
            except OSError:
                pass
                # warn("{} doesn't contain author_ext.json".format(dir))
    return sorted(authors, key=lambda a: a["id"])
    print('Done')


def create_new_author(name):
    author_json = {}
    author_file = ('templates/author_template.json')
    try:
        author_str = open(author_file, 'r', encoding="utf8").read().replace('\n', '')
        author_json = json.loads(author_str)
        author_json['name_ru'] = name
        print("Loaded", author_file)
    except OSError:
        warn("{} not found, will create new one".format(author_file))
    return author_json


# ========= start ==========

rootDir = sys.argv[1]

# obtain name from dir
author_name = os.path.basename(os.path.normpath(rootDir))
print("For [%s]" % author_name)

# get author pic files in dir
pics_f = findPictureFiles(rootDir)
if len(pics_f) == 0:
    warn("pics files not found in {}".format(rootDir))
    exit(1)

# try to load or create new author_ext.json
author_file = os.path.join(rootDir, 'author_ext.json')
print("\nTry load ", author_file)
try:
    author_str = open(author_file, 'r', encoding="utf8").read().replace('\n', '')
    author_json = json.loads(author_str)
    print("Loaded", author_file)
except OSError as e:
    print("author_ext.json not found, will create new one")
    author_json = create_new_author(author_name)

# get all author id to avoid dublicate
authors = collectAuthors()
if len(authors) == 0:
    warn("Can't find other authors to check id is unique")

# check id unique
if 'id' in author_json:
    for a in authors:
        if (author_json['id'] == a['id']):
            warn('This id {} already exist in {}'.format(a['id'], a['name_ru']))
            warn('Next free is {}'.format(int(authors[len(authors)-1]["id"])+1))
else:
    newId = int(authors[len(authors)-1]["id"])+1
    print("create new ID {}".format(newId))
    author_json['id'] = newId

# check images from json exist in dir
print('Check pictures in author_ext.json:')
for p in author_json['pictures']:
    if len(p['path']) == 0:
        author_json['pictures'].remove(p)
        del p
        warn('!found picture with empty path! gonna remove it!')
        continue
    if os.path.basename(p['path']) not in pics_f:
        warn('!!!Miss {} file from json'.format(p['path']))

# check images in dir added to json and add new
pictures_to_add = []
for p_f in pics_f:
    # don't use path.join because on Windows it produces '\\' separator
    full_path = author_name + '/' + p_f
    exist = False
    # load template for picture
    p_str = open('templates/picture_template.json', 'r', encoding="utf8").\
        read().replace('\n', '')
    p = json.loads(p_str)

    for pic in author_json['pictures']:
        if pic['path'] == full_path:
            exist = True
    if exist is True:
        print('file found')
    else:
        print('file {} not found, will add'.format(full_path))
        p = tryFillFromName(p, p_f)
        p['path'] = full_path
        p['author'] = author_json['id']
        pictures_to_add.append(p)

for p in pictures_to_add:
    author_json['pictures'].append(p)

outFile = open(os.path.join(rootDir, "author_ext.json"), 'w', encoding="utf8")
outFile.write(json.dumps(author_json, indent=2, ensure_ascii=False))
outFile.close()

print("author_ext.json updated")
print("\n\n ======== collected warnings =========\n")
sys.stdout.write(' '.join(warn_out))
