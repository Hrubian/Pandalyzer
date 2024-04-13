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
import python.datastructures.pandas.dataframe.DataFrameGroupBy
import python.fail
import python.map
import python.ok
import python.withWarn

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

    override fun clone(): PythonDataStructure = DataFrame_GroupByFunc(dataFrame.clone() as DataFrame)

    private fun groupBy(
        dataFrame: DataFrame,
        by: PythonString,
    ): OperationResult<PythonDataStructure> {
        return if (by.value == null) {
            DataFrameGroupBy(null, null).withWarn("The 'by' is unknown")
        } else if (dataFrame.fields == null) {
            DataFrameGroupBy(null, null).withWarn("The fields of dataframe are unknown")
        } else if (by.value in dataFrame.fields) {
            DataFrameGroupBy(dataFrame, mutableListOf(by.value)).ok()
        } else {
            fail("The dataframe does not contain the field $by. Dataframe columns: ${dataFrame.fields.keys}")
        }
    }

    private fun groupBy(
        dataFrame: DataFrame,
        by: PythonList,
    ): OperationResult<PythonDataStructure> {
        if (by.items == null) {
            return DataFrameGroupBy(null, null).withWarn("The 'by' is unknown")
        }
        if (dataFrame.fields == null) {
            return DataFrameGroupBy(null, null).withWarn("The fields of the dataframe are unknown")
        }
        val keys = by.items.map { it as? PythonString ?: return fail("Cannot group a dataframe by ${it.typeName}") }

        val missingKeys = keys.filterNot { it.value in dataFrame.fields }
        if (missingKeys.isNotEmpty()) {
            return fail(
                "Cannot group by keys $missingKeys, since they were not found in the dataframe. " +
                    "Dataframe columns: ${dataFrame.fields.keys}",
            )
        }

        return DataFrameGroupBy(dataFrame, by.items.map { (it as PythonString).value }.toMutableList()).ok()
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
