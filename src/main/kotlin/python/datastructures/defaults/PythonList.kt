package python.datastructures.defaults

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.fail
import python.ok
import python.withWarn

@JvmInline
value class PythonList(
    val items: MutableList<PythonDataStructure>?,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "copy" -> PythonList(items?.map { it }?.toMutableList()).ok()
            "append" -> Append(this).ok()
            "insert" -> Insert(this).ok()
            "remove" -> Remove(this).ok()
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
            return fail("Cannot subscript a ${key.typeName} to a list.")
        }
    }

    override fun boolValue(): Boolean? = items?.isNotEmpty()

    override fun inn(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        TODO()
    }

    data class Append(val list: PythonList) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext
        ): OperationResult<PythonDataStructure> {
            val elem = args.singleOrNull() ?: keywordArgs.singleOrNull { it.first == "elem" }?.second
                ?: return fail("Missing 'elem' argument in list append function.")
            if (list.items != null) {
                list.items.add(elem)
                return PythonNone.ok()
            } else {
                return PythonNone.withWarn("Unable to add element to a list with unknown content")
            }
        }
    }

    data class Insert(val list: PythonList) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext
        ): OperationResult<PythonDataStructure> {
            TODO("Not yet implemented")
        }
    }

    data class Remove(val list: PythonList) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext
        ): OperationResult<PythonDataStructure> {
            TODO("Not yet implemented")
        }
    }

}
