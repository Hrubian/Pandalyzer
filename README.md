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
  - ast and library (abstract syntax tree) for parsing the python code
- Kotlin language for the analysis itself

### References
- [Official Pandas webpage](https://pandas.pydata.org/)
- [ast library docs](https://docs.python.org/3/library/ast.html)
- [Kotlin language docs](https://kotlinlang.org/docs/home.html)


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

The Python interpreter does know not the structure of the csv files, so it cannot lead us and tell us that something 
does not make sense. But usually we know in advance what the data look like. 

The tool uses Abstract Interpretation method for the analysis.
Detailed information regarding the implementation can be found in my bachelor-thesis repository in the third, fourth 
and fifth chapter:
(https://github.com/Hrubian/bachelor-thesis)

### An example config file
```
[file.csv]
col1 = "int"
col2 = "string"
col3 = "string"

[file2.csv]
int_col = "int"
str_col = "string"
bool_col = "bool"
```

## Building from source

To build the Pandalyzer from sources, follow the steps below:

1. Ensure that you have Java (version 21.0.1 or higher), Git and Python 3.x installed.
2.  Clone the Pandalyzer repository:
```
git clone https://github.com/Hrubian/Pandalyzer.git
```
3. Navigate to the root folder of the repository:
```
cd Pandalyzer
```
4. Run the Gradle bootstrap script:
```
./gradlew build (or ./gradlew.bat build on Windows)
```

## Running the tool

The build generates a ./build
Check that there are also ./build/distributions/Pandalyzer.tar ./build/distributions/Pandalyzer.zip archives.
Unpack one of them (depending on what tools you are provided with) and run the Pandalyzer (or
Pandalyzer.bat) script in the bin folder.
The program accepts the following command-line arguments:
- -h, --help - Prints usage information and exits
- -i, --input - The input python script to analyze **(mandatory)**
- -o, --output - The output file to store the analysis result to (standard output by default)
- -c, --config - The configuration file to read the file structures from (config.toml by default)
- -f, --format - The format of the analysis output, possible options: hr (human-readable), json (hr by default), csv

# Case studies
There is a folder *case_studies* containing various examples. 
You can use these examples when trying to run the Pandalyzer.
each directory contains script.py and config.toml that can be set as --input and --config command-line arguments.
The behavior of these case studies is explained in the fifth chapter of 
[my bachelor thesis](https://github.com/Hrubian/bachelor-thesis).
