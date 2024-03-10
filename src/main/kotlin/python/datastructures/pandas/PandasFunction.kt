package python.datastructures.pandas

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

interface PandasFunction : PythonDataStructure {
    object MergeFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")

    }

    object DataFrameFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")


        // we want to support also constructs like: pd.DataFrame.from_dict(...)
        override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
            when(identifier) {
                "from_dict" -> FromDictFunc.ok()
                else -> fail("Unknown identifier $identifier")
            }

        object FromDictFunc : PandasFunction {

        }

    }

    object SeriesFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")

    }

    object ConcatFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {

            return concat()
        }

        private fun concat(
            objects: List<Series>,
        ): OperationResult<PythonDataStructure> {
            //todo there is a non-trivial index logic
            val differentTypes = objects.map { it.type }.distinct()
            return when (differentTypes.size) {
                0 -> fail("") //todo
                1 -> Series(type = differentTypes.first()).ok()
                else -> fail("Cannot concatenate series of different types. The types in series: $differentTypes")
            }
        }

        private fun concat(
            objects: List<DataFrame>,
            join: String = ""
        ): OperationResult<PythonDataStructure> {

        }
    }

    object GroupByFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")
    }

    object ReadCsvFunc : PandasFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> = fail("not implemented")
    }
}
