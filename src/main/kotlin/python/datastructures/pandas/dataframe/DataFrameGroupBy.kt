package python.datastructures.pandas.dataframe

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonInvokable
import python.fail
import python.ok
import python.withWarn

data class DataFrameGroupBy(
    private val dataFrame: DataFrame?,
    private val by: MutableList<FieldName?>?,
) : PythonDataStructure {
    override fun clone(): PythonDataStructure = DataFrameGroupBy(dataFrame = dataFrame?.clone() as DataFrame, by = by?.toMutableList())

    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "mean" -> Mean(this).ok()
            "sum" -> Sum(this).ok()
            "first" -> First(this).ok()
            "last" -> Last(this).ok()
            "head" -> Head(this).ok()
            "count" -> Count(this).ok()
            else -> fail("Unknown attribute $identifier on DataFrameGroupBy object")
        }

    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> {
        TODO()
//        when (key) {
//            is PythonString -> { // returns SeriesGroupBy
//                if (key.value == null) {
//                    return SeriesGroupBy(null, null).withWarn("") //todo
//                }
//                if (dataFrame == null) {
//                    return SeriesGroupBy(null, null).withWarn("")
//                }
//                if (by == null) {
//                    return SeriesGroupBy(null, null).withWarn("")
//                }
//                if (dataFrame.fields == null) {
//                    return SeriesGroupBy(null, null).withWarn("")
//                }
//
//                return SeriesGroupBy()
//            }
//            is PythonList -> { // returns DataframeGroupBy
//
//            }
//        }
    }

    data class Mean(private val dfGroupBy: DataFrameGroupBy) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (dfGroupBy.by == null) {
                return DataFrame(null).withWarn("Cannot resolve 'by' of groupby -> not able to check mean operation")
            }
            val nonNumerics =
                dfGroupBy.dataFrame?.columns
                    ?.filter { it.key !in dfGroupBy.by }
                    ?.filter { it.value !in setOf(FieldType.IntType, FieldType.FloatType) }
                    ?: return dfGroupBy.dataFrame?.clone()?.ok() ?: return DataFrame(null).ok() // todo mesage?
            return if (nonNumerics.isNotEmpty()) {
                val message =
                    StringBuilder().apply {
                        append("Cannot apply mean on the columns: ")
                        nonNumerics.forEach { append("${it.key} of type ${it.value.name}, ") }
                    }.toString()
                fail(message)
            } else {
                dfGroupBy.dataFrame.clone().ok()
            }
        }
    }

    data class Sum(private val dfGroupBy: DataFrameGroupBy) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (dfGroupBy.by == null) {
                return DataFrame(null).withWarn("Cannot resolve 'by' of groupby -> not able to check sum operation")
            }
            val nonSummable = //wrong
                dfGroupBy.dataFrame?.columns
                    ?.filter { it.key !in dfGroupBy.by }
                    ?.filter { it.value !in setOf(FieldType.IntType, FieldType.FloatType, FieldType.StringType) }
                    ?: return dfGroupBy.dataFrame?.clone()?.ok() ?: return DataFrame(null).ok() // todo mesage?
            return if (nonSummable.isNotEmpty()) {
                val message =
                    StringBuilder().apply {
                        append("Cannot apply sum on the columns: ")
                        nonSummable.forEach { append("${it.key} of type ${it.value.name}, ") }
                    }.toString()
                fail(message)
            } else {
                dfGroupBy.dataFrame.clone().ok()
            }
        }
    }

    data class First(private val dfGroupBy: DataFrameGroupBy) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (dfGroupBy.by == null) {
                return DataFrame(null).withWarn("Cannot resolve 'by' of groupby -> not able to check first operation")
            }
            val nonNumerics =
                dfGroupBy.dataFrame?.columns
                    ?.filter { it.key !in dfGroupBy.by }
                    ?.filter { it.value !in setOf(FieldType.IntType, FieldType.FloatType) }
                    ?: return dfGroupBy.dataFrame?.clone()?.ok() ?: return DataFrame(null).ok() // todo mesage?
            return if (nonNumerics.isNotEmpty()) {
                val message =
                    StringBuilder().apply {
                        append("Cannot apply first on the columns: ")
                        nonNumerics.forEach { append("${it.key} of type ${it.value.name}, ") }
                    }.toString()
                fail(message)
            } else {
                dfGroupBy.dataFrame.clone().ok()
            }
        }
    }

    data class Last(private val dfGroupBy: DataFrameGroupBy) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (dfGroupBy.by == null) {
                return DataFrame(null).withWarn("Cannot resolve 'by' of groupby -> not able to check last operation")
            }
            val nonNumerics =
                dfGroupBy.dataFrame?.columns
                    ?.filter { it.key !in dfGroupBy.by }
                    ?.filter { it.value !in setOf(FieldType.IntType, FieldType.FloatType) }
                    ?: return dfGroupBy.dataFrame?.clone()?.ok() ?: return DataFrame(null).ok() // todo mesage?
            return if (nonNumerics.isNotEmpty()) {
                val message =
                    StringBuilder().apply {
                        append("Cannot apply last on the columns: ")
                        nonNumerics.forEach { append("${it.key} of type ${it.value.name}, ") }
                    }.toString()
                fail(message)
            } else {
                dfGroupBy.dataFrame.clone().ok()
            }
        }
    }

    data class Head(private val dfGroupBy: DataFrameGroupBy) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (dfGroupBy.by == null) {
                return DataFrame(null).withWarn("Cannot resolve 'by' of groupby -> not able to check last operation")
            }
            return (dfGroupBy.dataFrame?.clone() ?: DataFrame(null)).ok() //todo is this correct?
        }
    }

    data class Count(private val dfGroupBy: DataFrameGroupBy) : PythonInvokable {
        override fun invoke(
            args: List<PythonDataStructure>,
            keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
            outerContext: AnalysisContext,
        ): OperationResult<PythonDataStructure> {
            if (args.isNotEmpty() || keywordArgs.isNotEmpty()) {
                return fail("The count function does not accept any arguments")
            }
            if (dfGroupBy.by == null) {
                return DataFrame(null).withWarn("Cannot resolve 'by' of groupby -> not able to check count operation")
            }
            if (dfGroupBy.dataFrame?.columns == null) {
                return DataFrame(null).withWarn("Cannot resolve dataframe of groupby -> unable to check count operation")
            }
            return DataFrame(columns = dfGroupBy.dataFrame.columns.map { (columnName, columnType) ->
                columnName to if (columnName in dfGroupBy.by)  columnType else FieldType.IntType
            }.toMap().toMutableMap()).ok()
        }
    }
}
