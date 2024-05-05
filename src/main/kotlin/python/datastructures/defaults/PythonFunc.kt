package python.datastructures.defaults

import analyzer.AnalysisContext
import analyzer.Identifier
import analyzer.StatementAnalysisResult
import analyzer.StatementAnalysisResult.NondeterministicResult
import analyzer.analyzeStatements
import python.OperationResult
import python.PythonEntity
import python.arguments.ArgumentMatcher
import python.arguments.MatchedFunctionSchema
import python.arguments.ResolvedArguments
import python.datastructures.NondeterministicDataStructure.Companion.combineResults
import python.datastructures.PythonDataStructure
import python.fail
import python.ok
import python.orElse

data class PythonFunc(
    val name: Identifier,
    val body: List<PythonEntity.Statement>,
    val arguments: ResolvedArguments,
    val functionDef: PythonEntity.Statement.FunctionDef,
) : PythonDataStructure {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val functionContext = AnalysisContext.buildForFunction(outerContext)
        val (matchedArguments, warns) = ArgumentMatcher.match(arguments, args, keywordArgs.toMap()).orElse { return fail(it) }
        outerContext.addWarnings(warns, functionDef)
        functionContext.addArgs(matchedArguments)

        return analyzeStatements(body, functionContext).extractOperationResult()
    }

    override fun clone(): PythonDataStructure = this

    private companion object {
        fun AnalysisContext.addArgs(args: MatchedFunctionSchema) {
            args.matchedArguments.forEach { upsertStruct(it.key, it.value) }
        }

        fun StatementAnalysisResult.extractOperationResult(): OperationResult<PythonDataStructure> =
            when (this) {
                StatementAnalysisResult.Breaked -> fail("Break outside the loop")
                StatementAnalysisResult.Continued -> fail("Continue outside the loop")
                StatementAnalysisResult.Ended -> PythonNone.ok()
                is NondeterministicResult ->
                    combineResults(leftResult.extractOperationResult(), rightResult.extractOperationResult())
                is StatementAnalysisResult.Returned -> returnValue
            }
    }
}
