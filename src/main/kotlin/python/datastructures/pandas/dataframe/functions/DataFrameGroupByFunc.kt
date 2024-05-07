package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonEntity
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

data class DataFrameGroupByFunc(override val dataFrame: DataFrame) : DataFrameFunction {
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

    override fun clone(): PythonDataStructure = DataFrameGroupByFunc(dataFrame.clone() as DataFrame)

    private fun groupBy(
        dataFrame: DataFrame,
        by: PythonString,
    ): OperationResult<PythonDataStructure> {
        return if (by.value == null) {
            DataFrameGroupBy(null, null).withWarn("The 'by' is unknown")
        } else if (dataFrame.columns == null) {
            DataFrameGroupBy(null, null).withWarn("The fields of dataframe are unknown")
        } else if (by.value in dataFrame.columns) {
            DataFrameGroupBy(dataFrame, mutableListOf(by.value)).ok()
        } else {
            fail("The dataframe does not contain the field ${by.value}. Dataframe columns: ${dataFrame.columns.keys}")
        }
    }

    private fun groupBy(
        dataFrame: DataFrame,
        by: PythonList,
    ): OperationResult<PythonDataStructure> {
        if (by.items == null) {
            return DataFrameGroupBy(null, null).withWarn("The 'by' is unknown")
        }
        if (dataFrame.columns == null) {
            return DataFrameGroupBy(null, null).withWarn("The fields of the dataframe are unknown")
        }
        val keys = by.items.map { it as? PythonString ?: return fail("Cannot group a dataframe by ${it.typeName}") }

        val missingKeys = keys.filterNot { it.value in dataFrame.columns }
        if (missingKeys.isNotEmpty()) {
            return fail(
                "Cannot group by keys $missingKeys, since they were not found in the dataframe. " +
                    "Dataframe columns: ${dataFrame.columns.keys}",
            )
        }

        return DataFrameGroupBy(dataFrame, by.items.map { (it as PythonString).value }.toMutableList()).ok()
    }

    private val argumentSchema =
        ResolvedArguments(
            arguments =
                listOf(
                    PythonEntity.Arg("by"),
                    PythonEntity.Arg("axis"),
                    PythonEntity.Arg("level"),
                    PythonEntity.Arg("as_index"),
                    PythonEntity.Arg("sort"),
                    PythonEntity.Arg("group_keys"),
                    PythonEntity.Arg("observed"),
                    PythonEntity.Arg("dropna"),
                ),
            defaults =
                listOf(
                    PythonNone,
                    PythonNone,
                    PythonNone,
                    PythonBool(true),
                    PythonBool(true),
                    PythonBool(true),
                    PythonNone,
                    PythonBool(true),
                ),
        )
}
