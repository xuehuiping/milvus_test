# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 2:26 下午

import sys
import time
import numpy as np
from milvus import Milvus, IndexType

table_name = 'kuakua_table'
_HOST = '182.92.233.254'
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


def index(milvus, vectors):
    status, ok = milvus.has_table(table_name)
    if ok:
        milvus.drop_table(table_name)
        time.sleep(2)
    param = {
        'table_name': table_name,
        'dimension': _DIM
    }
    milvus.create_table(param)

    status, ids = milvus.insert(table_name=table_name, records=vectors)
    print(status)
    # print(ids)
    with open('../data/ids.txt', 'w') as f:
        for s in ids:
            f.write(str(s) + "\n")
    time.sleep(6)
    index_param = {
        'index_type': IndexType.IVFLAT,  # choice ivflat index
        'nlist': 2048
    }
    status = milvus.create_index(table_name, index_param)
    print(status)


def load_vectors():
    lines = open('../data/title_vec.txt').readlines()
    vectors = []
    for line in lines:
        line = line.strip()
        sets = line.split(' ')
        if len(sets) < _DIM:
            continue
        values = []
        for v in sets:
            values.append(float(v))
        vectors.append(values)
    return vectors


if __name__ == '__main__':
    vectors = load_vectors()
    milvus = get_milvus()
    index(milvus, vectors)
