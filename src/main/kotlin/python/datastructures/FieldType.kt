package python.datastructures

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
