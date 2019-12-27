# CN-WikiCatReader: Fine-grained Relation Miner from Chinese Wikipedia Categories

### By Chengyu Wang (https://chywang.github.io)

**Introduction:** This software extracts various types of fine-grained semantic relations from the Chinese Wikipedia category system, serving as a "deep reader" for Chinese short texts. It employs a rule-based extractor, a word embedding based projection learner, a collective inference step and hypernym expansion techniques to extract hypernymy relations from Chinese Wikipedia categories. We further design several fully unsupervised, data-driven algorithm to identify non-taxonomic relations (i.e., relations other than hypernymy) from these category names.

**Papers** 
1. Wang et al. Learning Fine-grained Relations from Chinese User Generated Categories. EMNLP 2017
2. Wang et al. Decoding Chinese User Generated Categories for Fine-grained Knowledge Harvesting. TKDE (2019) (extended version)
3. Wang et al. Open Relation Extraction for Chinese Noun Phrases. TKDE (accepted)


**APIs**

#### Part I: Hypernymy Relation Extraction

Please run the programs according to the following order.

+ RuleBasedIsAGenerator (in the isa package)

Required Inputs:

1. blacklist.txt: The list of thematic words in Chinese (provided in this project).

2. whitelist: The list of conceptual words in Chinese (provided in this project).

3. cat.txt: Entity and category names in Chinese Wikipedia.

> NOTE: We provide the names of version 20170120, which is rather old. Readers are suggested to replace it with the up-to-date version.

+ ProjectionModelTrainer (in the isa package)

Required Inputs:

1. positive.txt and negative.txt: Automatically generated training sets. Refer to our paper for details.

2. The Word2Vec model: Due to the large size of neural language models, we do not provide the model here. Please use your own neural language model instead and replace the values of the parameters "dimension" (the dimensionality of word embeddings) and "w2vModel" (the path of the model), if you would like to try the algorithm over your datasets.

> NOTE: The inputs of outputs of previous programs are omitted here.

+ ProjectionBasedIsAGenerator (in the isa package)

+ ProjectionBasedIsARelationExtractor (in the isa package)

+ HypernymExpander (in the isa package)

Final Output:

1. total-isa-expand.txt: The extracted is-a relations from Chinese Wikipedia categories.


#### Part II: Pattern-based Non-hypernymy Relation Extraction

Please run the programs according to the following order.

+ WikiDicGenerator (in the nontaxonomic package)

Required Input:

1. cat.txt: Entity and category names in Chinese Wikipedia.

It generates dictionaries of Chinese Wikipedia entities.

+ RelationPatternMiner (in the nontaxonomic package)

It extracts frequent category patterns from Chinese Wikipedia categories.

+ RelationPatternConfCalculator (in the nontaxonomic package)

It computes confidence scores for frequent category patterns.

+ VerbBasedFilter (in the nontaxonomic package)

It selects confident category patterns based on threshold filering and POS rules.

+ VerbBasedRelationExtractor (in the nontaxonomic package)

It extracts non-hypernymy relations from selected patterns.

Final Outputs:

1. verb-relations.txt

2. verb-relations-infer.txt

#### Part III: Data-driven Non-hypernymy Relation Extraction

Please run the programs according to the following order.

+ WikiSentenceExtractor (in the wiki package)

Extract all the sentences from the Chinese Wikipedia data dump (xml format).

+ Indexer and Searcher (in the lucene package)

Build sentence-level inverted index using Apache Lucene.

>NOTE: Due to the large size of the texts and index, we do not provide the data here. Users can download the newest Wikipedia data dumps to build the index using our code.

**Dependencies**

1. This software is run in the JaveSE-1.8 environment. With a large probability, it runs properly in other versions of JaveSE as well. However, there is no guarantee.

2. It requires the FudanNLP toolkit for Chinese NLP analysis (https://github.com/FudanNLP/fnlp/), and the JAMA library for matrix computation (https://math.nist.gov/javanumerics/jama/). We use Jama-1.0.3.jar in this project.

3. Please refer to the JAVA implementation of the Word2Vec model here: https://github.com/NLPchina/Word2VEC_java.


**More Notes on the Algorithm** 

1. This is an updated version of the algorithm proposed in our paper. We make slight changes and add more heuristics to extract more is-a relations. In total, we extract 1.94M is-a relation triples, instead of 1.12M, as reported in the paper.

2. We are developing better algorithms for non-taxonomic relations from Chinese Wikipedia categories. The codes will be added to this project continuously.

**Citations**

If you find this software useful for your research, please cite the following papers.

> @inproceedings{emnlp2017a,<br/>
&emsp;&emsp; author    = {Chengyu Wang and Yan Fan and Xiaofeng He and Aoying Zhou},<br/>
&emsp;&emsp; title     = {Learning Fine-grained Relations from Chinese User Generated Categories},<br/>
&emsp;&emsp; booktitle = {Proceedings of the 2017 Conference on Empirical Methods in Natural Language Processing},<br/>
&emsp;&emsp; pages     = {2567–2577},<br/>
&emsp;&emsp; year      = {2017}<br/>
}

> @article{tkde2018,<br/>
&emsp;&emsp; author    = {Chengyu Wang and Yan Fan and Xiaofeng He and Aoying Zhou},<br/>
&emsp;&emsp; title     = {Decoding Chinese User Generated Categories for Fine-grained Knowledge Harvesting},<br/>
&emsp;&emsp; journal   = {IEEE Transactions on Knowledge and Data Engineering},<br/>
&emsp;&emsp; volume    = {31},<br/>
&emsp;&emsp; number    = {8},<br/>
&emsp;&emsp; pages    = {1491–1505},<br/>
&emsp;&emsp; year      = {2018}<br/>
}

> @article{tkde2019,<br/>
&emsp;&emsp; author    = {Chengyu Wang and Xiaofeng He and Aoying Zhou},<br/>
&emsp;&emsp; title     = {Open Relation Extraction for Chinese Noun Phrases},<br/>
&emsp;&emsp; journal   = {IEEE Transactions on Knowledge and Data Engineering},<br/>
&emsp;&emsp; doi     = {10.1109/TKDE.2019.2953839},<br/>
&emsp;&emsp; year      = {2019}<br/>
}

More research works can be found here: https://chywang.github.io.


