# Pandalyzer

## Introduction

### The goal

These days, programming language Python is gaining lots of popularity among data scientists. One of the reasons is its
simple syntax and relatively shallow learning curve. Over the years, many packages were developed trying to make
data science and data manipulation in python easy and efficient, of which the most well known is a library called
Pandas. However, due to the dynamic nature of Python, it is easy to make mistakes in code, that will not be spotted
before the program is actually run, and it fails in runtime. 

The goal of Pandalyzer is to spot some of these mistakes in a common Pandas code before the execution of the program 
actually starts.

### Used technologies
- Python language
  - ast and ast2json libraries (abstract syntax tree) for parsing the python code and converting it to json format
- Kotlin language for the analysis itself

### References
- [Official Pandas webpage](https://pandas.pydata.org/)
- [ast library docs](https://docs.python.org/3/library/ast.html)
- [ast2json library docs](https://pypi.org/project/ast2json/)
- [Kotlin language docs](https://kotlinlang.org/docs/home.html)


## In-depth description of the project

### Motivation
Consider for example the following program.

```python
import pandas as pd

df = pd.read_csv("data.csv")
df_copy = df
df_copy.drop("column1", inplace=True)

grouped = df.groupby("column1")
# Error - column1 does not exist already

final_score = df["score_a"] + df["score_b_note"]
# Error - summing series of ints with strings

print(df["colunm2"])
# Error - misspelled column name colunm2
```

There are some harder-to-spot mistakes such as referencing a dropped column, summing columns of different types
or a misspelled column name.
All these mistakes are detected at~runtime causing crash of the program.

The Python interpreter does not the structure of the csv files, so it cannot lead us and tell us that something does not
make sense. But usually we know in advance what the data look like. 

### Specification
Our goal will be to develop a tool that is able to take a simple Python code and analyze it, given the CSV file
structures in advance.

It will be a command-line tool that would be able to analyze a single Python file with configuration file.

### Project Scope Limitations

When people write code with pandas, they usually do not modify one dataframe. They create a new dataframe with each 
operation, so the dataframes act as if they were immutable. In my project I will make this an assumption - all datasets
cannot be modified with respect to their column structure after they are created. 

Another challenging topic is conditional execution - if statements and for/while loops. We will support if statements
and will analyze each branch of the if statement separately and then assume that the dataset is in non-deterministic 
state and we will remember all options. However, we will not support while and for loops.

In the initial version (proof of concept) we will assume that the code is not organized - it is just written line by line,
no custom functions and no if statements. Support for functions and if statements will be added later.
