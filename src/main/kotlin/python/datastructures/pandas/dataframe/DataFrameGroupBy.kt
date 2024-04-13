package python.datastructures.pandas.dataframe

import python.datastructures.FieldName
import python.datastructures.PythonDataStructure

data class DataFrameGroupBy(
    private val dataFrame: DataFrame?,
    private val by: MutableList<FieldName?>?,
) : PythonDataStructure {
    override fun clone(): PythonDataStructure = DataFrameGroupBy(dataFrame = dataFrame?.clone() as DataFrame, by = by?.toMutableList())
}
