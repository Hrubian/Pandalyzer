package python.datastructures.pandas

import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure

data class Series(
//    val name: FieldName,
    val type: FieldType,
    val indexType: FieldType? = null,
) : PythonDataStructure
