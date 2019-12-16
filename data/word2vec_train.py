# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 9:06 上午 
from gensim.models import word2vec

file3 = 'fenci_result.txt'
model = word2vec.Word2Vec(corpus_file=file3, min_count=1)
model.wv.save_word2vec_format("word2vec.txt")
