package python.datastructures.pandas.dataframe

import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.functions.DataFrame_DropFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_GroupByFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_MergeFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_RenameFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_SortValuesFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_ToCsv
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
            "rename" -> DataFrame_RenameFunc(this).ok()
            "to_csv" -> DataFrame_ToCsv(this).ok()
            "drop" -> DataFrame_DropFunc(this).ok()
            "sort_values" -> DataFrame_SortValuesFunc(this).ok()
            else -> fail("Unknown identifier on dataframe: $identifier")
        }

    override fun clone(): PythonDataStructure = DataFrame(fields?.toMutableMap())

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> {
        when (key) {
            is PythonString -> {
                return if (fields == null) {
                    Series(null)
                        .withWarn("Unable to subscript a dataframe since the fields of the data frame are unknown")
                } else if (key.value == null) {
                    Series(null).withWarn("The key for subscript of dataframe is not known")
                } else if (key.value in fields.keys) {
                    Series(fields[key.value]!!).ok()
                } else {
                    fail("The key ${key.value} does not exist in the dataframe")
                }
            }

            is PythonList -> {
                if (fields == null) {
                    return DataFrame(null).withWarn("The data frame structure is unknown")
                } else if (key.items == null) {
                    return DataFrame(null).withWarn("The key for subscripting data frame is unknown")
                } else {//if (key.items.all { it is PythonString && it.value in fields }) {
                    val nonStringKeys = key.items.filterNot { it is PythonString }
                    if (nonStringKeys.isNotEmpty()) {
                        return fail("The items in the subscript list for dataframe must be all strings")
                    }

                    val unknownKeys = key.items.filter { (it as PythonString).value == null }
                    if (unknownKeys.isNotEmpty()) {
                        return DataFrame(null).withWarn("Unable to resolve some items for subscript of dataframe")
                    }

                    val actualKeys = key.items.map { (it as PythonString).value!! }.toSet()

                    return DataFrame(fields.filterKeys { it in actualKeys }.toMutableMap()).ok()
                }
            }
            is Series -> {
                return when (key.type) {
                    FieldType.BoolType -> clone().ok()
                    null -> UnresolvedStructure("Unknown type of series items").ok()
                    else -> fail("Boolean series expected, got ${key.type.name}")
                }
            }
            else -> return fail("Cannot subscript with ${key.typeName} on dataframe")
        }
    }

    override fun storeSubscript(
        slice: PythonDataStructure,
        value: PythonDataStructure
    ): OperationResult<PythonDataStructure> { //wrong!!! acts like normal subscript
        when (slice) {
            is PythonString -> {
                if (slice.value == null) {
                    return PythonNone.withWarn("Unable to subscript by an unknown string type")
                }
                if (fields == null) {
                    return PythonNone.withWarn("Unable to subscript to an unknown dataframe")
                }
                fields[slice.value] = FieldType.fromPythonDataStructure(value)
                    ?: return fail("Unable to convert type ${value.typeName} to pandas value")
                return PythonNone.ok()
            }
            is PythonList -> {
                if (fields == null) {
                    return DataFrame(null).withWarn("Unable to subscript to an unknown dataframe")
                }
                if (slice.items == null) {
                    return DataFrame(null).withWarn("Unable to subscript by an unknown list type")
                }
                val columns = slice.items.map {
                    it as? PythonString ?: return fail("All items of subscript list must be strings")
                }.map {
                    it.value ?: return DataFrame(null).withWarn("Unable to subscript by a list with unknown values")
                }
                TODO()
//                return DataFrame(fields = columns.associateWith {
//                    (fields[it] ?: return fail("The column $it is not in the dataframe")) }.toMutableMap()).ok()
            }
            else -> return fail("Cannot subscript to value of type ${value.typeName}")
        }
    }
}
