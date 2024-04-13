package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

@JvmInline
value class PythonString(val value: String?) : PythonDataStructure {
    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonString) {
            PythonString(value + other.value).ok()
        } else {
            fail("Cannot sum $typeName with ${other.typeName}")
        }
    }

    override fun clone(): PythonDataStructure = PythonString(value)

    override fun boolValue(): Boolean? = value?.isNotEmpty()
}
