package python.datastructures

import python.OperationResult

data class Series(
    private val name: FieldName,
    private val type: FieldType,
    private val indexType: FieldType? = null,
) : PythonDataStructure {

}
