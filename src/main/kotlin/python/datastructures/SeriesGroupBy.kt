package python.datastructures


data class SeriesGroupBy(
    private val series: Series,
    private val by: List<FieldName>
) : PythonDataStructure {
    fun mean() {
        //todo check that it is possible to do mean
    }
    fun sum() {}
}