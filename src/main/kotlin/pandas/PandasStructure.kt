package pandas

typealias FieldName = String

enum class FieldType {
    // numpy types
    FloatType,
    IntType,
    BoolType,
    TimeDelta,
    DateTime,

    // pandas extensions
    TZDateTime,
    Category,
    TimeSpan,
    Sparse,
    Intervals,
    NullInt,
    NullFloat,
    StringType,
    NABoolean,
}

sealed interface PandasStructure

data class DataFrame(
    private val fields: Map<FieldName, FieldType>
) : PandasStructure {

    fun renameCols(columnMapping: Map<FieldName, FieldName>): OperationResult<DataFrame> = DataFrame(
        fields = fields.mapKeys { columnMapping[it.key] ?: it.key } //todo announce missing key
    ).ok()

    fun filterCols(selectedCols: Set<FieldName>): DataFrame = DataFrame( //todo maybe a list? ordered?
        fields = fields.filterKeys { it in selectedCols } //todo announce missing key
    )

    fun filterCol(colName: FieldName): Series = fields[colName]?.let {
        Series(name = colName, type = it)
    }!! //todo announce missing key

    fun concatWith(df: DataFrame): DataFrame =
        DataFrame(
            fields = fields
        ) //todo check tha structure is the same

    fun filterByBoolDataFrame(boolDataFrame: DataFrame): OperationResult<DataFrame> = fail("Just a test")

    fun groupBy(selectedCols: Set<FieldName>) = DataFrameGroupBy(
        dataFrame = this,
        by = selectedCols.toList(),
    ) //todo check missing key


    companion object {
        fun merge(left: DataFrame, right: DataFrame, ): DataFrame {
            TODO()
        }
    }
}

data class DataFrameGroupBy(
    private val dataFrame: DataFrame,
    private val by: List<FieldName>
) : PandasStructure

data class Series(
    private val name: FieldName,
    private val type: FieldType
) : PandasStructure

data class SeriesGroupBy(
    private val series: Series,
    private val by: List<FieldName>
) : PandasStructure {
    fun mean() {
        //todo check that it is possible to do mean
    }
    fun sum() {}
}