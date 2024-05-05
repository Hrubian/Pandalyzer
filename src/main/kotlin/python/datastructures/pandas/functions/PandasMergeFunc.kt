package python.datastructures.pandas.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonEntity
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.invokeNondeterministic
import python.datastructures.nonDeterministically
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map
import python.ok
import python.withWarn

object PandasMergeFunc : PandasFunction {

    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        invokeNondeterministic(args, keywordArgs, outerContext) { iArgs, kArgs, ctx -> invokeInner(iArgs, kArgs, ctx) }

        private fun invokeInner(
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
                on != null && leftOn == null && rightOn == null -> {
                    if (on.value == null) {
                        DataFrame(null).withWarn("The value of on is unknown")
                    } else {
                        merge(left, right, on.value, on.value)
                    }
                }

                on == null && leftOn != null && rightOn != null -> {
                    if (leftOn.value == null) {
                        DataFrame(null).withWarn("the leftOn is unknown")
                    } else if (rightOn.value == null) {
                        DataFrame(null).withWarn("the rightOn is unknown")
                    } else {
                        merge(left, right, leftOn.value, rightOn.value)
                    }
                }
                else -> fail("Incorrect combination of 'on', 'left_on' and 'right_on' arguments to merge")
            }
        }

    private fun merge( // TODO suffixes and lists
        left: DataFrame,
        right: DataFrame,
        leftOn: String,
        rightOn: String,
    ): OperationResult<PythonDataStructure> = left.nonDeterministically {
        if (left.fields == null) {
            return DataFrame(null).withWarn("The fields of the left dataframe are unknown")
        }
        if (right.fields == null) {
            return DataFrame(null).withWarn("The fields of the right dataframe are unknown")
        }
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
                    PythonEntity.Arg("left"),
                    PythonEntity.Arg("right"),
                    PythonEntity.Arg("how"),
                    PythonEntity.Arg("on"),
                    PythonEntity.Arg("left_on"),
                    PythonEntity.Arg("right_on"), // todo missing args
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
