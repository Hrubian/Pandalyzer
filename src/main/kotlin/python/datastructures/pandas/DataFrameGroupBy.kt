package python.datastructures.pandas

import python.datastructures.PythonDataStructure

data class DataFrameGroupBy(
    private val dataFrame: DataFrame,
    private val by: List<FieldName>
) : PythonDataStructure {

}
