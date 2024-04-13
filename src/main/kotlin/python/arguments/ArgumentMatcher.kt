package python.arguments

import analyzer.AnalysisContext
import analyzer.Identifier
import analyzer.Pandalyzer.analyzeWith
import python.OperationResult
import python.PythonType
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.fail
import python.ok
import python.orElse

data class ResolvedArguments(
    val positionalArgs: List<PythonType.Arg> = emptyList(),
    val arguments: List<PythonType.Arg> = emptyList(),
    val variadicArg: PythonType.Arg? = null,
    val keywordVariadicArg: PythonType.Arg? = null,
    val keywordOnlyArgs: List<PythonType.Arg> = emptyList(),
    val keywordDefaults: List<PythonDataStructure> = emptyList(),
    val defaults: List<PythonDataStructure> = emptyList(),
) {
    companion object {
        fun PythonType.Arguments.resolve(context: AnalysisContext): ResolvedArguments {
            val resolved = defaults.map { expr -> expr.analyzeWith(context).orElse { UnresolvedStructure(it) } }
            val resolvedKeywords =
                keywordDefaults.map { expr -> expr?.analyzeWith(context)?.orElse { UnresolvedStructure(it) } ?: PythonNone }

            return ResolvedArguments(
                positionalArgs = positionalArgs,
                arguments = arguments,
                variadicArg = variadicArg,
                keywordVariadicArg = keywordVariadicArg,
                keywordOnlyArgs = keywordOnlyArgs,
                keywordDefaults = resolvedKeywords.toList(),
                defaults = resolved.toList(),
            )
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
                } // todo should the positional take defaults too?
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
