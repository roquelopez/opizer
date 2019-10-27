# -*- coding: utf-8 -*-
'''
Created on 05/05/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
from random import randint
from nltk.corpus import stopwords
from nltk.tokenize import RegexpTokenizer
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer
import itertools
import nlpnet
import os
import codecs
import utils 
import re
import networkx
import json
#import matplotlib.pyplot as plt

depth_adt = -1
tokenizer = RegexpTokenizer(r'\w+')
nlpnet.set_data_dir(str("../resource//nlpnet_data/"))
tagger = nlpnet.POSTagger()

class Opizera_Summarizer(object):
    '''
    Class that implements Opizera method
    '''

    def __init__(self, name, opinions_path, aspect_manager):
        self.__name = name
        self.__aspect_manager = aspect_manager
        self.__graph = networkx.MultiDiGraph()
        self.__aspect_tuple_list = []
        self.__qualifier_list = {}
        self.__dirmoi_list = {}
        self.__moi_list = {}
        self.__cluster_list = {}
        self.__read_files(opinions_path)
        self.__read_dizer_files()
        self.__create_aspect_rhetorical_graph()
        self.__summary = ""

    def __read_files(self, opinions_path):
        ''' Read the annotations of the files (opinions) '''
        files = sorted(os.listdir(opinions_path))
        for file_name in files:
            sentences = utils.get_sentences(os.path.join(opinions_path, file_name))
            for (id_sentence, text_sentence) in sentences:
                annotations = self.__aspect_manager.get_data_sentence(self.__name,  re.match('(.+)\.txt', file_name).group(1), id_sentence)['annotations']
                self.__process_annotations(annotations)

    def __process_annotations(self, annotations):
        ''' Process the annotations of a sentence getting the aspects and their polarities '''
        for annotation in annotations:
            aspect = annotation['aspect']
            polarity = annotation['polarity']

            if aspect not in self.__qualifier_list: self.__qualifier_list[aspect] = {'+':[], '-':[]}
            self.__qualifier_list[aspect][polarity].append(annotation['qualifier'])
            if aspect not in self.__dirmoi_list: self.__dirmoi_list[aspect] = 0
            if polarity == '+': polarity_value = 1 # polarity strengh by default 1 or -1
            else: polarity_value = -1
            self.__dirmoi_list[aspect] += polarity_value ** 2

    def __read_dizer_files(self):
        ''' Read the RST files (output of parser Dizer) '''
        folder_path = os.path.join("../resource/dizer/", self.__name)
        files = sorted(os.listdir(folder_path))
        for file_name in files:
            self.__read_discourse_tree(file_name, os.path.join(folder_path, file_name))

    def __read_discourse_tree(self, file_name, file_path):
        ''' Read the RST annotations of parser Dizer '''
        global depth_adt 
        segment_list = {}

        with codecs.open(file_path, 'r', 'utf-8') as fin:
            lines = fin.readlines()

        for i in  range(1, len(lines)):
            text = lines[i].strip()
            if text == "Trees:":
                tree = lines[i+1].strip().replace(';', '').replace("'", "")
                break
            else:
                key = str(i)
                segment =  re.match(key + ': (.+)', text).group(1)
                segment_list[key] = segment

        aspect_list = self.__search_aspects(file_name, segment_list)
        relation_list = []
        self.__parse_discourse_tree(tree, relation_list)
        self.__create_aspect_relation_tuples(aspect_list, relation_list, float(len(segment_list)))
        depth_adt = -1

    def __search_aspects(self, file_name, segment_list):
        ''' Search the aspects in the segments or EDUs (Elementary Discourse Units) identified by Dizer '''
        aspect_list = self.__aspect_manager.get_hierarchy_aspects(self.__name, re.match('(.+)\.txt', file_name).group(1))
        found_aspect_list = {}

        for id_segment, text_segment in segment_list.items():
            text_segment = self.__clean_text_dizer(text_segment)
            for ud_aspect, raw_aspects in aspect_list.items():
                for raw_aspect in raw_aspects:
                    if raw_aspect.lower() in text_segment: #doubt
                        if id_segment not in found_aspect_list: found_aspect_list[id_segment] = []
                        if ud_aspect not in found_aspect_list[id_segment]: found_aspect_list[id_segment].append(ud_aspect)

        return found_aspect_list

    def __parse_discourse_tree(self, tree_text, relation_list):
        ''' Read the structure of the RST trees '''
        tmp_text = ""
        cont = 0
        flag_first = True
        size = len(tree_text)

        for index, letter in enumerate(tree_text):
            if letter == "(":
                cont += 1
                if flag_first and tmp_text != "":
                    relation = tmp_text.replace("(", "")
                    if relation[0].isdigit(): break
                    tmp_text = ""
                    flag_first = False
            elif letter == ")": cont -= 1
            tmp_text += letter

            if cont == 0:
                if re.match("\(\d+\)", tmp_text): break
                elif not flag_first:
                    distance_list = self.__calculate_distance(tmp_text)
                    relation_list.append((relation, distance_list))                  
                    self.__parse_discourse_tree(self.__remove_span(tmp_text), relation_list)# left side
                    break

        if index < size - 1 and tree_text[index+1] == ",":
            self.__parse_discourse_tree(self.__remove_span(tree_text[index+2:].strip()), relation_list)# right side  

    def __calculate_distance(self, text):
        ''' Calculate the distance between the EDUs '''
        global depth_adt
        cont, i = 0, 0
        size = len(text)
        distance_list = []

        while i < size:
            if text[i] == "(": 
                cont += 1
            elif text[i] == ")":
                cont -= 1
            elif (text[i] == "n" or text[i] == "s") and re.match("\W", text[i-1]):
                if cont-1 == 0: role = text[i]#scope
            elif text[i].isdigit():
                digit = re.match("(\d+).*", text[i:]).group(1)
                distance = cont/2
                if distance > depth_adt: depth_adt = distance
                distance_list.append((digit, role, distance))
                i += len(digit) - 1
            i += 1

        return distance_list

    def __create_aspect_relation_tuples(self, aspect_list, relation_list, total_edu):
        ''' Create tuples of aspects, RST relations and their levels of confidence '''
        combination_list = []
        tuple_dict = {}

        for (relation, edu_list) in relation_list:
            combination_list += self.__generate_combinations(edu_list, aspect_list, relation, total_edu)
        
        for (aspect1, relation, aspect2, confidence) in combination_list:
            if (aspect1, relation, aspect2) in tuple_dict:
                if confidence > tuple_dict[(aspect1, relation, aspect2)]:# the maximum confidence
                    tuple_dict[(aspect1, relation, aspect2)] = confidence
            else:
                tuple_dict[(aspect1, relation, aspect2)] = confidence

        for key, value in tuple_dict.items():
            self.__aspect_tuple_list.append((key[0], key[1], key[2], value))

    def __generate_combinations(self, edu_list, aspect_list, relation, total_edu):
        ''' Generate combinations of aspects and RST relations '''
        combination_edu_list = []
        combination_aspect_list = []

        for (id_edu1, role1, distance1) in edu_list:
            if id_edu1 in aspect_list and role1 == 's':#satellite -->(go) nucleus
                for (id_edu2, role2, distance2) in edu_list:
                    if id_edu2 in aspect_list and id_edu2 != id_edu1 and role2 != role1:
                        confidence = self.__calculate_confidence(int(id_edu1), int(id_edu2), distance1, distance2, total_edu)
                        combination_edu_list.append((id_edu1, id_edu2, confidence))

        for (id_edu1, id_edu2, confidence) in combination_edu_list:
            for aspects in itertools.product(aspect_list[id_edu1], aspect_list[id_edu2]):
                if aspects[0] != aspects[1]:
                    combination_aspect_list.append((aspects[0], relation, aspects[1], confidence))
        
        return combination_aspect_list

    def __calculate_confidence(self, id_edu1, id_edu2, distance1, distance2, total_edu):
        ''' Calculate the confidence of the RST relations '''
        depth_subtree = float(distance1) if distance1 > distance2 else float(distance2)
        w = 1 - 0.5 * ((abs(id_edu1 - id_edu2) - 1) / total_edu) - 0.5 * (depth_subtree / depth_adt)
        return w

    def __create_aspect_rhetorical_graph(self):
        ''' Create a graph using the aspects and their RST relations '''
        for (aspect1, relation, aspect2, confidence) in self.__aspect_tuple_list:
            self.__graph.add_node(aspect1)
            self.__graph.add_node(aspect2)

            if self.__graph.has_edge(aspect1, aspect2) and relation in self.__graph[aspect1][aspect2]:
                w_value = round(self.__graph[aspect1][aspect2][relation]['weight'] + confidence, 3)
                self.__graph.add_edge(aspect1, aspect2, key=relation, weight=w_value, label=relation+":"+str(w_value))
            else:
                w_value = round(confidence, 3)
                self.__graph.add_edge(aspect1, aspect2, key=relation, weight=w_value, label=relation+":"+str(w_value))

        #self.__draw_graph_graphviz(self.__graph, "1")

    def create_summary(self, folder_path, top, write=True):
        ''' Create a summary given a number of aspects '''
        self.__subgraph_extraction(top)
        
        if write:
            with codecs.open(os.path.join(folder_path, self.__name + ".sum"), 'w','utf-8') as fout:
                fout.write(self.__summary)
        else:
            return self.__summary

    def __subgraph_extraction(self, top=5, alpha=0.5):
        ''' Extract a subgraph taking into account the desirable number of aspects '''
        if top > len(self.__graph.nodes()): top = len(self.__graph.nodes())
        page_rank = networkx.pagerank_numpy(self.__graph)

        for node in page_rank.keys():
            self.__moi_list[node] =  alpha * self.__dirmoi_list[node] + (1 - alpha) * page_rank[node]
        top_aspects = sorted(self.__moi_list.items(), key=lambda x:x[1], reverse=True)

        for (aspect, moi_value) in top_aspects[top:]:
            self.__graph.remove_node(aspect)

        #self.__draw_graph_graphviz(self.__graph, "2")
        self.__clustering(self.__graph.nodes())
        self.__subgraph_transformation(top_aspects[0][0])
        
    def __clustering(self, aspects, k=3):  
        ''' Cluster the segments (qualifiers) of an aspect '''      
        for aspect in aspects:
            self.__cluster_list[aspect] = {'+':[], '-':[]}
            for polarity in self.__qualifier_list[aspect].keys():
                qualifiers = self.__qualifier_list[aspect][polarity]
                #print aspect, polarity
                if len(qualifiers) > 0:
                    num_clusters = min(k, len(qualifiers))
                    vectorizer = TfidfVectorizer(tokenizer=tokenizer.tokenize,
                                                stop_words=stopwords.words('portuguese'),
                                                min_df=0.0, lowercase=True)
         
                    tfidf_model = vectorizer.fit_transform(qualifiers)
                    km_model = KMeans(n_clusters=num_clusters)
                    km_model.fit(tfidf_model)
                    self.__cluster_list[aspect][polarity] = self.__rank_qualifiers(qualifiers, num_clusters, km_model.labels_, tfidf_model.toarray())
                    #print ">", self.__cluster_list[aspect][polarity]

    def __rank_qualifiers(self, qualifiers, num_clusters, clusters, tfidf_values):
        ''' Rank the qualifiers using the size of the clusters '''
        tmp_dict = {i:[] for i in range(num_clusters)}

        for index, id_cluster in enumerate(clusters):
            tmp_dict[id_cluster].append(index)

        top_clusters = sorted(tmp_dict.items(), key=lambda x:len(x[1]), reverse=True)
        ranking_qualifiers = []

        for id_cluster, index_qualifiers in top_clusters:
            index_max_tfidf = self.__get_max_tfidf(index_qualifiers, tfidf_values, qualifiers)
            ranking_qualifiers.append(qualifiers[index_max_tfidf])

        return ranking_qualifiers

    def __get_max_tfidf(self, indexes, matrix, qualifiers):
        ''' Return the segment (qualifier) with the maximum TF-IDF value '''
        max_tfidf = -1.0
        max_index = 0

        for index in indexes:
            sum_tfidf = matrix[index].sum()
            size_tokens = len(tokenizer.tokenize(qualifiers[index]))
            normalized_tfidf = sum_tfidf / size_tokens

            if normalized_tfidf > max_tfidf:
                max_tfidf = normalized_tfidf
                max_index = index

        return max_index        

    def __subgraph_transformation(self, root):
        ''' Calculate the Maximum Spanning Tree of the subgraph '''
        tmp_graph = networkx.Graph()

        for u,v,data in self.__graph.edges_iter(data=True):   
            if tmp_graph.has_edge(u,v):
                w_value = tmp_graph[u][v]['weight'] - data['weight']
                tmp_graph[u][v]['weight'] = w_value
                tmp_graph[u][v]['label'] = w_value
            else:
                w_value = -data['weight']
                tmp_graph.add_edge(u, v, weight=w_value, label=w_value)

        #self.__draw_graph_graphviz(tmp_graph, "3")
        maximum_spanning_tree = networkx.minimum_spanning_tree(tmp_graph)
        #self.__draw_graph_graphviz(maximum_spanning_tree, "4")
        self.__microplanning(maximum_spanning_tree, root)

    def __microplanning(self, maximum_spanning_tree, root):
        ''' Plan the selection of sentences for the summary '''
        templates = self.__read_templates()
        sentiment_reviews = self.__aspect_manager.get_sentiment_reviews(self.__name)

        # First sentence
        aspect_info = self.__aspect_manager.get_aspect_information(self.__name, root)
        self.__summary += self.__first_sentence_realization(root, aspect_info, templates, sentiment_reviews[root])
        previous_aspect = root
        
        if len(maximum_spanning_tree.nodes()) == 0: return # when the number of aspects is 1

        # Sentences for children
        for aspect in maximum_spanning_tree.neighbors(root):
            aspect_info = self.__aspect_manager.get_aspect_information(self.__name, aspect)
            self.__summary += self.__sentence_realization(aspect, previous_aspect, aspect_info, templates, sentiment_reviews)
            previous_aspect = aspect

            # Sentences for child with children
            for aspect_child in maximum_spanning_tree.neighbors(aspect):
                if aspect_child != root:
                    aspect_info = self.__aspect_manager.get_aspect_information(self.__name, aspect_child)
                    self.__summary += self.__sentence_realization(aspect_child, previous_aspect, aspect_info, templates, sentiment_reviews)
                    previous_aspect = aspect_child
                    maximum_spanning_tree.remove_node(aspect_child)                    
            
            maximum_spanning_tree.remove_node(aspect)
        maximum_spanning_tree.remove_node(root)

        # For other aspects
        for aspect in  maximum_spanning_tree.nodes():
            aspect_info = self.__aspect_manager.get_aspect_information(self.__name, aspect)
            self.__summary += self.__sentence_realization(aspect, previous_aspect, aspect_info, templates, sentiment_reviews)
            previous_aspect = aspect
            
    def __first_sentence_realization(self, aspect, aspect_info, templates, polarities):
        ''' Create the first sentence summary '''
        polarity = self.__get_polarity(polarities)
        
        if polarity == '0':
            sentences = templates['First_Sentence']['Controversial']
        else:
            sentences = templates['First_Sentence']['Polarity']

        sentence = sentences[randint(0, len(sentences) -1)]
        if polarity != '0':
            adjetives = templates['PolarityAdjetives'][polarity]
            sentence += adjetives[randint(0, len(adjetives) -1)]

        sentence = self.__fill_slots(sentence, aspect, aspect_info)

        return sentence + ". "

    def __sentence_realization(self, aspect, previous_aspect, aspect_info, templates, sentiment_reviews):
        ''' Create a sentence using different types of templates '''
        sentence = ""
        agreement_id = self.__get_id_agreement(sentiment_reviews[aspect], sentiment_reviews[previous_aspect])
        sentence += self.__select_template_connective(aspect, agreement_id, templates['Connectives'])
        sentence += self.__select_template_polarity(templates['PolarityVerbs'], sentiment_reviews[aspect])
        sentence += self.__select_template_explanation(aspect, templates['Explanation'], sentiment_reviews[aspect])
        sentence = self.__fill_slots(sentence, aspect, aspect_info)

        return sentence

    def __select_template_connective(self, aspect, agreement_id, templates):
        ''' Select templates to use connectives in the sentence '''
        template_agreements = templates[agreement_id]
        sentence = template_agreements[randint(0, len(template_agreements) -1)]
        sentence += templates['General'][randint(0, len(templates['General']) -1)]

        return sentence

    def __select_template_polarity(self, templates, polarities):
        ''' Select templates to use polarity verbs '''
        polarity = self.__get_polarity(polarities)

        if polarity == '0':
            template_polarity_verbs = templates['Controversial']
        else:
            template_polarity_verbs = templates[polarity]
        
        return template_polarity_verbs[randint(0, len(template_polarity_verbs) -1)]

    def __select_template_explanation(self, aspect, templates, polarities):
        ''' Select templates for explanations in the sentence '''
        polarity = self.__get_polarity(polarities)
        template_explanations = templates['Explanation']
        sentence = template_explanations[randint(0, len(template_explanations) -1)]
        #print aspect

        if polarity == '0':
            qualifier = self.__cluster_list[aspect]['+'][0].lower()
            connective = self.__select_connective(qualifier)
            sentence += connective + qualifier + ", mas"
            qualifier = self.__cluster_list[aspect]['-'][0].lower()
            connective = self.__select_connective(qualifier)
            sentence += connective + qualifier
        else:
            qualifier = self.__cluster_list[aspect][polarity[0]][0].lower()
            connective = self.__select_connective(qualifier)
            sentence += connective + qualifier
                  
        return sentence + ". "

    def __select_connective(self, phrase):
        ''' Select templates to connect the segments (qualifiers) '''
        words_tags = tagger.tag(phrase)[0]
        first_word , first_tag = words_tags[0]
        connective = None

        if first_word == "não": 
            connective = " "
        elif first_tag == "V" or first_tag == "VAUX": 
            connective = " "
        elif first_tag == "ADJ" and len(words_tags) > 1 and words_tags[1][1] == "N": 
            connective = " tem "
        elif first_tag == "N" and len(words_tags) > 1 and words_tags[1][1] == "PREP": 
            connective = " tem "
        elif (first_tag == "ADJ" or first_tag == "N") and first_word[-1:] == "s":
            connective = " são "
        else: 
            connective = " é "
        
        return connective

    def __fill_slots(self, sentence, aspect, aspect_info):
        ''' Fill the template using the aspect information '''
        sentence = re.sub("<ASPECT>", aspect, sentence)

        for match in re.findall("(<([^>]+)>)", sentence):
            sentence = re.sub(match[0], match[1].split(",")[aspect_info], sentence)

        return sentence

    def __get_polarity(self, polarities):
        ''' Return the polarity id of an aspect '''
        total = float(polarities['+'] + polarities['-'])
        positive = polarities['+'] / total 
        negative = polarities['-'] / total
        diff = abs(polarities['+'] - polarities['-'])/ total
        polarity = None
        
        if diff < 0.2:
            polarity = '0'
        else:
            if positive >= 0.75: 
                polarity = '+2'
            elif positive >= 0.60: 
                polarity = '+1'
            elif negative >= 0.75: 
                polarity = '-2'
            else:# negative >= 0.60: 
                polarity = '-1'

        return polarity

    def __get_id_agreement(self, polarities_a, polarities_b):
        ''' Return the id agreement between two aspect polarities '''
        polarity_a = self.__get_polarity(polarities_a)
        polarity_b = self.__get_polarity(polarities_b)

        if polarity_a == '0' or polarity_b == '0':
            return 'Normal'
        elif polarity_a[0] == '+' and polarity_b[0] == '+':
            return 'Agreement'
        elif polarity_a[0] == '-' and polarity_b[0] == '-':
            return 'Agreement'
        else:
            return 'NoAgreement'

    def __draw_graph_matplotlib(self, name):
        ''' Plot the graph using matplotlib '''
        pos=networkx.spring_layout(self.__graph)
        networkx.draw(self.__graph, pos, with_labels=True, font_size=7)
        networkx.draw_networkx_nodes(self.__graph, pos)
        networkx.draw_networkx_edge_labels(self.__graph, pos, font_size=6)
        #plt.savefig("../resource/%s.png" % name)
        #plt.show()

    def __draw_graph_graphviz(self, graph, name):
        ''' Plot the graph using graphvis '''
        A = networkx.to_pydot(graph)
        A.write_png("../resource/%s.png" % name)

    def __clean_text_dizer(self, text):
        ''' Remove some annotations of parser Dizer '''
        text = re.sub("\s+", " ", text)
        for match in re.finditer("(\([^\s]+\)_[^\s]+)(\s|$)", text):
            text = text.replace(match.group(1), "")

        return text.lower() 

    def __remove_span(self, text):
        ''' Remove some substrings of the RST annotations '''
        if text.startswith("(n") or text.startswith("n(") or text.startswith("(s") or text.startswith("s("): text = text[2:-1] # to eliminate  (n|s ... )
        else: text = text[1:-1]# to eliminate  ( ... )
        return text

    def __read_templates(self):
        ''' Read the manual templates '''
        with codecs.open("../resource/templates_opizera.json", 'r','utf-8') as data_file:
            templates = json.loads(data_file.read())

        return templates