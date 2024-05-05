package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.series.Series
import python.fail
import python.ok
import python.withWarn

@JvmInline
value class PythonString(val value: String?) : PythonDataStructure {
    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonString -> PythonString(value + other.value).ok()
            is Series -> other.plus(this)
            else -> fail("Cannot sum $typeName with ${other.typeName}")
        }
    }

    override fun equal(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonString) {
            if (value == null || other.value == null) {
                PythonBool(null).withWarn("Unable to check unknown strings on equality")
            } else {
                PythonBool(value == other.value).ok()
            }
        } else {
            fail("Cannot sum $typeName with ${other.typeName}")
        }
    }

    override fun clone(): PythonDataStructure = PythonString(value)

    override fun boolValue(): Boolean? = value?.isNotEmpty()
}
