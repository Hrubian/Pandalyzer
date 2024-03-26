package analyzer

import analyzer.Pandalyzer.analyzeWith
import python.OperationResult
import python.PythonType
import python.datastructures.PythonDataStructure

fun analyzeStatements(statements: List<PythonType.Statement>, context: AnalysisContext): StatementAnalysisResult =
    statements.forEach { stmt ->
        when (stmt) {
            is PythonType.Statement.Break -> return StatementAnalysisResult.Breaked
            is PythonType.Statement.Continue -> return StatementAnalysisResult.Continued
            is PythonType.Statement.Return -> return StatementAnalysisResult.Returned(stmt.value.analyzeWith(context))
            else -> stmt.analyzeWith(context)
        }
    }.let { StatementAnalysisResult.Ended }

sealed interface StatementAnalysisResult {
    data object Breaked : StatementAnalysisResult

    data object Continued : StatementAnalysisResult

    @JvmInline
    value class Returned(val returnValue: OperationResult<PythonDataStructure>) : StatementAnalysisResult

    data object Ended : StatementAnalysisResult

    data class NondeterministicResult(
        val leftResult: StatementAnalysisResult,
        val rightResult: StatementAnalysisResult
    ) : StatementAnalysisResult
}