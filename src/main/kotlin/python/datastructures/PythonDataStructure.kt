package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.fail

interface PythonDataStructure {
    val typeName: String
        get() = javaClass.simpleName

    fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot subscript a value of type $typeName")

    operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot add a value of type $typeName")

    operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot subtract from a value of type $typeName")

    operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot multiply a value of type $typeName")

    operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot divide a value of type $typeName")

    infix fun floorDiv(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot floor-divide a value of type $typeName")

    fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        fail("the attribute $identifier of $typeName does not exist")

    fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> = fail("The $typeName is not callable")

    fun clone(): PythonDataStructure

    fun boolValue(): Boolean? = null

    fun negate(): OperationResult<PythonDataStructure> = fail("Cannot negate $typeName")

    fun positive(): OperationResult<PythonDataStructure> = fail("Cannot apply unary plus on $typeName")

    infix fun equal(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform equal on $typeName and ${other.typeName}")

    infix fun greaterThan(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform greaterThan on $typeName and ${other.typeName}")

    infix fun greaterThanEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform greaterThanEqual on $typeName and ${other.typeName}")

    infix fun inn(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform in on $typeName and ${other.typeName}")

    infix fun iss(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform is on $typeName and ${other.typeName}")

    infix fun isNot(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform isNot on $typeName and ${other.typeName}")

    infix fun lessThan(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform lessThan on $typeName and ${other.typeName}")

    infix fun lessThanEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform lessThanEqual on $typeName and ${other.typeName}")

    infix fun notEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform notEqual on $typeName and ${other.typeName}")

    infix fun notIn(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot perform notIn on $typeName and ${other.typeName}")

    fun storeAttribute(
        attribute: Identifier,
        value: PythonDataStructure,
    ): OperationResult<PythonDataStructure> = fail("Cannot store attribute on type $typeName")

    fun storeSubscript(
        slice: PythonDataStructure,
        value: PythonDataStructure,
    ): OperationResult<PythonDataStructure> = fail("Cannot subscript-assing on type $typeName")
}

fun invokeNondeterministic(
    args: List<PythonDataStructure>,
    keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
    outerContext: AnalysisContext,
    block: (
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ) -> OperationResult<PythonDataStructure>,
): OperationResult<PythonDataStructure> {
    var result: OperationResult<PythonDataStructure>? = null
    sequence { generateNondeterministicArgs(emptyList(), emptyList(), args, keywordArgs) }.forEach { (args, kwArgs) ->
        if (result == null) {
            result = block(args, kwArgs, outerContext)
        } else {
            result = NondeterministicDataStructure.combineResults(result!!, block(args, kwArgs, outerContext))
        }
    }
    if (result == null) {
        result = block(args, keywordArgs, outerContext)
    }
    return result!!
}

fun PythonDataStructure.nondeterministically(block: () -> OperationResult<PythonDataStructure>): OperationResult<PythonDataStructure> =
    when (this) {
        is NondeterministicDataStructure -> {
            NondeterministicDataStructure.combineResults(
                result1 = left.nondeterministically(block),
                result2 = right.nondeterministically(block),
            )
        }
        else -> block()
    }

private suspend fun SequenceScope<
    Pair<
        List<PythonDataStructure>,
        List<Pair<Identifier, PythonDataStructure>>,
        >,
    >.generateNondeterministicArgs(
    argsSoFar: List<PythonDataStructure>,
    keywordArgsSoFar: List<Pair<Identifier, PythonDataStructure>>,
    args: List<PythonDataStructure>,
    keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
) {
    if (args.isNotEmpty()) {
        val head = args.first()
        if (head is NondeterministicDataStructure) {
            generateNondeterministicArgs(argsSoFar + head.left, keywordArgsSoFar, args.drop(1), keywordArgs)
            generateNondeterministicArgs(argsSoFar + head.right, keywordArgsSoFar, args.drop(1), keywordArgs)
        } else {
            generateNondeterministicArgs(argsSoFar + head, keywordArgsSoFar, args.drop(1), keywordArgs)
        }
    } else if (keywordArgs.isNotEmpty()) {
        val head = keywordArgs.first()
        if (head.second is NondeterministicDataStructure) {
            val left = head.first to (head.second as NondeterministicDataStructure).left
            generateNondeterministicArgs(argsSoFar, keywordArgsSoFar + left, args, keywordArgs.drop(1))
            val right = head.first to (head.second as NondeterministicDataStructure).right
            generateNondeterministicArgs(argsSoFar, keywordArgsSoFar + right, args, keywordArgs.drop(1))
        }
    } else {
        yield(argsSoFar to keywordArgsSoFar)
    }
}
