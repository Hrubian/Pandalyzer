package python.datastructures.pandas

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonString
import python.fail
import python.ok

interface DataFrameFunction : PythonDataStructure {
    val dataFrame: DataFrame

    data class GroupByFunc(override val dataFrame: DataFrame) : DataFrameFunction {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext
        ): OperationResult<PythonDataStructure> {
            //todo pair args

            return groupBy(dataFrame, , )
        }


        private fun groupBy(
            dataFrame: DataFrame,
            by: PythonString
        ): OperationResult<PythonDataStructure> {
            return if (by.value in dataFrame.fields) {
                DataFrameGroupBy(dataFrame, listOf(by.value)).ok()
            } else {
                fail("The dataframe does not contain the field $by. Dataframe columns: ${dataFrame.fields.keys}")
            }
        }

        private fun groupBy(
            dataFrame: DataFrame,
            by: PythonList
        ): OperationResult<PythonDataStructure> {
            val keys = by.items.map { it as? PythonString ?: return fail("Cannot group a dataframe by ${it.typeName}") }

            val missingKeys = keys.filterNot { it.value in dataFrame.fields }
            if (missingKeys.isNotEmpty()) {
                return fail(
                    "Cannot group by keys $missingKeys, since they were not found in the dataframe. " +
                            "Dataframe columns: ${dataFrame.fields.keys}"
                )
            }

            return DataFrameGroupBy(dataFrame, by.items.map { (it as PythonString).value }).ok()
        }
    }

    data class MergeFunc(override val dataFrame: DataFrame) : DataFrameFunction {
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
}
