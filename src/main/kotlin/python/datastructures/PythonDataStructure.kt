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

    infix fun and(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot do 'and' on a value of type $typeName")

    infix fun or(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot do 'or' on a value of type $typeName")

    fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        fail("the attribute $identifier of $typeName does not exist")

    fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> = fail("The $typeName is not callable")
}
