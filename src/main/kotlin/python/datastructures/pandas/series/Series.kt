package python.datastructures.pandas.series

import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonFloat
import python.datastructures.defaults.PythonInt
import python.datastructures.defaults.PythonString
import python.fail
import python.ok
import python.withWarn

data class Series(
    val type: FieldType?,
    val label: FieldName? = null,
) : PythonDataStructure {
    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        type?.toPythonDataStructure()?.withWarn("Missing value information when subscripting Series")
            ?: fail("Unable to subscript to a series with unknown type")

    override fun clone(): PythonDataStructure = this

    override fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        when (other) {
            is Series -> {
                return if (type == null || other.type == null) {
                    Series(null).withWarn("Unable to sum series of unknown type")
                } else if (type == other.type) {
                    Series(type).ok()
                } else {
                    fail("Cannot sum series of two different types: $type, ${other.type}")
                }
            }
            is PythonInt -> {
                return if (type == null) {
                    Series(null).withWarn("Unable to sum series of unknown type")
                } else if (type.toPythonDataStructure() is PythonInt) {
                    Series(type).ok()
                } else {
                    fail("Cannot sum a series of type $type with PythonInt")
                }
            }
            is PythonString -> {
                return if (type == null) {
                    Series(null).withWarn("Unable to sum series of unknown type")
                } else if (type.toPythonDataStructure() is PythonString) {
                    Series(type).ok()
                } else {
                    fail("Cannot sum a series of type $type with PythonString")
                }
            }
            is PythonFloat -> {
                return if (type == null) {
                    Series(null).withWarn("Unable to sum series of unknown type")
                } else if (type.toPythonDataStructure() is PythonInt) {
                    Series(type).ok()
                } else {
                    fail("Cannot sum a series of type $type with PythonInt")
                }
            }
            else -> {
                return fail("Cannot sum a series with ${other.typeName}")
            }
        }
    }

    override infix fun equal(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other)

    override infix fun greaterThan(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other)

    override infix fun greaterThanEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other)

    override infix fun lessThan(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other)

    override infix fun lessThanEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other)

    override infix fun notEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other)

    private fun compare(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        if (type == null) {
            return Series(FieldType.BoolType).withWarn(
                "Unable to check type compatibility " +
                    "of comparison since the series structure is not known",
            )
        }
        return if (type.toPythonDataStructure().typeName == other.typeName) {
            // todo what about comparison of e.g., ints and floats
            Series(FieldType.BoolType).ok()
        } else {
            fail("Unable to compare values of type $type and ${other.typeName}")
        }
    }
}
