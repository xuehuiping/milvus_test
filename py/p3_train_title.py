# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 9:06 上午 
from gensim.models import word2vec

file3 = '../data/title_seg.txt'
model = word2vec.Word2Vec(corpus_file=file3, min_count=1)
model.wv.save_word2vec_format("../data/word2vec.txt")
