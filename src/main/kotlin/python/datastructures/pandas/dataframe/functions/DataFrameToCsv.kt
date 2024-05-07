package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.invokeNondeterministic
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.withWarn

data class DataFrameToCsv(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun clone(): PythonDataStructure = this

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
    ): OperationResult<PythonDataStructure> {
        if (keywordArgs.isNotEmpty()) {
            return fail("Unexpected keyword argument to read_csv: ${keywordArgs.first().first}")
        }
        val filename = args.singleOrNull() ?: return fail("Filename for read_csv not provided")
        return if (filename is PythonString) {
            toCsv(filename, outerContext)
        } else {
            fail("The provided filename should be a string but was a ${filename.typeName}")
        }
    }

    private fun toCsv(
        filename: PythonString,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        if (filename.value == null) {
            PythonNone.withWarn("Unable to store a dataframe to a file since the filename is not known")
        } else {
            context.storeDataframeToMetadata(filename.value, dataFrame)
        }
}
