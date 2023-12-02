package analyzer


data class AnalysisContext(
    val dataFrames: Map<String, DataFrame>,
    val returnValue: Any?
) {
    companion object {
        fun createEmpty(): AnalysisContext = AnalysisContext(
            dataFrames = emptyMap(),
            returnValue = null
        )
    }
}

data class DataFrame(
    val fields: Map<String, DataFrameFields>
)

enum class DataFrameFields {
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