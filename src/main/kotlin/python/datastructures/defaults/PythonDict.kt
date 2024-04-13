package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.fail
import python.ok

data class PythonDict(val values: MutableMap<PythonDataStructure, PythonDataStructure>?) : PythonDataStructure {
    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (values != null) {
            values[key]?.ok() ?: fail("The key $key is not present in the dictionary")
        } else {
            UnresolvedStructure("The dictionary has unknown values").ok()
        }
    }

    override fun clone(): PythonDataStructure =
        PythonDict(values?.map { it.key.clone() to it.value.clone() }?.toMap()?.toMutableMap())

    override fun boolValue(): Boolean? = values?.isNotEmpty()
}
