package python.datastructures.pandas

import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure

data class Series(
    private val name: FieldName,
    private val type: FieldType,
    private val indexType: FieldType? = null,
) : PythonDataStructure {

}
