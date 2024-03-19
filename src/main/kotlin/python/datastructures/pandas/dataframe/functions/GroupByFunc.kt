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
import python.datastructures.pandas.DataFrame
import python.datastructures.pandas.DataFrameFunction
import python.datastructures.pandas.DataFrameGroupBy
import python.fail
import python.map
import python.ok

data class DataFrame_GroupByFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val matchedArguments = ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap())
        return matchedArguments.map { argumentSchema ->
            when (val by = argumentSchema.matchedArguments["by"]!!) {
                is PythonString -> groupBy(dataFrame, by)
                is PythonList -> groupBy(dataFrame, by)
                else -> fail("The 'by' argument of Dataframe.groupby must be a string or a list")
            }
        }
    }

    private fun groupBy(
        dataFrame: DataFrame,
        by: PythonString,
    ): OperationResult<PythonDataStructure> {
        return if (by.value in dataFrame.fields) {
            DataFrameGroupBy(dataFrame, listOf(by.value)).ok()
        } else {
            fail("The dataframe does not contain the field $by. Dataframe columns: ${dataFrame.fields.keys}")
        }
    }

    private fun groupBy(
        dataFrame: DataFrame,
        by: PythonList,
    ): OperationResult<PythonDataStructure> {
        val keys = by.items.map { it as? PythonString ?: return fail("Cannot group a dataframe by ${it.typeName}") }

        val missingKeys = keys.filterNot { it.value in dataFrame.fields }
        if (missingKeys.isNotEmpty()) {
            return fail(
                "Cannot group by keys $missingKeys, since they were not found in the dataframe. " +
                    "Dataframe columns: ${dataFrame.fields.keys}",
            )
        }

        return DataFrameGroupBy(dataFrame, by.items.map { (it as PythonString).value }).ok()
    }

    private val argumentSchema =
        ResolvedArguments(
            arguments =
                listOf(
                    PythonType.Arg("by"),
                    PythonType.Arg("axis"),
                    PythonType.Arg("level"),
                    PythonType.Arg("as_index"),
                    PythonType.Arg("sort"),
                    PythonType.Arg("group_keys"),
                    PythonType.Arg("observed"),
                    PythonType.Arg("dropna"),
                ),
            defaults =
                listOf(
                    PythonNone,
                    PythonNone, // todo
                    PythonNone, // todo,
                    PythonBool(true),
                    PythonBool(true),
                    PythonBool(true),
                    PythonNone,
                    PythonBool(true),
                ),
            keywordDefaults = emptyList(),
            keywordOnlyArgs = emptyList(),
            variadicArg = null,
            keywordVariadicArg = null,
            positionalArgs = emptyList(),
        )
}
