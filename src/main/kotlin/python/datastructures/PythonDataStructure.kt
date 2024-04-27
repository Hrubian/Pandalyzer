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
}
