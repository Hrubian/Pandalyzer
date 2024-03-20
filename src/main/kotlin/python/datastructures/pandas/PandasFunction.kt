package python.datastructures.pandas

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.dataframe.DataFrame
import python.datastructures.pandas.series.Series
import python.fail
import python.ok

interface PandasFunction : PythonDataStructure {
    object SeriesFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")
    }

    object ConcatFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            return TODO()
        }

        private fun concat(objects: List<Series>): OperationResult<PythonDataStructure> {
            // todo there is a non-trivial index logic
            val differentTypes = objects.map { it.type }.distinct()
            return when (differentTypes.size) {
                0 -> fail("") // todo
                1 -> Series(type = differentTypes.first()).ok()
                else -> fail("Cannot concatenate series of different types. The types in series: $differentTypes")
            }
        }

        private fun concat(
            objects: List<DataFrame>,
            join: String = "",
        ): OperationResult<PythonDataStructure> {
            TODO()
        }
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
        ): OperationResult<PythonDataStructure> = fail("not implemented")
    }
}
