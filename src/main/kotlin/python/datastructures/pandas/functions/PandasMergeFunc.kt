package python.datastructures.pandas.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonType
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.PandasFunction
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map
import python.ok

object PandasMergeFunc : PandasFunction {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        ArgumentMatcher.match(functionSchema, args, keywordArgs.toMap()).map {
            val left = it.matchedArguments["left"] as? DataFrame ?: return@map fail("Incorrect left argument to merge")
            val right = it.matchedArguments["right"] as? DataFrame ?: return@map fail("Incorrect right argument to merge")
            val how = it.matchedArguments["how"] as? PythonString ?: return@map fail("Incorrect how argument to merge")
            val on = it.matchedArguments["on"] as? PythonString
            val leftOn = it.matchedArguments["left_on"] as? PythonString
            val rightOn = it.matchedArguments["right_on"] as? PythonString

            if (allowedHowValues.contains(how.value).not()) {
                return@map fail("Incorrect 'how' value: ${how.value}")
            }

            return@map when {
                on != null && leftOn == null && rightOn == null -> merge(left, right, on.value, on.value)
                on == null && leftOn != null && rightOn != null -> merge(left, right, leftOn.value, rightOn.value)
                else -> fail("Incorrect combination of 'on', 'left_on' and 'right_on' arguments to merge")
            }
        }

    private fun merge( // TODO suffixes and lists
        left: DataFrame,
        right: DataFrame,
        leftOn: String,
        rightOn: String,
    ): OperationResult<PythonDataStructure> {
        val leftJoin = left.fields[leftOn] ?: return fail("The left dataframe does not contain the column $leftOn")
        val rightJoin = right.fields[rightOn] ?: return fail("The right dataframe does not contain the column $rightOn")
        if (leftJoin != rightJoin) {
            return fail("The types of $leftOn and $rightOn are different")
        }
        return DataFrame((left.fields + right.fields).toMutableMap()).ok()
    }

    private val allowedHowValues = setOf("inner", "outer", "left", "right", "cross")

    private val functionSchema =
        ResolvedArguments(
            arguments =
                listOf(
                    PythonType.Arg("left"),
                    PythonType.Arg("right"),
                    PythonType.Arg("how"),
                    PythonType.Arg("on"),
                    PythonType.Arg("left_on"),
                    PythonType.Arg("right_on"), // todo missing args
                ),
            defaults =
                listOf(
                    PythonNone,
                    PythonNone,
                    PythonString("inner"),
                    PythonNone,
                    PythonNone,
                    PythonNone,
                ),
            keywordDefaults = emptyList(),
            keywordOnlyArgs = emptyList(),
            variadicArg = null,
            keywordVariadicArg = null,
            positionalArgs = emptyList(),
        )
}
