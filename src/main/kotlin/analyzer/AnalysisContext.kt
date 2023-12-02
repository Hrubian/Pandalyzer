package analyzer


data class AnalysisContext(
    val datasets: Map<String, Dataset>,
    val returnValue: Any?
) {
    companion object {
        fun createEmpty(): AnalysisContext = AnalysisContext(
            datasets = emptyMap(),
            returnValue = null
        )
    }
}

data class Dataset(
    val fields: Map<String, DatasetField>
)

enum class DatasetField {
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