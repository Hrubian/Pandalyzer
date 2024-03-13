package python.datastructures.defaults

import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

@JvmInline
value class PythonList(
    val items: MutableList<PythonDataStructure>,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "copy" -> PythonList(items.map { it }.toMutableList()).ok()
            else -> fail("unknown attributes $identifier")
        }

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> {
        if (key is PythonInt) {
            val item = items.getOrNull(key.value.toInt())
            if (item != null) {
                return item.ok()
            } else {
                return fail("Index out of bounds")
            }
        } else {
            return fail("Cannot subscript a ${key.typeName} to a list.")
        }
    }
}
