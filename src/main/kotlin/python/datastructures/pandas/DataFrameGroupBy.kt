package python.datastructures.pandas

import python.datastructures.FieldName
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.dataframe.DataFrame

data class DataFrameGroupBy(
    private val dataFrame: DataFrame,
    private val by: List<FieldName>,
) : PythonDataStructure
