package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.fail
import python.ok
import java.math.BigInteger

@JvmInline
value class PythonInt(val value: BigInteger?) : PythonDataStructure {
    override fun clone(): PythonDataStructure = PythonInt(value)

    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt) {
            if (value == null || other.value == null) return PythonInt(null).ok()
            PythonInt(this.value + other.value).ok()
        } else {
            fail("Cannot sum a $typeCode with ${other.typeCode}")
        }
    }

    override operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt) {
            if (value == null || other.value == null) return PythonInt(null).ok()
            PythonInt(this.value - other.value).ok()
        } else {
            fail("Cannot subtract a ${other.typeCode} from $typeCode")
        }
    }

    override operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt) {
            if (value == null || other.value == null) return PythonInt(null).ok()
            PythonInt(this.value * other.value).ok()
        } else {
            fail("Cannot multiply a $typeCode with $typeCode")
        }
    }

    override operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt && other.value != BigInteger.ZERO) {
            if (value == null || other.value == null) return PythonInt(null).ok()
            PythonInt(this.value / other.value).ok()
        } else {
            fail("Cannot divide $typeCode by ${other.typeCode}")
        }
    }

    override fun boolValue(): Boolean? = if (value == null) null else value != BigInteger.ZERO
}
