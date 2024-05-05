package python.datastructures.pandas

import analyzer.Identifier
import python.OperationResult
import python.datastructures.ImportStruct
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.functions.PandasConcatFunc
import python.datastructures.pandas.functions.PandasDataframeFunc
import python.datastructures.pandas.functions.PandasFunction
import python.datastructures.pandas.functions.PandasMergeFunc
import python.fail
import python.ok

data object PandasImport : ImportStruct {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "merge" -> PandasMergeFunc.ok()
            "DataFrame" -> PandasDataframeFunc.ok()
            "Series" -> PandasFunction.SeriesFunc.ok()
            "concat" -> PandasConcatFunc.ok()
            "groupby" -> PandasFunction.GroupByFunc.ok()
            "read_csv" -> PandasFunction.ReadCsvFunc.ok()
            else -> fail("Unknown pandas identifier $identifier.")
        }

    override fun clone(): PythonDataStructure = this
}
