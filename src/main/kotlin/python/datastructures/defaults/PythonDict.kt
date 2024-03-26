package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

data class PythonDict(val values: MutableMap<PythonDataStructure, PythonDataStructure>) : PythonDataStructure {
    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        values[key]?.ok() ?: fail("The key $key is not present in the dictionary")

    override fun clone(): PythonDataStructure =
        PythonDict(values.map { it.key.clone() to it.value.clone() }.toMap().toMutableMap())
}
