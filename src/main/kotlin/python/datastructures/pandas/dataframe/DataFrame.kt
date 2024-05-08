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
import python.datastructures.nondeterministically
import python.datastructures.pandas.dataframe.functions.DataFrameDropFunc
import python.datastructures.pandas.dataframe.functions.DataFrameGroupByFunc
import python.datastructures.pandas.dataframe.functions.DataFrameMergeFunc
import python.datastructures.pandas.dataframe.functions.DataFrameRenameFunc
import python.datastructures.pandas.dataframe.functions.DataFrameSortValuesFunc
import python.datastructures.pandas.dataframe.functions.DataFrameToCsv
import python.datastructures.pandas.series.Series
import python.fail
import python.ok
import python.withWarn

data class DataFrame(
    val columns: MutableMap<FieldName, FieldType>?,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "groupby" -> DataFrameGroupByFunc(this).ok()
            "merge" -> DataFrameMergeFunc(this).ok()
            "rename" -> DataFrameRenameFunc(this).ok()
            "to_csv" -> DataFrameToCsv(this).ok()
            "drop" -> DataFrameDropFunc(this).ok()
            "sort_values" -> DataFrameSortValuesFunc(this).ok()
            else -> fail("Unknown identifier on dataframe: $identifier")
        }

    override fun clone(): PythonDataStructure = DataFrame(columns?.toMutableMap())

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        nondeterministically {
            when (key) {
                is PythonString -> {
                    return@nondeterministically if (columns == null) {
                        Series(null)
                            .withWarn("Unable to subscript a dataframe since the fields of the data frame are unknown")
                    } else if (key.value == null) {
                        Series(null).withWarn("The key for subscript of dataframe is not known")
                    } else if (key.value in columns.keys) {
                        Series(columns[key.value]!!).ok()
                    } else {
                        fail("The key ${key.value} does not exist in the dataframe")
                    }
                }

                is PythonList -> {
                    if (columns == null) {
                        return@nondeterministically DataFrame(null).withWarn("The data frame structure is unknown")
                    } else if (key.items == null) {
                        return@nondeterministically DataFrame(null)
                            .withWarn("The key for subscripting data frame is unknown")
                    } else { // if (key.items.all { it is PythonString && it.value in fields }) {
                        val nonStringKeys = key.items.filterNot { it is PythonString }
                        if (nonStringKeys.isNotEmpty()) {
                            return@nondeterministically fail("The items in the subscript list for dataframe must be all strings")
                        }

                        val unknownKeys = key.items.filter { (it as PythonString).value == null }
                        if (unknownKeys.isNotEmpty()) {
                            return@nondeterministically DataFrame(null)
                                .withWarn("Unable to resolve some items for subscript of dataframe")
                        }

                        val actualKeys = key.items.map { (it as PythonString).value!! }.toSet()
                        val nonexistentKeys = actualKeys.filterNot { it in columns }
                        if (nonexistentKeys.isNotEmpty()) {
                            return@nondeterministically fail("The keys $nonexistentKeys do not exist in the dataframe")
                        }

                        return@nondeterministically DataFrame(columns.filterKeys { it in actualKeys }.toMutableMap()).ok()
                    }
                }
                is Series -> {
                    return@nondeterministically when (key.type) {
                        FieldType.BoolType -> clone().ok()
                        null -> UnresolvedStructure("Unknown type of series items").ok()
                        else -> fail("Boolean series expected, got ${key.type.name}")
                    }
                }
                else -> return@nondeterministically fail("Cannot subscript with ${key.typeName} on dataframe")
            }
        }

    override fun storeSubscript(
        slice: PythonDataStructure,
        value: PythonDataStructure,
    ): OperationResult<PythonDataStructure> {
        when (slice) {
            is PythonString -> {
                if (slice.value == null) {
                    return PythonNone.withWarn("Unable to subscript by an unknown string type")
                }
                if (columns == null) {
                    return PythonNone.withWarn("Unable to subscript to an unknown dataframe")
                }
                columns[slice.value] = FieldType.fromPythonDataStructure(value)
                    ?: return fail("Unable to convert type ${value.typeName} to pandas value")
                return PythonNone.ok()
            }
            is PythonList -> {
                if (columns == null) {
                    return DataFrame(null).withWarn("Unable to subscript to an unknown dataframe")
                }
                if (slice.items == null) {
                    return DataFrame(null).withWarn("Unable to subscript by an unknown list type")
                }
                val columns =
                    slice.items.map {
                        it as? PythonString ?: return fail("All items of subscript list must be strings")
                    }.map {
                        it.value ?: return DataFrame(null).withWarn("Unable to subscript by a list with unknown values")
                    }
                TODO("Store subscript based on list not implemented yet")
            }
            else -> return fail("Cannot subscript to value of type ${value.typeName}")
        }
    }
}
