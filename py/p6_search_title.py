# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 2:47 下午 

import sys
import jieba
import numpy as np
from milvus import Milvus, IndexType
from my_config import _HOST

table_name = 'kuakua_table'
_PORT = '19530'
_DIM = 100


def get_milvus():
    milvus = Milvus()
    param = {'host': _HOST, 'port': _PORT}
    status = milvus.connect(**param)
    if status.OK():
        print("Server connected.")
    else:
        print("Server connect fail.")
        sys.exit(1)
    return milvus


def queryVec(query_vectors, milvus, table_name):
    '''
    查询向量
    :param query_vectors:
    :return:
    '''

    param = {
        'table_name': table_name,
        'query_records': query_vectors,
        'top_k': 10,
        'nprobe': 16
    }
    status, results = milvus.search_vectors(**param)
    return results


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
    vectors.fill(0.0)
    for term in terms:
        if term not in mapWordVec.keys():
            continue
        word_vec = mapWordVec[term]
        word_vec = np.array(word_vec)
        vectors = my_sum(vectors, word_vec)
    vectors = 1.0 * vectors / _DIM
    vectors = [float(v) for v in vectors]
    return vectors


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


if __name__ == "__main__":
    query = '学习好辛苦哦'
    query = '科研好累，能不能换专业？求导师理解！！'
    query = '怎样才能发表论文？！'
    query = '回归20年来，“一国两制”的成功实践，让澳门发生了翻天覆地的变化。'
    print(query)
    table_name = 'kuakua_table'
    milvus = get_milvus()
    mapWordVec = loadMapWordVec()
    terms = jieba.cut(query)
    sent_vectors = getSentVector(' '.join(terms).split(' '), mapWordVec)

    # 参数需要是2维的，这里只有一句话
    query_vectors = [sent_vectors]
    results = queryVec(query_vectors, milvus, table_name)
    ids = open('../data/ids.txt').readlines()
    ids = [i.strip() for i in ids]
    titles = open('../data/title.txt').readlines()
    for sent_vec in results:
        for word_vec in sent_vec:
            _id = word_vec.id
            distance = word_vec.distance
            row = ids.index(str(_id))
            title = titles[row].strip()
            print("{} {} {:.10f}".format(title, _id, distance))
        print()
