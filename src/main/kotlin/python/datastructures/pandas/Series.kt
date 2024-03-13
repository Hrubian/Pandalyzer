package python.datastructures.pandas

import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonString
import python.fail

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
