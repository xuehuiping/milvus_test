# -*- coding: utf-8 -*-
# author: huihui
# date: 2019/12/16 2:17 下午 

'''
抽取原始语料的title字段，保存到文件title.txt
'''
import json

file_name = "../data/content_file.txt"
json_data = json.load(open(file_name))
print(len(json_data))

f = open('../data/title.txt', 'w')

for doc in json_data:
    title = doc['title']
    f.write(title + "\n")

f.close()
