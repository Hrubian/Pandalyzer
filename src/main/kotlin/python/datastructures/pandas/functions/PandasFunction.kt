package python.datastructures.pandas.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.DataFrame
import python.datastructures.pandas.series.Series
import python.fail
import python.ok
import python.withWarn

interface PandasFunction : PythonDataStructure {
    override fun clone(): PythonDataStructure = this

    object SeriesFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")
    }

    object GroupByFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")
    }

    object ReadCsvFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (keywordArgs.isNotEmpty()) {
                return fail("Unexpected keyword argument to read_csv: ${keywordArgs.first().first}")
            }
            val filename = args.singleOrNull() ?: return fail("Filename for read_csv not provided")
            return if (filename is PythonString) {
                readCsv(filename, outerContext)
            } else {
                fail("The provided filename should be a string but was a ${filename.typeName}")
            }
        }

        private fun readCsv(
            filename: PythonString,
            context: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            return if (filename.value == null) {
                DataFrame(null).withWarn("Unable to resolve filename of unknown string for read_csv")
            } else {
                context.getDataframeFromMetadata(filename.value)?.ok()
                    ?: fail("Unknown file with name: ${filename.value}")
            }
        }
    }
}
