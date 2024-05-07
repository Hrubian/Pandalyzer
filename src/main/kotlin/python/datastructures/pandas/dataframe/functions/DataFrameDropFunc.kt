package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonEntity
import python.addWarnings
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
import python.ok
import python.orElse
import python.withWarn

data class DataFrameDropFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun clone(): PythonDataStructure = this // todo

    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> = invokeNondeterministic(args, keywordArgs, outerContext) { arg, kw, _ -> invokeInner(arg, kw) }

    private fun invokeInner(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
    ): OperationResult<PythonDataStructure> {
        val (matchedArgs, warns) =
            ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap())
                .orElse { return fail(it) }
        val inplace =
            (matchedArgs.matchedArguments["inplace"] as? PythonBool)
                ?: return fail("The inplace argument of drop function should be a bool")
        val columns =
            matchedArgs.matchedArguments["columns"]
                ?: return fail("The columns argument of drop function was not provided")
        if (inplace.value == null) {
            return fail("Unable to perform drop operation as the inplace argument is not known")
        }

        return when (columns) {
            is PythonString -> drop(PythonList(mutableListOf(columns)), inplace.value)
            is PythonList -> drop(columns, inplace.value)
            else -> fail("The columns argument of drop function should be a string or a list, but was ${columns.typeName}")
        }.addWarnings(warns)
    }

    private fun drop(
        columns: PythonList,
        inplace: Boolean,
    ): OperationResult<PythonDataStructure> {
        if (columns.items == null) {
            return fail("The list of columns for drop function is not resolvable")
        }

        if (dataFrame.columns == null) {
            return fail("The dataframe structure is not known.")
        }

        val nonStrings = columns.items.filterNot { col -> col is PythonString }
        if (nonStrings.isNotEmpty()) {
            return fail("The columns list should contain only strings but contains also ${nonStrings.map { it.typeName }}")
        }

        val unknownStrings = columns.items.filter { col -> (col as PythonString).value == null }
        if (unknownStrings.isNotEmpty()) {
            return if (inplace) {
                fail("Unable to drop columns inplace as some of the columns are unknown")
            } else {
                DataFrame(null)
                    .withWarn("Unable to resolve structure of the drop result as some of the strings are null")
            }
        }

        val actualColumns = columns.items.map { (it as PythonString).value!! }
        val incorrectColumns = actualColumns.filterNot { it in dataFrame.columns }.toSet()
        if (incorrectColumns.isNotEmpty()) {
            return fail("The following columns are not present in the dataframe: $incorrectColumns")
        }
        return if (inplace) {
            actualColumns.forEach { dataFrame.columns.remove(it) }
            PythonNone.ok()
        } else {
            DataFrame(dataFrame.columns.filterNot { it.key in actualColumns }.toMutableMap()).ok()
        }
    }

    private val argumentSchema =
        ResolvedArguments(
            keywordOnlyArgs = listOf(PythonEntity.Arg("columns"), PythonEntity.Arg("inplace")),
            keywordDefaults = listOf(PythonNone, PythonBool(false)),
        )
}
