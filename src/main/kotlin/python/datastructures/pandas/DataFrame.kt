package python.datastructures.pandas

import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.dataframe.functions.DataFrame_GroupByFunc
import python.datastructures.pandas.dataframe.functions.DataFrame_MergeFunc
import python.fail
import python.ok

data class DataFrame(
    val fields: Map<FieldName, FieldType>,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "groupby" -> DataFrame_GroupByFunc(this).ok()
            "merge" -> DataFrame_MergeFunc(this).ok()
            else -> fail("Unknown identifier on dataframe: $identifier")
        }
}
