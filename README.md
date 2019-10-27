# Opizer

In this project, we implemented the following six methods to generate opinion summaries:
- Hu and Liu method
- Tadano method
- Opizer-E method
- Ganesan method
- Gerani method
- Opizer-A method

For more information about these methods, you can read this paper(https://www.sciencedirect.com/science/article/pii/S0957417417300829).

We used  OpiSums-PT corpus for the experiments.


## Installation
We used Python 2.7.12 to run this project. After clone the repository or download the source code, you must install the prerequisite Python packages listed in the file `requirements.txt`.

With `pip`, this is done with:

    $ pip install -r requirements.txt


## Execution
The general way to run this program is the following:
```
 $ cd src/
 $ python main.py summarizer corpus
```
Where `summarizer` could be:
- "huliu", to use the summarizer.
- "tadano", to use the summarizer.
- "opizere", to use the summarizer.
- "ganesan", to use the summarizer.
- "gerani", to use the summarizer.
- "opizera", to use the summarizer.

and `corpus` could be:
- "buscape", to use the Buscape corpus.
- "reli", to use the ReLi corpus.

## Example
```
 $ python main.py opizera buscape
```

## Outputs
All the generated summaries are located in the folder `resource/automatic_summaries`
