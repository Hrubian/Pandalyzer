package python.datastructures.pandas

import analyzer.AnalysisContext
import python.OperationResult
import python.datastructures.PythonDataStructure

interface DataFrameFunctions : PythonDataStructure {
    val dataFrame: DataFrame

    data class GroupByFunc(override val dataFrame: DataFrame) : DataFrameFunctions {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext
        ): OperationResult<PythonDataStructure> {

        }
    }

    data class MergeFunc(override val dataFrame: DataFrame) : DataFrameFunctions {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext
        ): OperationResult<PythonDataStructure> {

        }
    }
}