package python.datastructures.pandas

import python.datastructures.PythonDataStructure

data class Series(
    private val name: FieldName,
    private val type: FieldType
) : PythonDataStructure {

}
