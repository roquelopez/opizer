# -*- coding: utf-8 -*-
'''
Created on 26/01/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
import os
import re
import codecs
import json

class BuscapeCorpusReader(object):
    '''
    Class to provide data and methods to read Buscape corpus
    '''

    def __init__(self, folder_path):
        self.__corpus = {}
        self.__aspect_information = {}
        self.__build_corpus(folder_path)
        self.__load_aspect_information()
    
    def __build_corpus(self, folder_path):
        ''' Read and get the data from the annotated files (opinions) '''
        files = sorted(os.listdir(folder_path))
        
        for file_name in files:
            product_name = re.match('(.+)\.anot', file_name).group(1)
            self.__corpus[product_name] = {}
            fin = codecs.open(os.path.join(folder_path, file_name), 'r','utf-8')
            flines = fin.readlines()
            id_sentence = None

            for fline in flines:
                fline = fline.strip() 
                
                if fline.startswith("#Resenha_") > 0:
                    id_review = re.match('#Resenha_(.+)', fline).group(1)
                    self.__corpus[product_name][id_review] = {}
                    self.__corpus[product_name][id_review]['sentences'] = {}
                    id_sentence = 1

                elif fline.startswith("#Aspectos:") > 0:
                    self.__process_aspects_review(fline[10:], product_name, id_review)

                elif fline.startswith("#Estrelas") > 0:
                    stars = re.match('#Estrelas: (.+)', fline).group(1)  
                    self.__corpus[product_name][id_review]['stars'] = stars   

                elif len(fline) > 0:
                    self.__corpus[product_name][id_review]['sentences'][str(id_sentence)] = {}
                    self.__process_aspects_sentence(fline, product_name, id_review, str(id_sentence))
                    id_sentence += 1
                    
            fin.close()
            
    def __process_aspects_review(self, fline, product_name, id_review):
        ''' Identify the aspects from the annotations '''
        self.__corpus[product_name][id_review]['aspects'] = {}
        raw_aspects = fline.split(',')

        for aspect in raw_aspects:
            result = re.match('(A\d+)_(.+)', aspect.strip())
            self.__corpus[product_name][id_review]['aspects'][result.group(1)] = result.group(2)[0].upper() + result.group(2)[1:]#In uppercase
        
    def __process_aspects_sentence(self, fline, product_name, id_review, id_sentence):
        ''' Get the text and aspects presented in the text sentence '''
        aspects = []

        for t in re.findall("\[([^\]_\[]+)\]_\[(AI{0,1}\d+),(\+|-)\]", fline):
            aspects.append((t[1], t[2], t[0]))

        new_text = re.sub("\[([^\]_\[]+)\]_\[(AI{0,1}\d+),(\+|-)\]", r"\1", fline)
        new_text = re.sub("\[([^\]_\[]+)\]_\[(AI{0,1}\d+)\]", r"\1", new_text)
        self.__corpus[product_name][id_review]['sentences'][id_sentence]['text'] = new_text
        self.__corpus[product_name][id_review]['sentences'][id_sentence]['raw_text'] = fline
        self.__corpus[product_name][id_review]['sentences'][id_sentence]['aspects'] = aspects

    def get_text_sentence(self, product_name, id_review, id_sentence):
        ''' Return the text sentence '''
        return self.__corpus[product_name][id_review]['sentences'][id_sentence]['text'] 

    def get_sentiment_sentence(self, product_name, id_review, id_sentence):
        ''' Return the sentiment of a sentence '''
        sentence_data = self.__corpus[product_name][id_review]['sentences'][id_sentence]['aspects'] 
        return self.__get_unique_sentiment(product_name, id_review, sentence_data)

    def __get_unique_sentiment(self, product_name, id_review, sentence_data_list):
        ''' Return the number of positive and negative annotations in a sentence '''
        aspect_sentiment_list = {}

        for (id_aspect, polarity, qualifier) in sentence_data_list:
            aspect_name = self.__corpus[product_name][id_review]['aspects'][id_aspect.replace('I', '')]
            if aspect_name not in aspect_sentiment_list: aspect_sentiment_list[aspect_name] = {'+':0,  '-':0}
            aspect_sentiment_list[aspect_name][polarity] += 1 

        return aspect_sentiment_list

    ####### Methods for Opinions Summarization #######

    def get_data_sentence(self, product_name, id_review, id_sentence):
        ''' Return all the information about a sentence '''
        data = {}
        data['text'] =  self.__corpus[product_name][id_review]['sentences'][id_sentence]['text']
        annotations = []

        for (id_aspect, polarity, qualifier) in self.__corpus[product_name][id_review]['sentences'][id_sentence]['aspects']:
            aspect_name = self.__corpus[product_name][id_review]['aspects'][id_aspect.replace('I', '')]
            annotations.append({'aspect':aspect_name, 'polarity':polarity, 'qualifier':qualifier})

        data['annotations'] = annotations
        return data

    def get_stars_review(self, product_name, id_review):
        ''' Return the number of stars in a sentence '''
        return self.__corpus[product_name][id_review]['stars'] 

    def get_aspects_reviews(self, product_name):
        ''' Return the aspects of a product '''
        aspects = []

        for id_review in self.__corpus[product_name].keys():
            for aspect in self.__corpus[product_name][id_review]['aspects'].values():
                if aspect not in aspects: aspects.append(aspect)

        return aspects

    def get_aspects_sentence(self, product_name, id_review, id_sentence):
        ''' Return the aspects of a sentence '''
        aspects = []

        for aspect_tuple in self.__corpus[product_name][id_review]['sentences'][id_sentence]['aspects']:
            id_aspect = aspect_tuple[0].replace('I', '')
            aspect_name = self.__corpus[product_name][id_review]['aspects'][id_aspect]
            if aspect_name not in aspects: aspects.append(aspect_name)

        return aspects

    def get_hierarchy_aspects(self, product_name, id_review):
        ''' Return the hierarchy of aspects in a opinion '''
        aspect_list = {key: value for (key, value) in self.__corpus[product_name][id_review]['aspects'].items()}
        hierarchy_aspect_list = {aspect:[] for aspect in aspect_list.values()}

        for id_sentence in self.__corpus[product_name][id_review]['sentences'].keys():
            text =  self.__corpus[product_name][id_review]['sentences'][id_sentence]['raw_text']
            for tuplee in re.findall("\[([^\]_\[]+)\]_\[(AI{0,1}\d+)\]", text):
                id_aspect = tuplee[1].replace('I', '')
                hierarchy_aspect_list[aspect_list[id_aspect]].append(tuplee[0])

        return hierarchy_aspect_list

    def get_sentiment_reviews(self, product_name):
        ''' Return the number of positive and negative annotations about a product '''
        aspect_sentiment_list = {}

        for id_review in self.__corpus[product_name].keys():
            for sentence_data in self.__corpus[product_name][id_review]['sentences'].values():
                for aspect, polarity in self.__get_unique_sentiment(product_name, id_review, sentence_data['aspects']).items():
                    if aspect not in aspect_sentiment_list: aspect_sentiment_list[aspect] = {'+':0,  '-':0}
                    aspect_sentiment_list[aspect]['+'] += polarity['+']
                    aspect_sentiment_list[aspect]['-'] += polarity['-']

        return aspect_sentiment_list

    def get_sentiment_quantifiers(self, product_name, top_aspect):
        ''' Return the number of positive and negative annotations  and it proportion '''
        cont = 0
        size = float(len(self.__corpus[product_name]))

        for id_review in self.__corpus[product_name].keys():
            if top_aspect in self.__corpus[product_name][id_review]['aspects'].values():
                cont += 1

        return cont, cont / size

    def get_aspect_information(self, product_name, aspect):
        ''' Return information about a aspect '''
        # 0=Male-Singular, 1=Male-Plural, 2=Female-Singular, 3=Female-Plural
        return self.__aspect_information[product_name][aspect]["concord"]

    def get_raw_aspect(self, product_name, id_review, id_sentence, aspect):
        ''' Return a list of raw aspects '''
        aspect_dict = {key: value for (key, value) in self.__corpus[product_name][id_review]['aspects'].items() if value == aspect}
        raw_aspect_list = []
        text =  self.__corpus[product_name][id_review]['sentences'][id_sentence]['raw_text']

        for aspect_tuple in re.findall("\[([^\]_\[]+)\]_\[(AI{0,1}\d+)\]", text):
            id_aspect = aspect_tuple[1].replace('I', '')

            if id_aspect in aspect_dict:
                raw_aspect_list.append(aspect_tuple[0])

        return raw_aspect_list

    def __load_aspect_information(self):
        ''' Load the informations about the aspect's product '''
        with codecs.open("../resource/aspects_info_buscape.json", 'r','utf-8') as data_file:
            self.__aspect_information = json.loads(data_file.read())


if __name__ == '__main__':
    bcr = BuscapeCorpusReader("../resource/corpus_buscape/")
    #print (bcr.get_aspects("Galaxy-SIII"))
    #print (bcr.get_unique_aspects_sentence("Galaxy-SIII", "4", "3"))
    #print (bcr.get_sentiment_reviews("Galaxy-SIII"))
    #print (bcr.get_text_sentence("Galaxy-SIII", "4", "3"))
    print (bcr.get_data_sentence("Galaxy-SIII", "4", "3"))
    #print (bcr.get_aspect_information("Galaxy-SIII", "Galaxy S III"))
    #print (bcr.get_hierarchy_aspects("Galaxy-SIII", "4"))
    print (bcr.get_raw_aspect("Galaxy-SIII", "4", "3", "Galaxy S III"))
    #print( bcr.get_sentiment_quantifiers("Galaxy-SIII", "Galaxy S III"))
