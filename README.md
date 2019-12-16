# milvus_test


我用docker安装。

https://www.milvus.io/docs/en/userguide/install_milvus/

## 第一部分 安装环境和服务

### 1. 申请阿里云机器
 
请安装官网要求配置。GPU、8G内存、SATA 3.0 SSD、Ubuntu LTS 18.04

请记住IP、登录密码、远程登录密码（6位数）

创建一个用户，不要用root。

### 2. 安装GPU、CUDA等

NVIDIA driver 418 or higher。

阿里云的机器，可以在装机的时候同步安装
```
Driver-418.67 installing
CUDA-10.1.168 installing
```

### 3. 更新docker
目前docker版本是18，需要升级到19.03 

#### 3.1 删除旧的docker
```
sudo apt-get remove docker docker-engine docker.io containerd runc
```

#### 3.2 安装新的docker
```
sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common


curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -


sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"


sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd
```

### 4. 安装NVIDIA docker
```
distribution=$(. /etc/os-release;echo $ID$VERSION_ID)


curl -s -L https://nvidia.github.io/nvidia-docker/gpgkey | sudo apt-key add -


curl -s -L https://nvidia.github.io/nvidia-docker/$distribution/nvidia-docker.list | sudo tee /etc/apt/sources.list.d/nvidia-docker.list


sudo apt-get update && sudo apt-get install -y nvidia-container-toolkit


sudo systemctl restart doc
```

### 5. 测试一下docker

 
```
docker --version
docker run --gpus all nvidia/cuda:9.0-base nvidia-smi
```

```
docker info
```

```bash
Client:
 Debug Mode: false

Server:
 Containers: 1
  Running: 0
  Paused: 0
  Stopped: 1
 Images: 1
 Server Version: 19.03.5
 ……
```

### 6. 安装Milvus
 

#### 6.1 准备目录

 
```
mkdir /home/$USER/milvus
cd /home/$USER/milvus
mkdir conf
cd conf
wget https://raw.githubusercontent.com/milvus-io/docs/master/assets/server_config.yaml
wget https://raw.githubusercontent.com/milvus-io/docs/master/assets/log_config.conf
#后面这两个是配置文件，如果下载失败，可以手动下载，再拷贝到服务器的目录

#配置文件，在本仓库的conf目录拷贝了一份
```

#### 6.2 下载镜像

```
# GPU
docker pull milvusdb/milvus:latest
```

#### 6.3 启动服务

官方的命令过时了，用这个

```
docker run -td --gpus all -e "TZ=Asia/Shanghai" -p 19530:19530 -p 8080:8080 -v /home/xuehp/milvus/db:/opt/milvus/db -v /home/xuehp/milvus/conf:/opt/milvus/conf -v /home/xuehp/milvus/logs:/opt/milvus/logs milvusdb/milvus:latest

(官方给的nvidia-docker命令，可把我折腾惨了。新版docker更新了参数)
 ```

### 7. 开放端口

如果有防火墙，或者用阿里云的机器，记得开放端口19530

## 第二部分 准备数据和测试

使用网上下载的数据，见data目录。

如有侵犯，请告知，多谢~

data目录的文件说明：
- content_file.txt 是原始文件。
- p1_get_title.py	抽取title字段，形成文件title.txt
- p2_seg_title.py 将title分词，形成title_seg.txt
- p3_train_title.py 训练词向量，形成word2vec.txt 可用gensim训练得到。
- p4_vec_title.py 计算title的向量，形成title_vec.txt
- p5_index_title.py 将title的语义向量写入向量搜索引擎
- p6_search_title.py 执行查询
- ids.txt是向量id到句子的映射文件，随项目临时生成。
- example.py 是官方给的例子



- java部分示例了insert
- python部分示例了index、query和解析结果

## 第三部分 ElasticSearch和Milvus搜索结果对比
为了对比两种搜索架构的效果，我做了一组对比试验。

数据：都是上述语料的title。
Elastic部分的操作，参考另外一个代码。

### 例子1 "学习好辛苦哦"
```
/Users/huihui/anaconda3/bin/python /Users/huihui/git/xuehuiping2/milvus_test/py/p6_search_title.py
学习好辛苦哦
Server connected.
求支招 1576482323979752037 6.521623276967148e-07
被妈妈打击 1576482323979752146 7.984817784745246e-07
支持不婚主义！ 1576482323979752306 8.597331770943129e-07
放弃转专业求夸 1576482323979752074 9.292808158534172e-07
永远没有学习方法 1576482323979752231 9.654111181589542e-07
yp从来没成功过 1576482323979752358 1.0979392754961737e-06
周末野餐自制+摆盘求夸 1576482323979752044 1.2113277989556082e-06
讨厌自己 1576482323979752422 1.3594934671345982e-06
一个多小时 1576482323979752269 1.5448420072061708e-06
啊啊啊总觉得好担心 1576482323979752103 1.6662115740473382e-06


Process finished with exit code 0
```


```
查询串：学习好辛苦哦

结果：
11.181026 决定这周起好好学习
10.14626 大家都好好哦
8.797304 辛苦工作回来，想和陌生人聊天的冲动忍住了，求表扬
8.231066 这学期开学到现在真的好努力呢，每天都能坚持用高效率学习，觉得自己好棒啊
7.987249 永远没有学习方法
7.7054224 不断学习画画，求表扬！
7.7054224 在家学习两天，快吐了！
7.4428086 雅思继续学习中求表扬
6.9678526 中午午休我还在学习 求夸夸
6.549879 决定学习到今天晚上九点，求表扬
```

