package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonEntity
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonBool
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.nonDeterministically
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map
import python.ok
import python.withWarn

data class DataFrame_MergeFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val matchedArguments = ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap())
        return matchedArguments.map {
            val left = dataFrame//it.matchedArguments["left"] as? DataFrame ?: return@map fail("Incorrect left argument to merge")
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

//            val right = argumentSchema.matchedArguments["right"] as DataFrame
//            when (val how = argumentSchema.matchedArguments["how"]!!) {
//                is PythonString -> TODO() // merge(dataFrame, right, how, )
//                is PythonList -> TODO()
//                else -> fail("The 'how' argument of merge function cannot be of type ${how.typeName}")
//            }
        }
    }

    override fun clone(): PythonDataStructure = DataFrame_MergeFunc(dataFrame.copy())

    private fun merge( // TODO suffixes and lists
        left: DataFrame,
        right: DataFrame,
        leftOn: String,
        rightOn: String,
    ): OperationResult<PythonDataStructure> = left.nonDeterministically {
        if (left.columns == null) {
            return DataFrame(null).withWarn("The fields of the left dataframe are unknown")
        }
        if (right.columns == null) {
            return DataFrame(null).withWarn("The fields of the right dataframe are unknown")
        }
        val leftJoin = left.columns[leftOn] ?: return fail("The left dataframe does not contain the column $leftOn")
        val rightJoin = right.columns[rightOn] ?: return fail("The right dataframe does not contain the column $rightOn")
        if (leftJoin != rightJoin) {
            return fail("The types of $leftOn and $rightOn are different")
        }
        return DataFrame((left.columns + right.columns).toMutableMap()).ok()
    }

    private val allowedHowValues = setOf("inner", "outer", "left", "right", "cross")

    private val argumentSchema =
        ResolvedArguments(
            arguments =
                listOf(
                    PythonEntity.Arg("right"),
                    PythonEntity.Arg("how"),
                    PythonEntity.Arg("on"),
                    PythonEntity.Arg("left_on"),
                    PythonEntity.Arg("right_on"),
                    PythonEntity.Arg("left_index"),
                    PythonEntity.Arg("right_index"),
                    PythonEntity.Arg("sort"),
                    PythonEntity.Arg("suffixes"),
                    PythonEntity.Arg("copy"),
                    PythonEntity.Arg("indicator"),
                    PythonEntity.Arg("validate"),
                ),
            defaults =
                listOf(
                    PythonNone,
                    PythonString("inner"),
                    PythonNone,
                    PythonNone,
                    PythonNone,
                    PythonBool(false),
                    PythonBool(false),
                    PythonBool(false),
                    PythonNone, // todo
                    PythonNone,
                    PythonBool(false),
                    PythonNone,
                ),
            keywordDefaults = emptyList(),
            keywordOnlyArgs = emptyList(),
            variadicArg = null,
            keywordVariadicArg = null,
            positionalArgs = emptyList(),
        )
}
