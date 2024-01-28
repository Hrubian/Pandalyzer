package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonType
import python.fail
import kotlin.reflect.KClass

sealed interface PythonDataStructure {
    val typeName: KClass<PythonDataStructure>
        get() = javaClass.kotlin

    //old implementation
//    fun index(key: Identifier): OperationResult<PythonDataStructure> =
//        fail("Cannot apply one-key indexing on $typeName")
//    fun indexList(keys: List<Identifier>): OperationResult<PythonDataStructure> =
//        fail("Cannot apply list indexing on $typeName")
//    fun indexStruct(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
//        fail("Cannot apply ${struct.typeName} indexing of $typeName")
//    fun concat(struct: PythonDataStructure, axis: Int): OperationResult<PythonDataStructure> =
//        fail("Cannot concatenate $typeName with $typeName")
//    fun groupBy(key: Identifier): OperationResult<PythonDataStructure> =
//        fail("Cannot apply one-key groupBy operation on $typeName")
//    fun groupBy(keys: List<Identifier>): OperationResult<PythonDataStructure> =
//        fail("Cannot apply list groupBy operation on $typeName")
//    fun mergeWith(struct: PythonDataStructure, how: String, on: FieldName?, leftOn: FieldName?, rightOn: FieldName?): OperationResult<PythonDataStructure> =
//        fail("Cannot merge $typeName with ${struct.typeName}")
//    fun mean(): OperationResult<PythonDataStructure> =
//        fail("Cannot calculate mean of type $typeName")
//    fun sortValues(by: Identifier, ascending: Boolean): OperationResult<PythonDataStructure> =
//        fail("Cannot sort values of $typeName by key $by")
//    fun sumWith(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
//        fail("Cannot sum $typeName with ${struct.typeName}")
//    fun multiplyWith(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
//        fail("Cannot multiply $typeName with ${struct.typeName}")
//    fun divideBy(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
//        fail("Cannot divide $typeName by ${struct.typeName}")
//    fun subtract(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
//        fail ("Cannot ${struct.typeName} from $typeName")

    // new implementation
    fun subscript(key: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot subscript a value of type $typeName")

    operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot add a value of type $typeName")
    operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot subtract from a value of type $typeName")
    operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot multiply a value of type $typeName")
    operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot divide a value of type $typeName")

    fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        fail("the attribute $identifier of $typeName does not exist")

    fun callWithArgs(args: List<PythonDataStructure>, outerContext: AnalysisContext): OperationResult<PythonDataStructure> =
        fail("The $typeName is not callable")

}
