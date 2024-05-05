package analyzer

import analyzer.Pandalyzer.analyzeWith
import python.OperationResult
import python.PythonEntity
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonNone
import python.ok

fun analyzeStatements(
    statements: List<PythonEntity.Statement>,
    context: AnalysisContext,
): StatementAnalysisResult =
    statements.forEachIndexed { index, stmt ->
        when (stmt) {
            is PythonEntity.Statement.Break -> return StatementAnalysisResult.Breaked
            is PythonEntity.Statement.Continue -> return StatementAnalysisResult.Continued
            is PythonEntity.Statement.Return -> return StatementAnalysisResult.Returned(stmt.value?.analyzeWith(context) ?: PythonNone.ok())
            else -> stmt.analyzeWith(context).anotherFun(statements.drop(index + 1), context)?.let { return it } // todo refactor }
        }
    }.let { StatementAnalysisResult.Ended }

private fun StatementAnalysisResult.anotherFun( // todo rename me :)
    remainingStatements: List<PythonEntity.Statement>,
    context: AnalysisContext,
): StatementAnalysisResult? {
    when (this) {
        is StatementAnalysisResult.NondeterministicResult ->
            return StatementAnalysisResult.NondeterministicResult(
                leftResult =
                    when (leftResult) {
                        StatementAnalysisResult.Breaked -> leftResult
                        StatementAnalysisResult.Continued -> leftResult
                        StatementAnalysisResult.Ended -> analyzeStatements(remainingStatements, context)
                        is StatementAnalysisResult.NondeterministicResult -> leftResult.anotherFun(remainingStatements, context) ?: return null
                        is StatementAnalysisResult.Returned -> leftResult
                    },
                rightResult =
                    when (rightResult) {
                        StatementAnalysisResult.Breaked -> rightResult
                        StatementAnalysisResult.Continued -> rightResult
                        StatementAnalysisResult.Ended -> analyzeStatements(remainingStatements, context)
                        is StatementAnalysisResult.NondeterministicResult -> rightResult.anotherFun(remainingStatements, context) ?: return null
                        is StatementAnalysisResult.Returned -> rightResult
                    },
            )
        StatementAnalysisResult.Breaked -> return this
        StatementAnalysisResult.Continued -> return this
        StatementAnalysisResult.Ended -> return null
        is StatementAnalysisResult.Returned -> return this
    }
}

sealed interface StatementAnalysisResult {
    data object Breaked : StatementAnalysisResult

    data object Continued : StatementAnalysisResult

    @JvmInline
    value class Returned(val returnValue: OperationResult<PythonDataStructure>) : StatementAnalysisResult

    data object Ended : StatementAnalysisResult

    data class NondeterministicResult(
        val leftResult: StatementAnalysisResult,
        val rightResult: StatementAnalysisResult,
    ) : StatementAnalysisResult
}
