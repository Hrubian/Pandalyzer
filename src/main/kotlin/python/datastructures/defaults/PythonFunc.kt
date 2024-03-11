package python.datastructures.defaults

import analyzer.AnalysisContext
import analyzer.ContextBuilder
import analyzer.Identifier
import analyzer.Pandalyzer
import analyzer.map
import python.OperationResult
import python.PythonType
import python.arguments.ArgumentMatcher
import python.arguments.MatchedFunctionSchema
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

data class PythonFunc(
    val name: Identifier?,
    val body: List<PythonType.Statement>,
    val arguments: ResolvedArguments
) : PythonDataStructure {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext
    ): OperationResult<PythonDataStructure> {
        val initialContext = ContextBuilder.buildEmpty(outerContext)
        val matchedArguments = ArgumentMatcher.match(arguments, args, keywordArgs.toMap())

        val contextWithArgs = initialContext.map {
            when (matchedArguments) {
                is OperationResult.Error -> fail(matchedArguments.reason)
                is OperationResult.Warning -> {
                    addWarning(matchedArguments.message)
                    addArgs(matchedArguments.result)
                }
                is OperationResult.Ok -> addArgs(matchedArguments.result)
            }
        }

        with(Pandalyzer) {
            return when (val resultContext = body.foldStatements(contextWithArgs)) {
                is AnalysisContext.OK -> PythonNone.ok()
                is AnalysisContext.Returned -> resultContext.value.ok()
                is AnalysisContext.Error -> fail("The function $name failed with reason: ${resultContext.reason}")
            }
        }
    }

    private companion object {
        fun ContextBuilder.addArgs(args: MatchedFunctionSchema) {
            args.matchedArguments.forEach { addStruct(it.key, it.value) }
        }
    }
}
