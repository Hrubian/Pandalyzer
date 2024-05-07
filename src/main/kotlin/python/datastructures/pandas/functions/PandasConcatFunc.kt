package python.datastructures.pandas.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonEntity
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonInt
import python.datastructures.defaults.PythonList
import python.datastructures.invokeNondeterministic
import python.datastructures.pandas.dataframe.DataFrame
import python.datastructures.pandas.series.Series
import python.fail
import python.map
import python.ok
import python.withWarn
import java.math.BigInteger

object PandasConcatFunc : PandasFunction {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        invokeNondeterministic(args, keywordArgs, outerContext) { iArgs, kArgs, ctx -> invokeInner(iArgs, kArgs, ctx) }

    private fun invokeInner(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        ArgumentMatcher.match(functionSchema, args, keywordArgs.toMap()).map {
            val objects =
                it.matchedArguments["objs"] as? PythonList
                    ?: return@map fail("The first argument of concat function should be a list")
            val axis =
                it.matchedArguments["axis"] as? PythonInt
                    ?: return@map fail("The axis argument of concat function must be an integer")
            return@map concat(objects, axis)
        }

    private fun concat(
        objects: PythonList,
        axis: PythonInt,
    ): OperationResult<PythonDataStructure> {
        if (axis.value == null) {
            return NondeterministicDataStructure(DataFrame(null), Series(null))
                .withWarn("Unable to resolve the 'axis' argument of concat function")
        }

        if (axis.value != BigInteger.ZERO && axis.value != BigInteger.ONE) {
            return fail("The 'axis' argument of concat function must be either a zero or a one")
        }

        if (objects.items == null) {
            return NondeterministicDataStructure(DataFrame(null), Series(null))
                .withWarn("Unable to resolve the objects to concatenate")
        }
        if (objects.items.isEmpty()) {
            return fail("Concat function with no object")
        }

        when {
            objects.items.all { it is Series } -> {
                val allSeries = objects.items.map { it as Series }
                val unknownSeries = allSeries.filter { it.type == null }
                if (unknownSeries.isNotEmpty()) {
                    return Series(null).withWarn(
                        "Unable to resolve the result of " +
                            "concatenation as some of the series have unknown type",
                    )
                }
                when (axis.value) {
                    BigInteger.ZERO -> {
                        val types = unknownSeries.map { it.type }.distinct()
                        if (types.size != 1) {
                            return fail("All concatenated series must have the same type. Types: $types")
                        }
                        return Series(types.single()).ok()
                    }
                    BigInteger.ONE -> {
                        var substituteIndex = 0
                        return DataFrame(
                            columns =
                                allSeries.associate { (it.label ?: substituteIndex++.toString()) to it.type!! }
                                    .toMutableMap(),
                        ).ok()
                    }
                    else -> error("does not happen")
                }
            }
            objects.items.all { it is DataFrame } -> {
                val allDataFrames = objects.items.map { it as DataFrame }
                val unknownDataFrames = allDataFrames.filter { it.columns == null }
                if (unknownDataFrames.isNotEmpty()) {
                    return DataFrame(null).withWarn(
                        "Unable to resolve the result of " +
                            "concatenation as some of the dataframes have unknown type",
                    )
                }
                when (axis.value) {
                    BigInteger.ZERO -> {
                        if (allDataFrames.all { haveEqualStructure(it, allDataFrames.first()) }.not()) {
                            return fail("All dataframes to be concatenated must have the same column structure")
                        }
                        return allDataFrames.first().clone().ok()
                    }
                    BigInteger.ONE -> {
                        val duplicateNames =
                            allDataFrames.flatMap { it.columns!!.keys }
                                .groupingBy { it }.eachCount().filter { it.value > 1 }.keys
                        if (duplicateNames.isNotEmpty()) {
                            return fail("The dataframes to be concatenated have the following common columns: $duplicateNames")
                        }
                        val resultFields = mutableMapOf<FieldName, FieldType>()
                        allDataFrames.forEach { resultFields.putAll(it.columns!!) }
                        return DataFrame(resultFields).ok()
                    }
                    else -> error("does not happen")
                }
            }
            else -> return fail("The objects to concatenate must be either all series or all dataframes")
        }
    }

    private val functionSchema =
        ResolvedArguments(
            positionalArgs = listOf(PythonEntity.Arg("objs")),
            arguments = listOf(PythonEntity.Arg("axis")),
            defaults = listOf(PythonInt(BigInteger.ZERO)),
        )

    private fun haveEqualStructure(
        df1: DataFrame,
        df2: DataFrame,
    ): Boolean = df1.columns!!.all { df2.columns!![it.key] == it.value } && df2.columns!!.all { df1.columns[it.key] == it.value }
}
