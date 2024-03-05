package python.datastructures.pandas

import analyzer.AnalysisContext
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.PythonList
import python.datastructures.PythonNone
import python.datastructures.PythonString
import python.fail
import python.ok

interface DataFrameFunction : PythonDataStructure {
    val dataFrame: DataFrame

    data class GroupByFunc(override val dataFrame: DataFrame) : DataFrameFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            //todo pair args

            return groupBy(dataFrame, , )
        }


        private fun groupBy(
            dataframe: PythonDataStructure,
            by: PythonDataStructure
        ): OperationResult<PythonDataStructure> {
            val df = dataframe as? DataFrame ?: return fail("todo")

            return when (by) {
                is PythonString -> {
                    //todo check
                    DataFrameGroupBy(df, listOf(by.value)).ok()
                }
                is PythonList -> {
                    //todo check
                    val byList = by.items.map { (it as? PythonString)?.value ?: return fail("todo") }
                    DataFrameGroupBy(df, byList).ok()
                }
                is PythonNone -> {

                }
                else -> {
                    fail("")
                }
            }
        }
    }

    data class MergeFunc(override val dataFrame: DataFrame) : DataFrameFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            // todo pair args

            return merge(dataFrame, )
        }

        private fun merge(
            left: PythonDataStructure,
            right: PythonDataStructure,
            how: PythonDataStructure,
            leftOn: PythonDataStructure,
            rightOn: PythonDataStructure,
        ): OperationResult<PythonDataStructure> {
            val leftDf = left as? DataFrame ?: return fail("todo")
            val rightDf = right as? DataFrame ?: return fail("todo")
            val howString = how as? PythonString ?: return fail("todo")
        }
    }
}
