package python.datastructures.pandas.dataframe

import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.functions.DataFrame_GroupByFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_MergeFunc
import python.datastructures.pandas.series.Series
import python.fail
import python.ok
import python.withWarn

data class DataFrame(
    val fields: MutableMap<FieldName, FieldType>?,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "groupby" -> DataFrame_GroupByFunc(this).ok()
            "merge" -> DataFrame_MergeFunc(this).ok()
            else -> fail("Unknown identifier on dataframe: $identifier")
        }

    override fun clone(): PythonDataStructure = DataFrame(fields?.toMutableMap())

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        when (key) {
            is PythonString -> {
                if (fields == null) {
                    Series(null).withWarn("The key for subscripting data frame is unknown")
                } else if (key.value in fields.keys) {
                    Series(fields[key.value]!!).ok()
                } else {
                    fail("The key ${key.value} does not exist in the dataframe")
                }
            }
            is PythonList -> {
                if (fields == null) {
                    DataFrame(null).withWarn("The data frame structure is unknown")
                } else if (key.items == null) {
                    DataFrame(null).withWarn("The key for subscripting data frame is unknown")
                } else if (key.items.all { it is PythonString && it.value in fields }) {
                    DataFrame(
                        fields.filterKeys { it in key.items.map { it as PythonString }.map { it.value } }.toMutableMap(),
                    ).ok() // todo this is disgusting :)
                } else {
                    fail("TODO") // todo
                }
            }
            is Series -> {
                when (key.type) {
                    FieldType.BoolType -> clone().ok()
                    null -> UnresolvedStructure("Unknown type of series items").ok()
                    else -> fail("Boolean series expected, got ${key.type.name}")
                }
            }
            else -> fail("Cannot subscript with ${key.typeName} on dataframe")
        }
}
