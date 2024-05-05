package python.datastructures

import python.datastructures.defaults.PythonBool
import python.datastructures.defaults.PythonInt
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.series.Series

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

    // unknown type
    Unknown;

    fun toPythonDataStructure(): PythonDataStructure = when (this) {
        FloatType -> TODO() //todo implement python float
        IntType -> PythonInt(null)
        BoolType -> PythonBool(null)
        TimeDelta -> TODO()
        DateTime -> TODO()
        TZDateTime -> TODO()
        Category -> TODO()
        TimeSpan -> TODO()
        Sparse -> TODO()
        Intervals -> TODO()
        NullInt -> NondeterministicDataStructure(PythonInt(null), PythonNone)
        NullFloat -> TODO()
        StringType -> PythonString(null)
        NABoolean -> TODO()
        Unknown -> TODO()
    }

    companion object {
        fun fromPythonDataStructure(struct: PythonDataStructure): FieldType? = when (struct) {
            is PythonInt -> IntType
            is PythonString -> StringType
            is PythonBool -> BoolType
            is Series -> struct.type
            else -> null
        }
    }
}
