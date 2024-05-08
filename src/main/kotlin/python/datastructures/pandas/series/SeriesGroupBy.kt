package python.datastructures.pandas.series

import python.datastructures.FieldName
import python.datastructures.PythonDataStructure

// TODO: implement SeriesGroupBy
data class SeriesGroupBy(
    private val series: Series,
    private val by: List<FieldName>,
) : PythonDataStructure {
    override fun clone(): PythonDataStructure = SeriesGroupBy(series.clone() as Series, by)
}
