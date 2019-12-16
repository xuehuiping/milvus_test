# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 3:06 下午 

import numpy as np

'''
计算title的语义
'''

_DIM = 100


def loadMapWordVec():
    '''
    加载term到向量的映射关系，这个文件在训练词向量的时候生成
    :return:
    '''
    lines = open('../data/word2vec.txt').readlines()
    mapWordVec = {}
    for line in lines:
        sets = line.strip().split(' ')
        values = []
        for v in sets[1:]:
            values.append(float(v))
        mapWordVec[sets[0]] = values
    return mapWordVec


def my_sum(vec1, vec2):
    r = np.zeros(len(vec1))
    index = 0
    for v1, v2 in zip(vec1, vec2):
        r[index] = v1 + v2
        index += 1
    return r


def getSentVector(terms, mapWordVec):
    '''
    计算句子的语义向量
    :param terms: 句子的terms
    :param mapWordVec:
    :return: 句子的向量表示
    '''
    vectors = np.zeros(_DIM)
    for term in terms:
        if term not in mapWordVec.keys():
            continue
        word_vec = mapWordVec[term]
        word_vec = np.array(word_vec)
        vectors = my_sum(vectors, word_vec)
    vectors = 1.0 * vectors / _DIM
    return vectors


def to_str(vector):
    r = ''
    for v in vector:
        r += str(v) + ' '
    return r.strip()


if __name__ == "__main__":
    mapWordVec = loadMapWordVec()
    titles = open('../data/title_seg.txt').readlines()
    f = open('../data/title_vec.txt', 'w')

    for title in titles:
        vector = getSentVector(title.strip().split(' '), mapWordVec)
        f.write(to_str(vector) + '\n')
    f.close()
