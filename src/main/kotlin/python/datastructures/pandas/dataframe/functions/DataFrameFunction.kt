package python.datastructures.pandas.dataframe.functions

import python.datastructures.PythonDataStructure
import python.datastructures.pandas.dataframe.DataFrame

interface DataFrameFunction : PythonDataStructure {
    val dataFrame: DataFrame
}
