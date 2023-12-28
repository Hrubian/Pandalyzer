package python.datastructures

import analyzer.Identifier
import python.OperationResult
import python.fail
import python.ok

data class DataFrame(
    val fields: Map<FieldName, FieldType>
) : PythonDataStructure {

    override fun index(key: Identifier): OperationResult<PythonDataStructure> {
        return fields[key]?.let { fieldType ->
            Series(key, fieldType).ok()
        } ?: fail("The dataframe does not contain field $key")
    }

    override fun indexList(keys: List<Identifier>): OperationResult<PythonDataStructure> {
        val missingKeys = keys.filterNot { it in fields.keys }
        if (missingKeys.isNotEmpty()) {
            return fail("The dataframe does not contain fields: $missingKeys")
        }

        val subset = fields.filterKeys { key -> key in keys }
        return DataFrame(subset).ok()
    }

    override fun indexStruct(struct: PythonDataStructure): OperationResult<PythonDataStructure> {
        TODO()
    }

    override fun concat(struct: PythonDataStructure, axis: Int): OperationResult<PythonDataStructure> {

    }

    override fun groupBy(key: Identifier): OperationResult<PythonDataStructure> {

    }

    override fun groupBy(keys: List<Identifier>): OperationResult<PythonDataStructure> {

    }



}
