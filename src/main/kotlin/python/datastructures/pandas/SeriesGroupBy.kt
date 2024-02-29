package python.datastructures.pandas

import python.datastructures.FieldName
import python.datastructures.PythonDataStructure

data class SeriesGroupBy(
    private val series: Series,
    private val by: List<FieldName>,
) : PythonDataStructure {
    fun mean() {
        // todo check that it is possible to do mean
    }

    fun sum() {}
}
