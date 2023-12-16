package python.datastructures

import analyzer.Identifier
import python.datastructures.pandas.FieldName
import python.OperationResult
import python.fail
import kotlin.reflect.KClass

interface PythonDataStructure {
    val typeName: KClass<PythonDataStructure>
        get() = javaClass.kotlin

    fun index(key: Identifier): OperationResult<PythonDataStructure> =
        fail("Cannot apply one-key indexing on $typeName")
    fun indexList(keys: List<Identifier>): OperationResult<PythonDataStructure> =
        fail("Cannot apply list indexing on $typeName")
    fun indexStruct(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot apply ${struct.typeName} indexing of $typeName")
    fun concat(struct: PythonDataStructure, axis: Int): OperationResult<PythonDataStructure> =
        fail("Cannot concatenate $typeName with $typeName")
    fun groupBy(key: Identifier): OperationResult<PythonDataStructure> =
        fail("Cannot apply one-key groupBy operation on $typeName")
    fun groupBy(keys: List<Identifier>): OperationResult<PythonDataStructure> =
        fail("Cannot apply list groupBy operation on $typeName")
    fun mergeWith(struct: PythonDataStructure, how: String, on: FieldName?, leftOn: FieldName?, rightOn: FieldName?): OperationResult<PythonDataStructure> =
        fail("Cannot merge $typeName with ${struct.typeName}")
    fun mean(): OperationResult<PythonDataStructure> =
        fail("Cannot calculate mean of type $typeName")
    fun sortValues(by: Identifier, ascending: Boolean): OperationResult<PythonDataStructure> =
        fail("Cannot sort values of $typeName by key $by")
    fun sumWith(struct: PythonDataStructure): OperationResult<PythonDataStructure> =
        fail("Cannot sum $typeName with ${struct.typeName}")
}
