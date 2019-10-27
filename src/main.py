# -*- coding: utf-8 -*-
'''
Created on 03/08/2014

@author: Roque Lopez
'''
from __future__ import unicode_literals
from tadano_summarizer import Tadano_Summarizer
from huliu_summarizer import HuLiu_Summarizer
from gerani_summarizer import Gerani_Summarizer
from ganesan_summarizer import Ganesan_Summarizer
from opizere_summarizer import Opizere_Summarizer
from opizera_summarizer import Opizera_Summarizer
from buscape_reader import BuscapeCorpusReader
from reli_reader import ReLiCorpusReader
import os
import sys

if __name__ == '__main__':
    ''' Main class which select a summarizer '''
    summarizer = sys.argv[1]
    corpus = sys.argv[2]
    print "Starting process ..."

    if corpus == "buscape":
        corpus_reader = BuscapeCorpusReader("../resource/corpus_buscape")
        reviews_path = "../resource/reviews_buscape/"
    elif corpus == "reli":
        corpus_reader = ReLiCorpusReader("../resource/corpus_reli_mini")
        reviews_path = "../resource/reviews_reli/"
        

    for item in os.listdir(reviews_path):
        print "Generating summary for %s" % item
        item_path = os.path.join(reviews_path, item)
        if summarizer == "huliu":
            hu_liu = HuLiu_Summarizer(item, item_path, corpus_reader)
            hu_liu.create_summary("../resource/automatic_summaries/hu_liu", 5, 1)
        elif summarizer == "tadano":
            tadano = Tadano_Summarizer(item, item_path, corpus_reader)
            tadano.create_summary("../resource/automatic_summaries/tadano/", 5, 2)
        elif summarizer == "opizere":
            opizere = Opizere_Summarizer(item, item_path, corpus_reader)
            opizere.create_summary("../resource/automatic_summaries/opizere/", 5, 1)
        elif summarizer == "ganesan":
            ganesan = Ganesan_Summarizer(item, item_path, corpus_reader)
            ganesan.create_summary("../resource/automatic_summaries/ganesan/", 5)
        elif summarizer == "gerani":
            gerani = Gerani_Summarizer(item, item_path, corpus_reader)
            gerani.create_summary("../resource/automatic_summaries/gerani/", 5)
        elif summarizer == "opizera":
            opizera = Opizera_Summarizer(item, item_path, corpus_reader)
            opizera.create_summary("../resource/automatic_summaries/opizera/", 5)

    print "Process finished"