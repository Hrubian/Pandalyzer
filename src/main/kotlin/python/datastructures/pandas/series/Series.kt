package python.datastructures.pandas.series

import python.OperationResult
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure

data class Series(
//    val name: FieldName,
    val type: FieldType,
    val indexType: FieldType? = null,
) : PythonDataStructure {
    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> = TODO()
//        when (key) {
//            is PythonList ->
//            is PythonString ->
//            else -> fail("")
//        }
}
