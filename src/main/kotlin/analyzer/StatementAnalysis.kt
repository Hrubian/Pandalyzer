package analyzer

import analyzer.Pandalyzer.analyzeWith
import python.OperationResult
import python.PythonType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonNone
import python.ok

fun analyzeStatements(statements: List<PythonType.Statement>, context: AnalysisContext): StatementAnalysisResult =
    statements.forEachIndexed { index, stmt ->
        when (stmt) {
            is PythonType.Statement.Break -> return StatementAnalysisResult.Breaked
            is PythonType.Statement.Continue -> return StatementAnalysisResult.Continued
            is PythonType.Statement.Return -> return StatementAnalysisResult.Returned(stmt.value?.analyzeWith(context) ?: PythonNone.ok())
            else -> stmt.analyzeWith(context).anotherFun(statements.drop(index + 1), context)?.let { return it } //todo refactor
        }
    }.let { StatementAnalysisResult.Ended }

private fun StatementAnalysisResult.anotherFun( //todo rename me :)
    remainingStatements: List<PythonType.Statement>,
    context: AnalysisContext
): StatementAnalysisResult? {
    if (this is StatementAnalysisResult.NondeterministicResult) {
        return StatementAnalysisResult.NondeterministicResult(
            leftResult = when (leftResult) {
                StatementAnalysisResult.Breaked -> leftResult
                StatementAnalysisResult.Continued -> leftResult
                StatementAnalysisResult.Ended -> analyzeStatements(remainingStatements, context)
                is StatementAnalysisResult.NondeterministicResult -> leftResult.anotherFun(remainingStatements, context) ?: return null
                is StatementAnalysisResult.Returned -> leftResult
            },
            rightResult = when (rightResult) {
                StatementAnalysisResult.Breaked -> rightResult
                StatementAnalysisResult.Continued -> rightResult
                StatementAnalysisResult.Ended -> analyzeStatements(remainingStatements, context)
                is StatementAnalysisResult.NondeterministicResult -> rightResult.anotherFun(remainingStatements, context) ?: return null
                is StatementAnalysisResult.Returned -> rightResult
            }
        )
    } else return null
}

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