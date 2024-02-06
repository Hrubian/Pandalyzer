package python.datastructures

import analyzer.*
import python.OperationResult
import python.PythonType
import python.fail
import python.ok

data class PythonFunc(
    val name: Identifier?,
    val body: List<PythonType.Statement>,
    val positionArguments: List<String>
) : PythonDataStructure {
    override fun callWithArgs(
        args: List<PythonDataStructure>,
        outerContext: AnalysisContext
    ): OperationResult<PythonDataStructure> {
        val initialContext = ContextBuilder.buildEmpty(outerContext) //todo add arguments
        with(Pandalyzer()) {
            return when (val resultContext = body.foldStatements(initialContext)) {
                is AnalysisContext.OK -> PythonNone.ok()//resultContext.returnValue.ok() //todo really?
                is AnalysisContext.Returned -> resultContext.value.ok()
                is AnalysisContext.Error -> fail("The function $name failed with reason: ${resultContext.reason}")
            }
        }
    }
}
