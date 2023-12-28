package python.datastructures

data class DataFrameGroupBy(
    private val dataFrame: DataFrame,
    private val by: List<FieldName>
) : PythonDataStructure {

}
