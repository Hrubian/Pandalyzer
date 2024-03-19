package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.fail
import python.ok
import python.withWarn

data class NondeterministicDataStructure(
    val left: PythonDataStructure,
    val right: PythonDataStructure,
) : PythonDataStructure {
    override fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.subscript(key), right.subscript(key))

    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.plus(other), right.plus(other))

    override operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.minus(other), right.minus(other))

    override operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.times(other), right.times(other))

    override operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.div(other), right.div(other))

    override infix fun and(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.and(other), right.and(other))

    override infix fun or(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        combineResults(left.or(other), right.or(other))

    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        combineResults(left.attribute(identifier), right.attribute(identifier))

    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        combineResults(
            result1 = left.invoke(args, keywordArgs, outerContext),
            result2 = right.invoke(args, keywordArgs, outerContext),
        )

    private fun combineResults(
        result1: OperationResult<PythonDataStructure>,
        result2: OperationResult<PythonDataStructure>,
    ): OperationResult<PythonDataStructure> =
        when {
            result1 is OperationResult.Ok && result2 is OperationResult.Ok ->
                NondeterministicDataStructure(result1.result, result2.result).ok()
            result1 is OperationResult.Ok ->
                result1.result.withWarn("TODO combineResults")
            result2 is OperationResult.Ok ->
                result2.result.withWarn("TODO")
            result1 is OperationResult.Warning && result2 is OperationResult.Warning ->
                NondeterministicDataStructure(result1.result, result2.result).withWarn("todo")
            result1 is OperationResult.Warning ->
                result1
            result2 is OperationResult.Warning ->
                result2
            result1 is OperationResult.Error && result2 is OperationResult.Error -> fail("todo")
            else -> error("The kotlin compiler is a bit dumb I guess :(")
        }
}
