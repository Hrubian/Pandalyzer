package python.datastructures.defaults

import python.OperationResult
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.ok
import python.withWarn
import java.math.BigInteger

@JvmInline
value class PythonBool(val value: Boolean?) : PythonDataStructure {
//    override fun and(other: PythonDataStructure): OperationResult<PythonDataStructure> =
//        if (other is PythonBool) {
//            PythonBool(value && other.value).ok()
//        } else {
//
//            fail("Cannot do 'and' on $typeName and ${other.typeName}")
//        }
//
//    override fun or(other: PythonDataStructure): OperationResult<PythonDataStructure> =
//        if (other is PythonBool) {
//            PythonBool(value && other.value).ok()
//        } else {
//            fail("Cannot do 'or' on $typeName and ${other.typeName}")
//        }

    override fun negate(): OperationResult<PythonDataStructure> =
        when (value) {
            true -> PythonInt(-BigInteger.ONE).ok()
            false -> PythonInt(BigInteger.ZERO).ok()
            null ->
                NondeterministicDataStructure(PythonInt(-BigInteger.ONE), PythonInt(BigInteger.ZERO))
                    .withWarn("Unable to negate unknown bool")
        }

    override fun positive(): OperationResult<PythonDataStructure> =
        when (value) {
            true -> PythonInt(BigInteger.ONE).ok()
            false -> PythonInt(BigInteger.ZERO).ok()
            null ->
                NondeterministicDataStructure(PythonInt(BigInteger.ZERO), PythonInt(BigInteger.ONE))
                    .withWarn("Unable to apply unary plus on unknown bool")
        }

    override fun clone(): PythonDataStructure = PythonBool(value)

    override fun boolValue(): Boolean? = value
}
