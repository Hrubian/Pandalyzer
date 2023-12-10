package analyzer.statement

import analyzer.AnalysisContext
import analyzer.nextContext
import pandas.PandasStructure
import python.PythonType

object AssignmentAnalysis {

    fun analyze(assignment: PythonType.Statement.Assign, context: AnalysisContext) = nextContext(context) {
        if (isPandasTypeHint(assignment.targets.first())) {
            returnValue(null)

        } else if (assignment.value is PythonType.Expression.Call) {
            return CallAnalysis.analyze(assignment.value).also { if (it.returnValue != null) }
        } else if (assignment.value is PythonType.Expression.Subscript) {

        } else if (assignment.value is PythonType.Expression.Name) {

        }
    }

    private fun createStructureFromTypeHint(typeHint: PythonType.Expression): PandasStructure {

    }

    private fun isPandasTypeHint(target: PythonType.Expression): Boolean =
        (target is PythonType.Expression.Name) && target.identifier.startsWith("__pandalyzer_hint_")

}