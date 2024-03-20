package python.arguments

import analyzer.AnalysisContext
import analyzer.Identifier
import analyzer.Pandalyzer.foldExpressions
import python.OperationResult
import python.PythonType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonString
import python.fail
import python.ok

data class ResolvedArguments(
    val positionalArgs: List<PythonType.Arg>,
    val arguments: List<PythonType.Arg>,
    val variadicArg: PythonType.Arg?,
    val keywordVariadicArg: PythonType.Arg?,
    val keywordOnlyArgs: List<PythonType.Arg>,
    val keywordDefaults: List<PythonDataStructure>,
    val defaults: List<PythonDataStructure>,
) {
    companion object {
        fun PythonType.Arguments.resolve(context: AnalysisContext): Pair<ResolvedArguments, AnalysisContext> {
            val (resolved, newContext) = defaults.foldExpressions(context)
            val (resolvedKeywords, finalContext) = keywordDefaults.foldExpressions(newContext)
            return ResolvedArguments(
                positionalArgs = positionalArgs,
                arguments = arguments,
                variadicArg = variadicArg,
                keywordVariadicArg = keywordVariadicArg,
                keywordOnlyArgs = keywordOnlyArgs,
                keywordDefaults = resolvedKeywords.toList(),
                defaults = resolved.toList(),
            ) to finalContext
        }
    }
}

data class MatchedFunctionSchema(val matchedArguments: Map<Identifier, PythonDataStructure>)

object ArgumentMatcher {
    fun match(
        resolvedArguments: ResolvedArguments,
        calledPositionalArguments: List<PythonDataStructure>,
        calledKeywordArguments: Map<Identifier, PythonDataStructure>,
    ): OperationResult<MatchedFunctionSchema> =
        with(resolvedArguments) {
            val resultArgs = mutableMapOf<Identifier, PythonDataStructure>()

            // first, process positional arguments
            var argIndex = 0
            var defaultsIndex = 0
            for (argDef in resolvedArguments.positionalArgs) {
                if (argIndex == calledPositionalArguments.size) {
                    return fail("Missing positional argument ${argDef.identifier}")
                }
                resultArgs[argDef.identifier] = calledPositionalArguments[argIndex]
                argIndex++
            }

            for (argDef in resolvedArguments.arguments) {
                if (argIndex == calledPositionalArguments.size) {
                    resultArgs[argDef.identifier] = calledKeywordArguments[argDef.identifier]
                        ?: defaults.getOrElse(defaultsIndex++) {
                            return fail("")
                        }
                } else {
                    resultArgs[argDef.identifier] = calledPositionalArguments[argIndex]
                    argIndex++
                }
            }

            if (resolvedArguments.variadicArg != null) {
                val variadic = mutableListOf<PythonDataStructure>()
                while (argIndex < calledPositionalArguments.size) {
                    variadic.add(calledPositionalArguments[argIndex])
                    argIndex++
                }
                resultArgs[resolvedArguments.variadicArg.identifier] = PythonList(variadic)
            }

            // then process keyword arguments
            val remainingKeywordArgs = (calledKeywordArguments.keys - resultArgs.keys).toMutableSet()
            var defaultKeywordIndex = 0
            for (argDef in resolvedArguments.keywordOnlyArgs) {
                resultArgs[argDef.identifier] = calledKeywordArguments[argDef.identifier]
                    ?: keywordDefaults.getOrElse(defaultKeywordIndex) { return fail("") }
                remainingKeywordArgs.remove(argDef.identifier)
                defaultKeywordIndex++
            }

            if (resolvedArguments.keywordVariadicArg != null) {
                val variadicKeywords = mutableMapOf<PythonString, PythonDataStructure>()
                for (remainingKey in remainingKeywordArgs) {
                    variadicKeywords[PythonString(remainingKey)] = calledKeywordArguments[remainingKey]!!
                }
                resultArgs[keywordVariadicArg!!.identifier]
            } else if (remainingKeywordArgs.isNotEmpty()) {
                return fail("") // todo there is more keyword args than needed
            }

            return MatchedFunctionSchema(resultArgs.toMap()).ok()
        }
}