### 例子2 ”科研好累，能不能换专业？求导师理解！！“

```
/Users/huihui/anaconda3/bin/python /Users/huihui/git/xuehuiping2/milvus_test/py/p6_search_title.py
科研好累，能不能换专业？求导师理解！！
Server connected.
#第一列为title，第二列为句子id，第三列为得分
熬过了第三次化疗，求表扬！ 1576482323979752009 9.702345096229692e-07
终于鼓起勇气分手，求表扬！！ 1576482323979752270 1.2134030384913785e-06
当伴郎去新娘家拱门手指被夹出血泡，求表扬！ 1576482323979752028 1.450270588065905e-06
我一个新人，被人排挤真的好难过啊 1576482323979752012 1.6236588180618128e-06
确诊重抑的第十一个月，求夸！ 1576482323979752127 1.641212975300732e-06
在是否要在职考研的路上徘徊不定，求支招 1576482323979752030 1.781353603291791e-06
三天没抽烟了，表扬我😎 1576482323979752343 1.8296789221494691e-06
第一次煮糖水，求表扬！ 1576482323979752278 2.0037011836393503e-06
丧的想去死了啊求安慰 1576482323979752431 2.073961695714388e-06
和男盆友分手了，求表扬 1576482323979752481 2.1450407530210214e-06


Process finished with exit code 0
```

```
第一列为得分，第二列为title
10.966908 二战学科教学英语，很可能没学上
10.502903 终于跟爸妈摊牌考研专业是考古了！求表扬！
10.47923 考研复试联系的老师特别好
10.439194 好累
10.199052 放弃转专业求夸
9.994813 作为一名情趣用品测评师，能被表扬吗
9.595035 认为领导决定弊大于利，怼了一下，还是没能改变结果。这种傻瓜式说话能夸夸吗？
8.649442 颓废:大三中文专业
8.623367 虽然马上要复试了，可是学不进去又不敢联系导师怎么办
8.447514 坚持背单词171天，希望努力能有好结果
```


### 例子3 ”回归20年来，“一国两制”的成功实践，让澳门发生了翻天覆地的变化。“

```
回归20年来，“一国两制”的成功实践，让澳门发生了翻天覆地的变化。
Server connected.
删掉了喜欢的人的微信，求表扬 1576482323979752091 0.0000014174
昨晚，11点半就早早的睡美容觉了，求表扬 1576482323979752181 0.0000015529
我今天知道了伤害自己爱的人是不对的 1576482323979752210 0.0000018259
今天去了看电影比悲伤更悲伤的故事，却一直在笑 1576482323979752359 0.0000019426
我终于鼓起勇气去调解爸妈之间的矛盾了，求表扬 1576482323979752483 0.0000019440
我已经离婚了，一直放不下。两岁多的儿子抚养权归他。 1576482323979752391 0.0000023038
低于50的私聊我，，我只能活到31岁啦…… 1576482323979752188 0.0000023801
大家都有夸夸群吗？我都没有，难过，求夸～ 1576482323979752043 0.0000023971
我今天去打肉毒素了，求表扬，嘻嘻 1576482323979752036 0.0000026752
突然觉得自己太懂事，感觉亏了一个亿，呜呜，求表扬 1576482323979752216 0.0000028988

```
```

回归20年来，“一国两制”的成功实践，让澳门发生了翻天覆地的变化。

15.566722 拉黑了表白4年没成功的男人
14.70176 yp从来没成功过
11.860057 地铁保护了两个女生求表扬
11.393671 真的好累  快要坚持不下去了  求夸我  让我回回血
11.3071165 间歇性发丧，持续性奔跑🏃🏻♀️，第一次完成了制定的运动目标！求表扬！
11.239575 辛苦工作回来，想和陌生人聊天的冲动忍住了，求表扬
11.08926 大家来说自己的一个感觉不满的地方。我来夸你！
10.741294 我已经离婚了，一直放不下。两岁多的儿子抚养权归他。
9.9555235 悲伤的一天，求朋友们回血
9.7804985 老板说看到我的实验结果愁得两天没睡好
```

### 例子4 ”怎样才能发表论文？！“
```

怎样才能发表论文？！

求夸！ 1576482323979752488 0.0000003139
今天早起啦 1576482323979752248 0.0000005281
今天休假去加班 1576482323979752092 0.0000006213
困死了 1576482323979752241 0.0000008259
不知道第几次分手 1576482323979752354 0.0000008694
大二英专女生 周末两天躺寝室 求夸 1576482323979752330 0.0000009717
雅思继续学习中求表扬 1576482323979752105 0.0000009762
就想知道 1576482323979752323 0.0000011092
玩手机不睡觉 1576482323979752345 0.0000012066
哭辽…… 1576482323979752486 0.0000012445
```
```

怎样才能发表论文？！

9.441331 怎么样给敏感的男孩信心啊
9.05562 早上将近十一点才起来，能被夸吗?
8.859569 一周完成了毕业论文初稿，求表扬
8.859569 写毕业论文收问卷到哭泣…… 求表扬
8.601582 熬夜写其实还没到deadline的论文，求表扬
8.586679 写论文 查了很久都没查到合适的文献
8.379265 刚写完申论大作文，求夸😭
8.111908 今天论文写了300字摘要 求夸
7.1847486 裤子穿成这样，求表扬！
6.936653 稿件怎么写都不满意，怀疑自己的能力
```