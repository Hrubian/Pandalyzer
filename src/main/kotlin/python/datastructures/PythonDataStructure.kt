package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.fail

interface PythonDataStructure {
    val typeCode: String
        get() = javaClass.simpleName

    fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot subscript a value of type $typeCode")

    operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot add a value of type $typeCode")

    operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot subtract from a value of type $typeCode")

    operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot multiply a value of type $typeCode")

    operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> = fail("Cannot divide a value of type $typeCode")

    fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        fail("the attribute $identifier of $typeCode does not exist")

    fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> = fail("The $typeCode is not callable")

    fun clone(): PythonDataStructure

    fun boolValue(): Boolean? = null
}
