package python.datastructures.pandas

import analyzer.Identifier
import python.OperationResult
import python.datastructures.ImportStruct
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

data object PandasImport : ImportStruct {

    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "merge" -> PandasFunc.MergeFunc.ok()
            "DataFrame" -> PandasFunc.DataFrameFunc.ok()
            "Series" -> PandasFunc.SeriesFunc.ok()
            "concat" -> PandasFunc.ConcatFunc.ok()
            "groupby" -> PandasFunc.GroupByFunc.ok()
            "read_csv" -> PandasFunc.GroupByFunc.ok()
            else -> fail("Unknown pandas identifier $identifier.")
        }
}

