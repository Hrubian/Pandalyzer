package python.datastructures.pandas.dataframe.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.DataFrame
import python.datastructures.pandas.DataFrameFunction
import python.fail

data class DataFrame_MergeFunc(override val dataFrame: DataFrame) : DataFrameFunction {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext
    ): OperationResult<PythonDataStructure> {
        // todo pair args

        return merge(dataFrame, )
    }

    private fun merge(
        left: PythonDataStructure,
        right: PythonDataStructure,
        how: PythonDataStructure,
        on: PythonDataStructure,
        leftOn: PythonDataStructure,
        rightOn: PythonDataStructure,
    ): OperationResult<PythonDataStructure> {
        val leftDf = left as? DataFrame ?: return fail("todo")
        val rightDf = right as? DataFrame ?: return fail("todo")
        val howString = how as? PythonString ?: return fail("todo")
    }


}