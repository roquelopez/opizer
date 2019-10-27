# Opizer

In this project, we implemented the following six methods to generate extractive and abstractive opinion summaries:
- Hu and Liu method
- Tadano method
- Opizer-E method
- Ganesan method
- Gerani method
- Opizer-A method

For more information about these methods, you can read this [paper](https://www.sciencedirect.com/science/article/pii/S0957417417300829).


## Installation
We used Python 2.7.12 in this project. After you clone the repository or download the source code, you should install the prerequisite Python packages listed in the file `requirements.txt`.

With `pip`, this is done with:

    $ pip install -r requirements.txt


## Execution
The general way to run this program is the following:
```
 $ cd src/
 $ python main.py summarizer corpus
```
Where `summarizer` could be:
- "huliu", to use Hu and Liu summarizer.
- "tadano", to use Tadano summarizer.
- "opizere", to use Opizer-E summarizer.
- "ganesan", to use Ganesan summarizer.
- "gerani", to use Gerani summarizer.
- "opizera", to use Opizer-A summarizer.

and `corpus` could be:
- "buscape", to use Buscape corpus.
- "reli", to use ReLi corpus.

## Example
```
 $ python main.py opizera buscape
```

## Outputs
All the generated summaries are located in the folder `resource/automatic_summaries/`. For instance, the following summary was produced by Opizer-A for the product Samsung Smart TV of the corpus Buscape.
```
Em geral, as opiniões sobre a Samsung Smart TV foram positivas. Contrariamente, com relação ao Preço 
foi avaliado como muito ruim, pois é valor muito alto. Em contraste, em relação ao Design, as pessoas 
adoraram porque é sofisticação e modernidade. Ademais, com relação à Câmera há opiniões controversas 
sobre ela, pois funciona bem e impressiona quem não conhece, mas poderia ter movimentação horizontal 
de visão e não só apenas vertical. Além disso, falando sobre a Qualidade da imagem, foi avaliada como 
excelente porque é ótima. 
```
