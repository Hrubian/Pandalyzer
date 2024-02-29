package python.datastructures.pandas

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure

interface PandasFunc : PythonDataStructure {
    object MergeFunc : PandasFunc {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
        }
    }

    object DataFrameFunc : PandasFunc {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
        }

        // we want to support also constructs like: pd.DataFrame.from_dict(...)
        override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> {
        }
    }

    object SeriesFunc : PandasFunc {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
        }
    }

    object ConcatFunc : PandasFunc {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
        }
    }

    object GroupByFunc : PandasFunc {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
        }
    }

    object ReadCsvFunc : PandasFunc {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
        }
    }
}
