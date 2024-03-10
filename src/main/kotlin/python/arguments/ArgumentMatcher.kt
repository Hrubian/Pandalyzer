package python.arguments

import analyzer.Identifier
import python.OperationResult
import python.PythonType
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

object ArgumentMatcher {
    fun match(
        definedArguments: PythonType.Arguments,
        calledPositionalArguments: List<PythonDataStructure>,
        calledKeywordArguments: Map<Identifier, PythonDataStructure>
    ): OperationResult<MatchedFunctionSchema> = with(definedArguments) {
        val resultArgs = mutableMapOf<Identifier, PythonDataStructure>()

        // first, process positional arguments
        var argIndex = 0
        val defaultKeywordsIndex = 0
        for (argDef in definedArguments.positionalArgs) {
            if (argIndex == calledPositionalArguments.size) {
                return fail("")
            }
            resultArgs[argDef.identifier] = calledPositionalArguments[argIndex]
            argIndex++
        }

        for (argDef in definedArguments.arguments) {
            if (argIndex == calledPositionalArguments.size) {
                resultArgs[argDef.identifier] = calledKeywordArguments[argDef.identifier]
                    ?: defaults.getOrElse(defaultKeywordsIndex++) {
                    return fail("")
                }
            } else {
                resultArgs[argDef.identifier] = calledPositionalArguments[argIndex]
                argIndex++
            }
        }

        if (definedArguments.variadicArg != null) {
            val
        }

        // then process keyword arguments
        var kwArgIndex = 0
        for (argDef in definedArguments.keywordOnlyArgs) {

        }

        if (definedArguments.keywordVariadicArg != null) {

        }


        return MatchedFunctionSchema(resultArgs.toMap()).ok()
    }
}
