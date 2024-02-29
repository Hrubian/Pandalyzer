package python.datastructures

import python.OperationResult
import python.fail
import python.ok

@JvmInline
value class PythonString(val value: String) : PythonDataStructure {
    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt) {
            PythonString(value + other.value).ok()
        } else {
            fail("Cannot sum $typeName with ${other.typeName}")
        }
    }
}
