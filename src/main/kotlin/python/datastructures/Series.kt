package python.datastructures

import python.OperationResult

data class Series(
    private val name: FieldName,
    private val type: FieldType,
    private val indexType: FieldType? = null,
) : PythonDataStructure {

    override fun sumWith(struct: PythonDataStructure): OperationResult<PythonDataStructure> {
         when (struct) {
            is DataFrame -> TODO()
            is DataFrameGroupBy -> TODO()
            is NondeterministicDataStructure -> TODO()
            is PythonInt -> TODO()
            is PythonString -> TODO()
            is Series -> TODO()
            is SeriesGroupBy -> TODO()
        }
    }
}
