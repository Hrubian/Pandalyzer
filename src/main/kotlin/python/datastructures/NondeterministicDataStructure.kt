package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.addWarnings
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

    override fun clone(): PythonDataStructure = NondeterministicDataStructure(left.clone(), right.clone())

    override fun boolValue(): Boolean? {
        val l = left.boolValue() ?: return null
        val r = right.boolValue() ?: return null
        return if (l == r) l else null
    }

    companion object {
        fun combineResults(
            result1: OperationResult<PythonDataStructure>,
            result2: OperationResult<PythonDataStructure>,
        ): OperationResult<PythonDataStructure> =
            when {
                result1 is OperationResult.Ok && result2 is OperationResult.Ok ->
                    NondeterministicDataStructure(result1.result, result2.result).ok()
                result1 is OperationResult.Ok && result2 is OperationResult.Warning ->
                    NondeterministicDataStructure(result1.result, result2.result).ok(result2.messages)
                result1 is OperationResult.Warning && result2 is OperationResult.Ok ->
                    NondeterministicDataStructure(result1.result, result2.result).ok(result1.messages)
                result1 is OperationResult.Warning && result2 is OperationResult.Warning ->
                    NondeterministicDataStructure(result1.result, result2.result)
                        .ok(result1.messages + result2.messages)
                result1 is OperationResult.Error && result2 is OperationResult.Error ->
                    fail("Both execution branches failed. Branch1: ${result1.reason}, Branch2: ${result2.reason}")
                result1 is OperationResult.Error && result2 is OperationResult.Ok ->
                    result2.result.withWarn("First branch of execution failed with reason: ${result1.reason}")
                result1 is OperationResult.Ok && result2 is OperationResult.Error ->
                    result1.result.withWarn("Second branch of execution failed with reason: ${result2.reason}")
                result1 is OperationResult.Error && result2 is OperationResult.Warning ->
                    result2.result.withWarn("First branch of execution failed with reason: ${result1.reason}")
                        .addWarnings(result2.messages)
                result1 is OperationResult.Warning && result2 is OperationResult.Error ->
                    result1.result.withWarn("Second branch of execution failed with reason: ${result2.reason}")
                        .addWarnings(result1.messages)
                else -> error("The kotlin compiler is a bit dumb I guess :(")
            }
    }
}

inline fun PythonDataStructure.nonDeterministically(
    block: (value: PythonDataStructure) -> OperationResult<PythonDataStructure>,
): OperationResult<PythonDataStructure> =
    when (this) {
        is NondeterministicDataStructure -> NondeterministicDataStructure.combineResults(block(this.left), block(this.right))
        else -> block(this)
    }
