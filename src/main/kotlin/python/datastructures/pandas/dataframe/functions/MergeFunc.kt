package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonType
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonBool
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map

data class DataFrame_MergeFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val matchedArguments = ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap())
        return matchedArguments.map { argumentSchema ->
            val right = argumentSchema.matchedArguments["right"] as DataFrame
            when (val how = argumentSchema.matchedArguments["how"]!!) {
                is PythonString -> TODO() // merge(dataFrame, right, how, )
                is PythonList -> TODO()
                else -> fail("The 'how' argument of merge function cannot be of type ${how.typeName}")
            }
        }
    }

    override fun clone(): PythonDataStructure = DataFrame_MergeFunc(dataFrame.copy())

    private fun merge(
        left: DataFrame,
        right: DataFrame,
        how: PythonList,
        leftOn: PythonString,
        rightOn: PythonString,
    ): OperationResult<PythonDataStructure> {
        TODO()
    }

    private val argumentSchema =
        ResolvedArguments(
            arguments =
                listOf(
                    PythonType.Arg("right"),
                    PythonType.Arg("how"),
                    PythonType.Arg("on"),
                    PythonType.Arg("left_on"),
                    PythonType.Arg("right_on"),
                    PythonType.Arg("left_index"),
                    PythonType.Arg("right_index"),
                    PythonType.Arg("sort"),
                    PythonType.Arg("suffixes"),
                    PythonType.Arg("copy"),
                    PythonType.Arg("indicator"),
                    PythonType.Arg("validate"),
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
