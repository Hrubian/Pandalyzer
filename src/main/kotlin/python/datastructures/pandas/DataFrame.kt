package python.datastructures.pandas

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure

data class DataFrame(
    val fields: Map<FieldName, FieldType>,
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> {
        TODO("Not yet implemented")
    }

    override fun invoke(
        args: List<PythonDataStructure>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        TODO("Not yet implemented")
    }
}
