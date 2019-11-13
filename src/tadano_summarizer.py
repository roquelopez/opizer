# -*- coding: utf-8 -*-
'''
Created on 05/05/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
from numpy import array
from nltk.probability import FreqDist
from nltk.corpus import stopwords
from nltk.tokenize import RegexpTokenizer
from nltk.cluster import KMeansClusterer, GAAClusterer, euclidean_distance
try:
    from cStringIO import StringIO
except:
    from io import StringIO
import utils
import sys
import nlpnet
import codecs
import os
import re
import math

nlpnet.set_data_dir(str("../resource//nlpnet_data/"))
stop_words = stopwords.words('portuguese')
tokenizer = RegexpTokenizer(r'\w+')

class Tadano_Summarizer(object):
    '''
    Class that implements Tadano method
    '''

    def __init__(self, name, opinions_path, aspect_manager):
        self.__name = name
        self.__aspect_manager = aspect_manager
        self.__sentence_list = {}
        self.__clusters = {}
        self.__aspect_list = {key:0 for key in aspect_manager.get_aspects_reviews(name)}
        self.__read_files(opinions_path)

    def __read_files(self, opinions_path):
        ''' Read the files (opinions) '''
        files = sorted(os.listdir(opinions_path))
        all_words = {}

        for file_name in files:
            sentences = utils.get_sentences(os.path.join(opinions_path, file_name))
            stars = self.__aspect_manager.get_stars_review(self.__name,  re.match('(.+)\.txt', file_name).group(1))
            
            review_words = []
            for (id_sentence, text_sentence) in sentences:
                id_general = id_sentence + "_" + file_name
                clean_text = self.__clean_text(text_sentence)
                review_words += clean_text
                self.__sentence_list[id_general] = {'clean_text': clean_text, 'raw_text': text_sentence, 'stars':stars, 
                                                    'tfidf_sentence':0, 'tfidf_words':{}}

            for word in review_words:
                if word not in all_words: all_words[word] = []
                if file_name not in all_words[word]: all_words[word].append(file_name)

        self.__calculate_tfidf(float(len(files)), all_words)

    def __calculate_tfidf(self, revall, all_words):
        ''' Calculate a variation of the TF-IDF for all words presented in the opinions '''
        for id_sentence, data_sentence in self.__sentence_list.items():
            clean_text = data_sentence['clean_text']
            frequency_review = FreqDist(clean_text)
            len_words = float(len(clean_text))
            tfidf_sentence = 0

            for word in clean_text:
                revi = len(all_words[word])
                if len_words > 1:
                    tfidf_word = (math.log(frequency_review[word] + 1, 2) /  math.log(len_words, 2)) *  (math.log(revall / revi, 2) + 1)
                    tfidf_sentence += tfidf_word
                else:
                    tfidf_word = frequency_review[word]

                self.__sentence_list[id_sentence]['tfidf_words'][word] = tfidf_word

            if len_words > 0:
                self.__sentence_list[id_sentence]['tfidf_sentence'] = tfidf_sentence / len_words

        self.__create_vectors(all_words.keys())

    def __create_vectors(self, words):
        ''' Create the  vector representations of all words '''
        vectors = []
        words = {word:index for index, word in enumerate(words)}
        dimension_size = len(words)
        id_sentences = self.__sentence_list.keys()
        
        for id_sentence in id_sentences:
            vector = [0] * dimension_size

            for word in self.__sentence_list[id_sentence]['clean_text']:
                index = words[word]
                vector[index] = self.__sentence_list[id_sentence]['tfidf_words'][word]
            
            vectors.append(vector)

        self.__iterate_aspects(vectors, id_sentences)

    def __iterate_aspects(self, vectors, id_sentences):
        ''' Create the  vector representations of words about an aspect '''
        for aspect in self.__aspect_list.keys():
            self.__clusters[aspect] = {}
            id_sentences_aspect = []
            vectors_aspect = []

            for index, id_sentence in enumerate(id_sentences):
                match = re.match('(\d+)_(\d+).txt', id_sentence)
                if aspect in self.__aspect_manager.get_aspects_sentence(self.__name, match.group(2), match.group(1)):
                    id_sentences_aspect.append(id_sentence)
                    vectors_aspect.append(vectors[index])
                    self.__aspect_list[aspect] += 1

            if len(vectors_aspect) > 0:
                self.__kmeans(aspect, vectors_aspect, id_sentences_aspect)

    def __kmeans(self, aspect, vectors, id_sentences, k=50):
        ''' Cluster sentences using the K-Means Algorithm '''
        k = min(k, len(vectors))
        vectors = [array(v) for v in vectors]
        means = vectors[:k]
        clusterer = KMeansClusterer(k, euclidean_distance, initial_means=means, avoid_empty_clusters=True)
        with utils.Capturing() as output:
            clusters = clusterer.cluster(vectors, True)
        
        for id_cluster in range(k):
            self.__clusters[aspect][id_cluster] = {'importance': 0, 'sentences':[], 'representative_words':[], 'max_sentence':None}

        for index, id_cluster in enumerate(clusters):
            self.__clusters[aspect][id_cluster]['sentences'].append(id_sentences[index])

        for id_cluster in range(k):# Delete empty clusters
            if len(self.__clusters[aspect][id_cluster]['sentences']) == 0:
                self.__clusters[aspect].pop(id_cluster)

        self.__search_representative_words(aspect)

    def __search_representative_words(self, aspect):
        ''' Search representative words using POS tags or word frequencies '''
        tagger = nlpnet.POSTagger()

        for id_cluster, data in self.__clusters[aspect].items():
            size_cluster = len(data['sentences'])
            if  size_cluster == 1:
                words = self.__sentence_list[data['sentences'][0]]['clean_text']
                words_tags = tagger.tag(" ".join(words))[0]
                representative_words = [word for (word, tag) in words_tags if tag == "N" or tag == "ADJ"]
                self.__clusters[aspect][id_cluster]['representative_words'] = representative_words
            else:
                words = []
                size_cluster /= 2
                
                for id_sentence in data['sentences']:
                    words += self.__sentence_list[id_sentence]['clean_text']

                frequency_words = FreqDist(words)
                
                for word in frequency_words.keys():
                    if frequency_words[word] > size_cluster:
                        self.__clusters[aspect][id_cluster]['representative_words'].append(word)
                    else:
                        break

            self.__clusters[aspect][id_cluster]['representative_words'] = self.__search_top_words(data['sentences'],
                                                                                                  self.__clusters[aspect][id_cluster]['representative_words'])
        self.__join_clusters(aspect)

    def __join_clusters(self, aspect):
        ''' Join clusters with common representative words '''
        clusters = list(self.__clusters[aspect].keys())
        clusters_size = len(clusters)
        tuple_list = []

        for i in range(clusters_size):
            for j in range(i+1, clusters_size):
                common_words =  self.__has_common_words(self.__clusters[aspect][clusters[i]]['representative_words'],
                                                        self.__clusters[aspect][clusters[j]]['representative_words'])
                if common_words:
                    tuple_list.append((clusters[i], clusters[j]))

        relations = {}

        for (cluster_a, cluster_b) in tuple_list:
            if cluster_b in self.__clusters[aspect]:
                if cluster_a not in self.__clusters[aspect]:
                    cluster_a = relations[cluster_a]

                self.__clusters[aspect][cluster_a]['sentences'] += self.__clusters[aspect][cluster_b]['sentences']
                self.__clusters[aspect][cluster_a]['representative_words'] += self.__clusters[aspect][cluster_b]['representative_words']
                self.__clusters[aspect].pop(cluster_b)
                relations[cluster_b] = cluster_a

        self.__calculate_importance(aspect)

    def __calculate_importance(self, aspect):
        ''' Calculate the importance of all sentence for an aspect '''
        for id_cluster, data in self.__clusters[aspect].items():
            cluster_size = len(data['sentences'])
            tfidf_sentences = 0
            max_tfidf = -1
            id_max_sentence = None
            
            for id_sentence in data['sentences']:
                tfidf_sentence = self.__sentence_list[id_sentence]['tfidf_sentence']
                tfidf_sentences += tfidf_sentence

                if tfidf_sentence > max_tfidf:
                    max_tfidf = tfidf_sentence
                    id_max_sentence = id_sentence

            self.__clusters[aspect][id_cluster]['max_sentence'] = id_max_sentence
            self.__clusters[aspect][id_cluster]['importance'] = (tfidf_sentences / cluster_size) * math.log(cluster_size + 1, 2)

    def create_summary(self, folder_path, number_aspects, number_sentences, write=True):
        ''' Create a summary given a number of aspects  and sentences  for the summary '''
        sentences = []
        top_aspects = sorted(self.__aspect_list.items(), key=lambda x:x[1], reverse=True)
        number_aspects = min(number_aspects, len(self.__aspect_list))

        for aspect, frequency in top_aspects[:number_aspects]:
            top_clusters = sorted(self.__clusters[aspect].items(), key=lambda x:x[1]['importance'], reverse=True)
            rating_values = {'1':[], '2':[], '3':[], '4':[], '5':[]}
            
            for id_cluster, data in top_clusters:
                rating = self.__sentence_list[data['max_sentence']]['stars']
                id_sentence = data['max_sentence']
                rating_values[rating].append(id_sentence)

            top_ratings = sorted(rating_values.items(), key=lambda x:len(x[1]), reverse=True)
            number_sentences = min(number_sentences, len(top_clusters))
            sentences += self.__select_sentences(number_sentences, top_ratings) 
                
        if write:
            with codecs.open(os.path.join(folder_path, self.__name + ".sum"), 'w','utf-8') as fout:
                fout.write("\n".join(sentences))
        else:
            return "\n".join(sentences)
        
    def __select_sentences(self, number_sentences, top_ratings): 
        ''' Select a given number of sentences for the summary '''
        cont_sentences = 0
        index_rating = 0
        sentences = []

        while True:
            for rating, id_sentences in top_ratings:
                if index_rating < len(id_sentences):
                    sentences.append(self.__sentence_list[id_sentences[index_rating]]['raw_text'])
                    cont_sentences += 1

                if cont_sentences == number_sentences:
                    return sentences

            index_rating += 1

    def __search_top_words(self, id_sentences, representative_words):
        ''' Get the words with maximum TF-IDF values '''
        max_tfidf = -1
        top_words = []

        for id_sentence in id_sentences:
            for word in representative_words:
                if word in self.__sentence_list[id_sentence]['tfidf_words']:
                    tfidf = self.__sentence_list[id_sentence]['tfidf_words'][word]
                    if tfidf > max_tfidf:
                        max_tfidf = tfidf
                        top_words = [word]
                    elif tfidf == max_tfidf:
                        top_words.append(word)

        return top_words

    def __has_common_words(self, words1, words2):
        ''' Verify if two lists have common words '''
        for word1 in words1:
            if word1 in words2:
                return True
        return False

    def __clean_text(self, text):
        ''' Clean a text removing stopwords and lemmatizing '''
        text = text.lower()
        #text = utils.lemmatize(text)
        return [x for x in tokenizer.tokenize(text) if x not in stop_words]

    def print_clusters(self, aspect):
        ''' Print the clusters ''' 
        for id_cluster, data in self.__clusters[aspect].items():
            print ("Cluster ", id_cluster)
            for id_sentence in data['sentences']:
                print (" ".join(self.__sentence_list[id_sentence]['clean_text']))
