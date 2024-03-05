package python.datastructures.pandas

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail

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
        override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> = fail("not implemented")

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
        ): OperationResult<PythonDataStructure> = fail("not implemented")

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
