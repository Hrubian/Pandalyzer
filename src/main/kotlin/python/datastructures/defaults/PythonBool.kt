package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

@JvmInline
value class PythonBool(val value: Boolean) : PythonDataStructure {
    override fun and(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        if (other is PythonBool) {
            PythonBool(value && other.value).ok()
        } else {
            fail("Cannot do 'and' on $typeName and ${other.typeName}")
        }

    override fun or(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        if (other is PythonBool) {
            PythonBool(value && other.value).ok()
        } else {
            fail("Cannot do 'or' on $typeName and ${other.typeName}")
        }
}
