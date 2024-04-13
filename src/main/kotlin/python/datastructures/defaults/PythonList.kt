package python.datastructures.defaults

import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.fail
import python.ok

@JvmInline
value class PythonList(
    val items: MutableList<PythonDataStructure>?,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "copy" -> PythonList(items?.map { it }?.toMutableList()).ok()
            else -> fail("unknown attributes $identifier")
        }

    override fun clone(): PythonDataStructure = PythonList(items?.map { it.clone() }?.toMutableList())

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> {
        if (key is PythonInt) {
            if (items == null) {
                return UnresolvedStructure("The list has unknown elements").ok()
            }
            if (key.value == null) {
                return UnresolvedStructure("The number used for indexing list is unknown").ok()
            }
            val item = items.getOrNull(key.value.toInt())
            return item?.ok() ?: fail("Index out of bounds")
        } else {
            return fail("Cannot subscript a ${key.typeCode} to a list.")
        }
    }

    override fun boolValue(): Boolean? = items?.isNotEmpty()
}
