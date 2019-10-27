# -*- coding: utf-8 -*-
'''
Created on 19/02/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
from random import randint
import itertools
import os
import codecs
import utils 
import re
import networkx
import json
#import matplotlib.pyplot as plt

depth_adt = -1

class Gerani_Summarizer(object):
    '''
    Class that implements Gerani method
    '''

    def __init__(self, name, opinions_path, aspect_manager):
        self.__name = name
        self.__aspect_manager = aspect_manager
        self.__graph = networkx.MultiDiGraph()
        self.__aspect_tuple_list = []
        self.__dirmoi_list = {}
        self.__moi_list = {}
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
        if top  > len(self.__graph.nodes()): top = len(self.__graph.nodes())
        page_rank = networkx.pagerank_numpy(self.__graph)

        for node in page_rank.keys():
            self.__moi_list[node] =  alpha * self.__dirmoi_list[node] + (1 - alpha) * page_rank[node]
        top_aspects = sorted(self.__moi_list.items(), key=lambda x:x[1], reverse=True)

        for (aspect, moi_value) in top_aspects[top:]:
            self.__graph.remove_node(aspect)

        #self.__draw_graph_graphviz(self.__graph, "2")
        self.__subgraph_transformation(top_aspects[0][0], top_aspects[1][0])
        
    def __subgraph_transformation(self, root, first_child):
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
        self.__microplanning(maximum_spanning_tree, root, first_child)

    def __microplanning(self, maximum_spanning_tree, root, first_child):
        ''' Plan the selection of sentences for the summary '''
        #print "Root:", root
        templates = self.__read_templates()
        sentiment_reviews = self.__aspect_manager.get_sentiment_reviews(self.__name)

        # First sentence
        aspect_info = self.__aspect_manager.get_aspect_information(self.__name, root)
        self.__summary += self.__sentence_realization(root, aspect_info, templates, sentiment_reviews, False, False)
        if self.__has_polarity_agreement(sentiment_reviews[root], sentiment_reviews[first_child]) and self.__has_connecting_relation(root, first_child):
            self.__summary += " principalmente devido %s %s" % (["ao", "aos", "à", "às"][aspect_info], first_child)
        self.__summary += ". "

        if len(maximum_spanning_tree.nodes()) == 0: return # when the number of aspects is 1

        # Sentences for children
        for aspect in maximum_spanning_tree.neighbors(root):
            #print "Aspect:", aspect
            aspect_info = self.__aspect_manager.get_aspect_information(self.__name, aspect)
            self.__summary += self.__sentence_realization(aspect, aspect_info, templates, sentiment_reviews) + ". "
            child_aspects = self.__get_child_aspects(maximum_spanning_tree.neighbors(aspect), root)

            # Sentences for children with children
            if len(child_aspects) > 0:
                if len(child_aspects) == 1:# Only one child
                    #print "Aspect Child:", child_aspects[0]
                    aspect_info = self.__aspect_manager.get_aspect_information(self.__name, child_aspects[0])
                    self.__summary +=  self.__sentence_realization(child_aspects[0], aspect_info, templates, sentiment_reviews) + ". "
                else:# Two children
                    #print "Aspect Children:", child_aspects[0], child_aspects[1]
                    aspect_info = self.__aspect_manager.get_aspect_information(self.__name, child_aspects[0])
                    self.__summary += self.__sentence_realization(child_aspects[0], aspect_info, templates, sentiment_reviews)
                    self.__summary += self.__select_template_connective_child(templates['Connectives'], self.__has_polarity_agreement(sentiment_reviews[child_aspects[0]], sentiment_reviews[child_aspects[1]]))
                    aspect_info = self.__aspect_manager.get_aspect_information(self.__name, child_aspects[1])
                    self.__summary += self.__sentence_realization(child_aspects[1], aspect_info, templates, sentiment_reviews, connective=False) + ". "
                    maximum_spanning_tree.remove_node(child_aspects[1])

                maximum_spanning_tree.remove_node(child_aspects[0])
            maximum_spanning_tree.remove_node(aspect)
        maximum_spanning_tree.remove_node(root)
        
        # For other aspects
        for aspect in  maximum_spanning_tree.nodes():
            aspect_info = self.__aspect_manager.get_aspect_information(self.__name, aspect)
            self.__summary += self.__sentence_realization(aspect, aspect_info, templates, sentiment_reviews, lowercase=True) + ". "

        #print self.__summary

    def __get_child_aspects(self, tree, root):
        ''' Return child aspects '''
        neighbor_list = []

        for neighbor in tree:
            if neighbor != root: neighbor_list.append(neighbor)

        return neighbor_list

    def __sentence_realization(self, aspect, aspect_info, templates, sentiment_reviews, lowercase=True, connective=True):
        ''' Create a sentence using different types of templates '''
        sentence = ""
        absolute_qtf, relative_qtf = self.__aspect_manager.get_sentiment_quantifiers(self.__name, aspect)

        if connective:
            sentence += self.__select_template_connective(templates['Connectives'], aspect)

        sentence += self.__select_template_quantifier(templates['Quantifiers'], aspect, absolute_qtf, relative_qtf, lowercase)
        sentence += self.__select_template_polarity(templates['PolarityVerbs'], sentiment_reviews[aspect])

        for match in re.findall("(<([^>]+)>)", sentence):
            sentence = re.sub(match[0], match[1].split(",")[aspect_info], sentence)

        return sentence

    def __select_template_quantifier(self, templates, aspect, absolute_quantifier, relative_quantifier, lowercase):
        ''' Select templates to use the aspect quantifiers '''
        template_quantifiers = None
        if relative_quantifier == 1.0: template_quantifiers = templates['1.0']
        elif relative_quantifier >= 0.8: template_quantifiers = templates['0.8']
        elif relative_quantifier >= 0.6: template_quantifiers = templates['0.6']
        elif relative_quantifier >= 0.45: template_quantifiers = templates['0.45']
        elif relative_quantifier >= 0.2: template_quantifiers = templates['0.2']
        elif relative_quantifier >= 0.0: template_quantifiers = templates['0.0']
        else: template_quantifiers = ["Not template found"]      

        template_quantifier = template_quantifiers[randint(0, len(template_quantifiers) -1)]
        template_quantifier = re.sub("<ASPECT>", aspect, template_quantifier)
        template_quantifier = re.sub("<NUMBER>", str(absolute_quantifier), template_quantifier)
        template_quantifier = re.sub("<PERCENT>", str(round(relative_quantifier * 100, 1)), template_quantifier)

        if lowercase: template_quantifier = template_quantifier[0].lower() + template_quantifier[1:]

        return template_quantifier

    def __select_template_polarity(self, templates, polarities):
        ''' Select templates to use polarity verbs '''
        template_polarity_verbs = None
        average = polarities['+'] - polarities['-']

        if average == -1 or average == 1: template_polarity_verbs = templates['Controversial']
        elif average < -3: template_polarity_verbs = templates['-3']
        elif average < -2: template_polarity_verbs = templates['-2']
        elif average < -1: template_polarity_verbs = templates['-1']
        elif average == 0: template_polarity_verbs = templates['0']
        elif average > 1: template_polarity_verbs = templates['+1']
        elif average > 2: template_polarity_verbs = templates['+2']
        elif average > 3: template_polarity_verbs = templates['+3']
        else: template_polarity_verbs = ["Not template found"]
        
        return template_polarity_verbs[randint(0, len(template_polarity_verbs) -1)]

    def __select_template_connective(self, templates, aspect): 
        ''' Select templates to connect aspects '''
        template_connectives = templates['General']
        template_connective = template_connectives[randint(0, len(template_connectives) -1)]
        template_connective = re.sub("<ASPECT>", aspect, template_connective)

        return template_connective

    def __select_template_connective_child(self, templates, agreement):
        ''' Select templates to connect child aspects '''
        template_connectives = None
        if agreement: template_connectives = templates['ChildAgreement']
        else: template_connectives = templates['ChildNoAgreement']
        template_connective = template_connectives[randint(0, len(template_connectives) -1)]

        return template_connective

    def __has_polarity_agreement(self, polarities_parent, polarities_child):
        ''' Verify the polarity  of the parent and child aspect '''
        average_parent = polarities_parent['+'] - polarities_parent['-']
        average_child = polarities_child['+'] - polarities_child['-']

        if abs(average_parent) > 1 and abs(average_child) > 1:
            return (average_parent > 0 and average_child > 0) or (average_parent < 0 and average_child < 0)
        else:
            return abs(average_parent) == 1 and abs(average_child) == 1

    def __has_connecting_relation(self, aspect_parent, aspect_child):
        ''' Verify the type of RST relations between the parent and child aspect '''
        relation_list = ["elaboration", "explain", "cause", "summary", "same-unit", "background", "evidence", "justify"]

        if self.__graph.has_edge(aspect_child, aspect_parent):
            for relation in  self.__graph[aspect_child][aspect_parent].keys():
                if relation in relation_list:
                    return True
        return False

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
        with codecs.open("../resource/templates_gerani.json", 'r','utf-8') as data_file:
            templates = json.loads(data_file.read())

        return templates