package python.datastructures.pandas.series

import python.datastructures.FieldName
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.series.Series

data class SeriesGroupBy(
    private val series: Series,
    private val by: List<FieldName>,
) : PythonDataStructure {
    fun mean() {
        // todo check that it is possible to do mean
    }

    fun sum() {}
}
