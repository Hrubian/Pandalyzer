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
import python.datastructures.invokeNondeterministic
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map
import python.ok
import python.withWarn

data class DataFrame_SortValuesFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun clone(): PythonDataStructure = this

    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext
    ): OperationResult<PythonDataStructure> =
        invokeNondeterministic(args, keywordArgs, outerContext) { iArgs, kArgs, ctx -> invokeInner(iArgs, kArgs, ctx) }

    private fun invokeInner(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext
    ): OperationResult<PythonDataStructure> {
        val matchedArguments = ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap())
        return matchedArguments.map { argumentSchema ->
            if (argumentSchema.matchedArguments["ascending"] !is PythonBool)
                return@map fail("The 'ascending' argument of sort_vallues must be a bool value")

            when (val by = argumentSchema.matchedArguments["by"]!!) {
                is PythonString -> sort_values(PythonList(mutableListOf(by)))
                is PythonList -> sort_values(by)
                else -> fail("The 'by' argument of Dataframe.sort_values must be a string or a list")
            }
        }
    }

    private fun sort_values(byList: PythonList): OperationResult<PythonDataStructure> {
        if (byList.items == null) {
            return dataFrame.clone().withWarn("Unable to resolve the by values in sort_values function")
        }

        val nonStringByItems = byList.items.filterNot { it is PythonString }
        if (nonStringByItems.isNotEmpty()) {
            return fail("All values of the 'by' argument of sort_values have to be strings but were $nonStringByItems")
        }

        val unknownByItems = byList.items.filter { (it as PythonString).value == null }
        if (unknownByItems.isNotEmpty()) {
            return dataFrame.clone().withWarn("Unable to resolve some 'by' values in the sort_values function.")
        }

        if (dataFrame.fields == null) {
            return DataFrame(null).ok()
        }

        val actualByItems = byList.items.map { (it as PythonString).value!! }
        val nonexistentByItems = actualByItems.filterNot { it in dataFrame.fields }
        if (nonexistentByItems.isNotEmpty()) {
            return fail("The dataframe does not contain the columns $nonexistentByItems")
        }

        return dataFrame.clone().ok()
    }

    private val argumentSchema = ResolvedArguments(
        arguments = listOf(PythonEntity.Arg("by"), PythonEntity.Arg("ascending")),
        defaults = listOf(PythonNone, PythonBool(true))
    )
}