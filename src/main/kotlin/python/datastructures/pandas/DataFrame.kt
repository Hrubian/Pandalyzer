package python.datastructures.pandas

import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.functions.DataFrame_GroupByFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_MergeFunc
import python.fail
import python.ok

data class DataFrame(
    val fields: Map<FieldName, FieldType>,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "groupby" -> DataFrame_GroupByFunc(this).ok()
            "merge" -> DataFrame_MergeFunc(this).ok()
            else -> fail("Unknown identifier on dataframe: $identifier")
        }

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        when (key) {
            is PythonString -> {
                if (key.value in fields.keys) {
                    Series(fields[key.value]!!).ok()
                } else {
                    fail("The key ${key.value} does note exist in the dataframe")
                }
            }
            is PythonList -> {
                if (key.items.all { it is PythonString && it.value in fields }) {
                    DataFrame(fields.filterKeys { it in fields.keys }).ok()
                } else {
                    fail("TODO") // todo
                }
            }
            else -> fail("Cannot subscript with ${key.typeName} on dataframe")
        }
}
