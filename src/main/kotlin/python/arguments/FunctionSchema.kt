package python.arguments

import analyzer.Identifier
import python.datastructures.PythonDataStructure

data class FunctionSchema(
    val arguments: List<ArgumentMarker>,
)

sealed interface ArgumentMarker

data class NormalArgument<ExpectedType : PythonDataStructure>(val name: String) : ArgumentMarker

data class PositionalArgument<ExpectedType : PythonDataStructure>(val name: String, val default: ExpectedType) : ArgumentMarker

data object VariadicArgsMarker : ArgumentMarker

data object VariadicKeywordArgsMarker : ArgumentMarker

data class MatchedFunctionSchema(
//    val matchedArguments: List<Pair<ArgumentMarker, PythonDataStructure>>,
    val matchedArguments: Map<Identifier, PythonDataStructure>
)
