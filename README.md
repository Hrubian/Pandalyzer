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

### Milestones plan
| Date       | Milestone        |
|------------|------------------|
| 2023-12-04 | Proof of concept |



## In-depth description of the project

### Motivation

Consider the following list of friends with some basic info about them in a csv format:
```csv
id,nickname,fullname,height,city
1,user123,John Doe,175,Prague
2,coolgirl23,Alice Johnson,160,Brno
3,friend99,Bob Smith,182,Old Town
4,hikinglover,Susan Williams,165,Old Town
5,bookworm88,Michael Brown,178,Bratislava
6,anotheruser,Adam Green,155,Prague
```

and another csv file telling us information about some cities:
```csv
name,distance_from_prague,established
Prague,0,03/03/1723
Brno,50,05/02/1834
Old Town,5,09/02/2000
Bratislava,80,12/12/1940
New York,600,03/11/1820
```

Suppose that I want to visit one of our friend in his city. But I really do not want to go too far away. So I would like
the city to be not further than 30 km from Prague (where I live). Also I really do not feel like talking to somebody
taller than me, so the friend should probably have less than 180 cm). I really like old cities and so If we would be able
to connect it with visiting a city that is more than 150 years old, that would be really nice. All this is very well
doable using Pandas library. The code would look something like this:
```python
import pandas as pd

friends_df = pd.read_csv("friends.csv")
cities_df = pd.read_csv("cities.csv")

TODO

```

Notice that the code is a bit error-prone. TODO

The Python interpreter does not the structure of the csv files, so it cannot lead us and tell us that something does not
make sense. But usually we know in advance what the data look like. TODO

### Project Scope Limitations

When people write code with pandas, they usually do not modify one dataframe. They create a new dataframe with each 
operation, so the dataframes act as if they were immutable. In my project I will make this an assumption - all datasets
cannot be modified with respect to their column structure after they are created. 

Another challenging topic is conditional execution - if statements and for/while loops. We will support if statements
and will analyze each branch of the if statement separately and then assume that the dataset is in non-deterministic 
state and we will remember all options. However, we will not support loops. TODO explanation

In the initial version (proof of concept) we will assume that the code is not organized - it is just written line by line,
no custom functions and no if statements. Support for functions and if statements will be added later.

### How it will work

We will go through the code sequentially and track column structure of all currently existing datasets and report any
case of illegal operation.
In case of if statement, we will remember all possible outcomes and if any new operation will be done on the resulting 
datasets, all possibilities will be tried. This could generally make the algorithm highly inefficient, but that is not
the case in most pandas codes.