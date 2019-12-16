# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 2:21 下午 

import jieba

lines = open('../data/title.txt').readlines()

f = open('../data/title_seg.txt', 'w')

for line in lines:
    r = ' '.join(jieba.cut_for_search(line))
    print(r)
    f.write(r)

f.close()
