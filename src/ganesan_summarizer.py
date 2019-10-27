# -*- coding: utf-8 -*-
'''
Created on 17/12/2014

@author: Roque Lopez
'''
from __future__ import unicode_literals
from nltk.tag import brill
import unicodedata
import itertools
import nlpnet
import utils
import codecs
import os
import re

nlpnet.set_data_dir(str("../resource//nlpnet_data/"))

class Ganesan_Summarizer(object):
    '''
    Class that implements Ganesan method
    '''

    def __init__(self, name, opinions_path, aspect_manager):
        self.__name = name
        self.__aspect_manager = aspect_manager
        self.__data = {}
        self.__aspect_frequency = {}
        self.__tagger = nlpnet.POSTagger()
        self.__read_files(opinions_path)

    def __read_files(self, opinions_path):
        ''' Read the files (opinions) '''
        files = sorted(os.listdir(opinions_path))

        for file_name in files:
            sentences = utils.get_sentences(os.path.join(opinions_path, file_name))
            for (id_sentence, text_sentence)  in sentences:
                self.__process_sentence(file_name, id_sentence, text_sentence) 

    def __process_sentence(self, file_name, id_sentence, text_sentence):
        ''' Process a sentence tagging words and calculating their frequencies '''
        tag_token_list = list(itertools.chain.from_iterable(self.__tagger.tag(text_sentence)))
        sentence_text_list = []
        
        for (token, tag) in tag_token_list:
            index = tag.find("+")
            if index != -1: tag = tag[:index]
            index = tag.find("-")
            if index != -1: tag = tag[:index]
            sentence_text_list.append(token + "/" + tag)

        sentence_text = " ".join(sentence_text_list) + "\n\n"
        aspects = {}

        for annotation in self.__aspect_manager.get_data_sentence(self.__name,  re.match('(.+)\.txt', file_name).group(1), id_sentence)['annotations']:
            aspect = annotation['aspect']
            if aspect not in self.__aspect_frequency: self.__aspect_frequency[aspect] = 0
            self.__aspect_frequency[aspect] += 1
            if aspect not in aspects:
                aspects[aspect] = unicodedata.normalize('NFKD', aspect).encode('ASCII', 'ignore')
                #with codecs.open(os.path.join("../resource/opinosis/data/input/", "%s_%s.parsed" % (self.__name, aspects[aspect])), 'a','utf-8') as fout:
                #    fout.write(sentence_text)

    def create_summary(self, folder_path, top, write=True):
        ''' Create a summary given a number of aspects '''
        java_folder = "../resource/opinosis/data/output/"
        aspects = {}

        for file_name in [item for item in os.listdir(java_folder) if item.startswith(self.__name)]:
            with codecs.open(os.path.join(java_folder, file_name), 'r', 'utf-8') as fin:
                lines = fin.readlines()

            if len(lines) > 0:
                aspect = re.match('.+_(.+)\.summaries\.system', file_name).group(1)
                if aspect not in aspects: aspects[aspect] = []
                aspects[aspect] = self.__format_text(lines[0])

        if top > len(aspects): top = len(aspects)
        sentences = self.__get_top_sentences(top, aspects)
        summary = "\n".join(sentences)

        if write:
            with codecs.open(os.path.join(folder_path, self.__name + ".sum"), 'w','utf-8') as fout:
                fout.write(summary)
        else:
            return summary

    def __get_top_sentences(self, top, aspects):
        ''' Return sentences of the top aspects '''
        sentences = []
        top_aspects = sorted(self.__aspect_frequency.items(), key=lambda x:x[1], reverse=True)
        cont = 0

        for (aspect, frequency) in top_aspects:
            if cont == top: break
            aspect = unicodedata.normalize('NFKD', aspect).encode('ASCII', 'ignore')
            if aspect in aspects:
                sentences.append(aspects[aspect])
                cont += 1

        return sentences

    def __format_text(self, text):
        ''' Remove withespaces in punctuation marks (output of Ganesan jar) '''
    	text = text.replace(':/:', ':')
    	text = text.replace('!/!', '!')
    	text = text.replace(' .', '.')
    	text = text.replace(' ,', ',')
    	text = text.replace(' :', ':')
    	text = text.replace(' !', '!')
        text = text[0].upper() + text[1:]
    	return text.strip()