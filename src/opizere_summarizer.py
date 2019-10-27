# -*- coding: utf-8 -*-
'''
Created on 05/05/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
import random
import os
import codecs
import utils 
import re

class Opizere_Summarizer(object):
    '''
    Class that implements Opizere method
    '''

    def __init__(self, name, opinions_path, aspect_manager):
        self.__name = name
        self.__aspect_manager = aspect_manager
        self.__data = {}
        self.__sentences = {}
        self.__reviews = {}
        self.__read_files(opinions_path)

    def __read_files(self, opinions_path):
        ''' Read the files (opinions) '''
        files = sorted(os.listdir(opinions_path))
        for file_name in files:
            sentences = utils.get_sentences(os.path.join(opinions_path, file_name))
            self.__reviews[file_name] = float(len(sentences))
            for (id_sentence, text_sentence) in sentences:
                annotations = self.__aspect_manager.get_data_sentence(self.__name,  re.match('(.+)\.txt', file_name).group(1), id_sentence)['annotations']
                self.__process_annotations(file_name+'_'+id_sentence, annotations, text_sentence)

    def __process_annotations(self, id_sentence, annotations, sentence):
        ''' Process the annotations of a sentence getting the aspects and their polarities and frequencies '''
        if id_sentence not in self.__sentences: 
                self.__sentences[id_sentence] =  sentence

        for annotation in annotations:
            aspect = annotation['aspect']
            polarity = annotation['polarity']           

            if aspect not in self.__data: 
                self.__data[aspect] = {'+':{}, '-':{}, 'frequency':0}

            if id_sentence not in self.__data[aspect][polarity]: 
                self.__data[aspect][polarity][id_sentence] = 0

            self.__data[aspect][polarity][id_sentence]  += 1
            self.__data[aspect]['frequency'] += 1

    def create_summary(self, folder_path, number_aspects, number_sentences, write=True):
        ''' Create a summary given a number of aspects  and sentences  for the summary '''
        top_aspects = sorted(self.__data.items(), key=lambda x:x[1]['frequency'], reverse=True)

        if number_aspects > len(top_aspects): number_aspects =  len(top_aspects)
        text = ""

        for (aspect, data) in top_aspects[:number_aspects]:
            text += "Aspecto: %s \n" % aspect
            pos_size = len(self.__data[aspect]['+'])            
            text += "          Sentenças Positivas: %s\n" % pos_size
            items = min(number_sentences, pos_size)
            positive_sentences = self.__get_sentence_ranking(aspect, '+')
 
            for i in range(items):
                #text += "  - %.4f %s\n" % (positive_sentences[i][1], self.__sentences[positive_sentences[i][0]])
                text += "          - %s\n" % self.__sentences[positive_sentences[i][0]]

            neg_size = len(self.__data[aspect]['-'])
            text += "          Sentenças Negativas: %s\n" % neg_size
            items = min(number_sentences, neg_size)
            negative_sentences = self.__get_sentence_ranking(aspect, '-')

            for i in range(items):
                #text += "  - %.4f %s\n" % (negative_sentences[i][1], self.__sentences[negative_sentences[i][0]])
                text += "          - %s\n" % self.__sentences[negative_sentences[i][0]]

        if write:
            with codecs.open(os.path.join(folder_path, self.__name + ".sum"), 'w','utf-8') as fout:
                fout.write(text)
        else:
            return text

    def __get_sentence_ranking(self, aspect, polarity, alpha=0.5):
        ''' Rank sentences of an aspect according to their positions and proximities '''
        sentence_by_position = self.__get_sentence_by_position(aspect, polarity)
        sentence_by_proximity = self.__get_sentence_by_proximity(aspect, polarity)
        raking_list = {}

        for id_sentence in sentence_by_proximity.keys():
            raking_list[id_sentence] = alpha * sentence_by_position[id_sentence] + (1.0 - alpha) * sentence_by_proximity[id_sentence]
        
        return sorted(raking_list.items(), key=lambda x:x[1], reverse=True)

    def __get_sentence_by_polarity(self, aspect, polarity):
        ''' Sort sentences according to their polarities '''
        tmp_list = self.__data[aspect][polarity]
        return sorted(tmp_list.items(), key=lambda x:x[1], reverse=True)

    def __get_sentence_by_position(self, aspect, polarity):
        ''' Sort sentences according to their positions in the opinions '''
        sentences_list = {}
        
        for id_sentence in self.__data[aspect][polarity]:
            match = re.match('(.+)_(\d+)', id_sentence)
            review_size = self.__reviews[match.group(1)]
            sentence_position = int(match.group(2))
            #value = abs(sentence_position - (review_size / 2)) / review_size
            value = (review_size - sentence_position) / review_size
            sentences_list[id_sentence] = value

        #return sorted(sentences_list.items(), key=lambda x:x[1], reverse=True)
        return sentences_list

    def __get_sentence_by_proximity(self, aspect, polarity):
        ''' Sort sentences according to the proximities between their aspects and qualifiers '''
        sentences_list = {}

        for id_sentence in self.__data[aspect][polarity]:
            match = re.match('(\d+).txt_(\d+)', id_sentence)
            raw_aspects = self.__aspect_manager.get_raw_aspect(self.__name, match.group(1), match.group(2), aspect)
            sentence_data = self.__aspect_manager.get_data_sentence(self.__name, match.group(1), match.group(2))
            distance = self.__calculate_min_distance(aspect, raw_aspects, sentence_data)
            sentences_list[id_sentence] = 1.0 - distance
            
        #return sorted(sentences_list.items(), key=lambda x:x[1], reverse=True)
        return sentences_list

    def __calculate_min_distance(self, aspect, raw_aspects, sentence_data):
        ''' Calculate the minimum distance between aspects and their qualifiers '''
        text = sentence_data['text']
        min_distance = len(text)

        for annotation in sentence_data['annotations']:
            if aspect == annotation['aspect']:
                qualifier = annotation['qualifier']
                
                for raw_aspect in raw_aspects:
                    index_aspect = text.find(raw_aspect)
                    if index_aspect != -1:
                        index_qualifier = text.find(qualifier)
                        if index_aspect < index_qualifier:
                            index_aspect += len(raw_aspect)
                        else:
                            index_qualifier += len(qualifier)
                        
                        distance = abs(index_aspect - index_qualifier)
                        if distance < min_distance:
                            min_distance = distance

                    else: #for implicit aspects
                        min_distance = text.find(qualifier)

        return float(min_distance) / len(text)