# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 11:05 上午
import jieba
import sys
from milvus import Milvus


def loadMapVecIds():
    '''
    加载向量id到向量的映射关系，这个文件在insert、index的时候生成
    :return:
    '''
    lines = open('data/mapVectorId2Term.txt').readlines()
    mapVecIds = {}
    for line in lines:
        sets = line.strip().split('\t')
        mapVecIds[sets[0]] = sets[1]
    return mapVecIds


def loadMapWordVec():
    '''
    加载term到向量的映射关系，这个文件在训练词向量的时候生成
    :return:
    '''
    lines = open('data/word2vec.txt').readlines()
    mapWordVec = {}
    for line in lines:
        sets = line.strip().split(' ')
        values = []
        for v in sets[1:]:
            values.append(float(v))
        mapWordVec[sets[0]] = values
    return mapWordVec


def getSentVector(sent, mapWordVec):
    '''
    计算句子的语义向量
    :param sent:
    :param mapWordVec:
    :return:
    '''
    terms = jieba.cut(sent)
    vectors = []
    index = 0
    for term in terms:
        word_vec = mapWordVec[term]
        vectors.insert(index, word_vec)
        index += 1
    return vectors


def getMilvus():
    _HOST = '182.92.233.254'
    _PORT = '19530'
    _DIM = 100
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


if __name__ == "__main__":
    query = '学习好辛苦哦'
    table_name = 'kuakua_table'
    milvus = getMilvus()

    mapWordVec = loadMapWordVec()
    query_vectors = getSentVector(query, mapWordVec)
    print(query_vectors)
    results = queryVec(query_vectors, milvus, table_name)
    mapVecIds = loadMapVecIds()
    for sent_vec in results:
        for word_vec in sent_vec:
            _id = word_vec.id
            distance = word_vec.distance
            word = mapVecIds[str(_id)]
            print("{} {} {}".format(word, _id, distance))
        print()
