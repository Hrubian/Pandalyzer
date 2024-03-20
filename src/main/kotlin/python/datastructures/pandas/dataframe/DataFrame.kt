package python.datastructures.pandas.dataframe

import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.functions.DataFrame_GroupByFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_MergeFunc
import python.datastructures.pandas.series.Series
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
                    DataFrame(fields.filterKeys { it in key.items.map { it as PythonString}.map { it.value} }).ok() //todo this is disgusting :)
                } else {
                    fail("TODO") // todo
                }
            }
//            is DataFrame -> {
//                if (fields.keys != key.fields.keys) {
//                    fail("The indexing dataset has different keys")
//                }
//                if (key.fields.values.any { it != FieldType.BoolType }) {
//                    fail("Cannot use dataframe with field of different type than bool for indexing")
//                }
//
//            }
            else -> fail("Cannot subscript with ${key.typeName} on dataframe")
        }
}
