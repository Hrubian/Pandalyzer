package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonEntity
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonDict
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.invokeNondeterministic
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map
import python.ok
import python.withWarn

data class DataFrame_RenameFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun clone(): PythonDataStructure = this

    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        invokeNondeterministic(args, keywordArgs, outerContext) { a, k, o -> invokeInner(a, k, o) }


        fun invokeInner(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val matchedArguments = ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap())
        return matchedArguments.map { arguments ->
            val columns =
                arguments.matchedArguments["columns"] as? PythonDict
                    ?: return@map fail("The rename func only accepts a dictionary")
            rename(columns)
        }
    }

    private fun rename(dict: PythonDict): OperationResult<PythonDataStructure> {
        if (dict.values == null) {
            return DataFrame(null).withWarn("Unable to rename dataframe because the values of dictionary are unkown")
        }
        if (dataFrame.columns == null) {
            return DataFrame(null).withWarn("Unable to rename dataframe with unknown structure")
        }

        // check that the old values in the mapping dict are all strings
        val nonStringOldValues = dict.values.filterKeys { (it is PythonString).not() }.keys
        if (nonStringOldValues.isNotEmpty()) {
            return fail("The old column names should be strings, but were $nonStringOldValues")
        }

        // check that the new values in the mapping dict are all strings
        val nonStringNewValues = dict.values.filterValues { (it is PythonString).not() }.values
        if (nonStringNewValues.isNotEmpty()) {
            return fail("the new column names should be strings, but were $nonStringNewValues")
        }

        // check that the old and new values in the mapping dict are all known
        val nullMapping = dict.values.map { (it.key as PythonString).value to (it.value as PythonString).value }
        if (nullMapping.any { it.first == null || it.second == null }) {
            return DataFrame(null).withWarn("Unable to resolve some mapping parts of rename function")
        }
        val mapping = nullMapping.associate { it.first!! to it.second!! }

        // check that all the old values exist in the dataframe
        val missingOldValues = mapping.keys.filterNot { it in dataFrame.columns }
        if (missingOldValues.isNotEmpty()) {
            return fail("The values $missingOldValues do not exist in the dataframe")
        }

        // check that the new values are not colliding with any old values
        val collidingValues = mapping.filter { it.value in dataFrame.columns }
        if (collidingValues.isNotEmpty()) {
            val message =
                collidingValues
                    .map {
                        "Cannot rename a dataframe column ${it.key} to ${it.value} " +
                            "since ${it.value} already exists in the dataframe"
                    }
                    .joinToString("\n")
            return fail(message)
        }

        // check that there are no duplicate new values
        val duplicateValues = mapping.values.groupBy { it }.filterValues { it.size > 1 }
        if (duplicateValues.isNotEmpty()) {
            return fail("There are duplicate new values in the rename function: ${duplicateValues.keys}")
        }

        return DataFrame(columns = dataFrame.columns.mapKeys { mapping[it.key] ?: it.key }.toMutableMap()).ok()
    }

    private val argumentSchema =
        ResolvedArguments(
            arguments = listOf(PythonEntity.Arg("columns")), // todo the structure is bigger
            defaults = listOf(PythonNone),
        )
}
