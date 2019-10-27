# -*- coding: utf-8 -*-
'''
Created on 13/05/2015

@author: Roque Lopez
'''
from __future__ import unicode_literals
from bs4 import BeautifulSoup
import httplib, urllib
import re
import os
import codecs
import time 

def get_dizer_text(text):
    ''' Return a text with its RST annotations using parser Dizer '''
    parameters = {'metodo':"greedy", 'numberTrees':"1", 'compact':"off", 's':"unchecked", 'cod':"Português-DiZer-Padrões tradicionais do DiZer", 'texto': text}

    for k, v in parameters.iteritems():
        parameters[k] = unicode(v).encode('iso-8859-1')

    html = urllib.urlopen("https://10.11.14.181/dizer2/step3.php", urllib.urlencode(parameters))
    soup = BeautifulSoup(html.read())
    id_file = soup.input['value']
    url_file = "https://10.11.14.181/dizer2/bin/segments/segments_clear_%s.txt" % id_file
    text = urllib.urlopen(url_file).read()
    print id_file
    time.sleep(100)

    return text.decode('iso-8859-1').encode('utf8')

if __name__ == '__main__':
    print "Starting..."

    input_path = "/home/roque/reviews_reli/"
    output_path = "../resource/dizer"

    for folder in sorted(os.listdir(input_path)):
        folder_input__path = os.path.join(input_path, folder)
        folder_output__path = os.path.join(output_path, folder)
        if not os.path.exists(folder_output__path):
            os.makedirs(folder_output__path)
        for file_name in sorted(os.listdir(folder_input__path)):
            print folder, file_name
            fin = codecs.open(os.path.join(folder_input__path, file_name), 'r', 'utf-8')
            text = fin.read()
            fin.close()
            new_text = get_dizer_text(text)
            with codecs.open(os.path.join(folder_output__path, file_name), 'w') as fout:
                fout.write(new_text)

        time.sleep(600)

    print "Finished"