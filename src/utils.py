# -*- coding: utf-8 -*-
'''
Created on 23/02/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
from cStringIO import StringIO
import os
import codecs
import shutil
import sys

def get_sentences(file_path):
    ''' Return the sentences of a file in a list '''
    sentence_list = []
    with codecs.open(file_path, 'r','utf-8') as handle:
        lines = handle.readlines()

    i = 1

    for line in lines:
        line = line.strip()
        if len(line) > 0:
            sentence_list.append((str(i), line))
            i += 1

    return sentence_list

def make_dir(folder_path):
    ''' Create a folder '''
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)

def read_file(file_path):
    ''' Return the text of a file '''
    fin = codecs.open(file_path, 'r','utf-8')
    text = fin.read()
    fin.close()

    return text

def lemmatize(sentence):
    ''' Return a sentence lemmatized. This method use the lemmatizer available in http://nilc.icmc.usp.br/nilc/index.php/tools-and-resources '''
    lemmatizer_path = "YOUR_PATH_HERE"
    file_name = "file.txt"
    current_dir =  os.getcwd()
    os.chdir(lemmatizer_path)

    with codecs.open(file_name, 'w','utf-8') as fout:
        fout.write(sentence)

    os.system("java -jar lematizador.jar %s nf > /dev/null" % file_name)

    with codecs.open(file_name + ".out", 'r','utf-8') as fin:
        text = fin.read()

    os.chdir(current_dir)
    
    return text.strip()

def join_files(input_path, output_path):
    folders = os.listdir(input_path)
    for folder in folders:
        folder_path = os.path.join(input_path, folder)
        files = sorted(os.listdir(folder_path)) 
        text = ""

        for file_name in files:
            with codecs.open(os.path.join(folder_path, file_name), 'r','utf-8') as fin:
                text += fin.read()

        with codecs.open(os.path.join(output_path, folder + ".txt"), 'w','utf-8') as fout:
            fout.write(text.strip()) 

def copy_files(input_path, output_path):
    folders = os.listdir(input_path)

    for folder in folders:
        folder_path = os.path.join(input_path, folder)
        file_path = os.path.join(input_path, folder, "summaries", "1_summary.txt")

        for i in range(1, 6):
            #print os.path.join(output_path, folder, "summaries", "%s_summary.txt" % str(i))
            shutil.copy(file_path, os.path.join(output_path, folder, "summaries", "%s_summary.txt" % str(i)))

class Capturing(list):
    '''
    Class that capture the terminal outputs
    '''
    def __enter__(self):
        self._stdout = sys.stdout
        sys.stdout = self._stringio = StringIO()
        return self
        
    def __exit__(self, *args):
        self.extend(self._stringio.getvalue().splitlines())
        sys.stdout = self._stdout