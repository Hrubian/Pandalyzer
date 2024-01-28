package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.fail
import python.ok

data class DataFrame(
    val fields: Map<FieldName, FieldType>
) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> {
        TODO("Not yet implemented")
    }

    override fun callWithArgs(args: List<PythonDataStructure>, outerContext: AnalysisContext): OperationResult<PythonDataStructure> {
        TODO("Not yet implemented")
    }

}
