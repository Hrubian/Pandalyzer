package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.series.Series
import python.fail
import python.ok
import python.withWarn
import java.math.BigInteger

@JvmInline
value class PythonInt(val value: BigInteger?) : PythonDataStructure {
    override fun clone(): PythonDataStructure = PythonInt(value)

    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonInt(this.value + other.value).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonFloat(this.value.toDouble() + other.value).ok()
            }
            is Series -> {
                other.plus(this)
            }
            else -> fail("Cannot sum a $typeName with ${other.typeName}")
        }
    }

    override operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonInt(this.value - other.value).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonFloat(this.value.toDouble() - other.value).ok()
            }
            is Series -> {
                other.minus(this)
            }
            else -> fail("Cannot subtract a ${other.typeName} from $typeName")
        }
    }

    override operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonInt(this.value * other.value).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonFloat(this.value.toDouble() * other.value).ok()
            }
            is Series -> {
                other.times(this)
            }
            else -> fail("Cannot multiply a $typeName with ${other.typeName}")
        }
    }

    override operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonFloat(this.value.toDouble() / other.value.toDouble()).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonFloat(this.value.toDouble() * other.value).ok()
            }
            is Series -> {
                other.times(this)
            }
            else -> fail("Cannot divide $typeName by ${other.typeName}")
        }
    }

    override fun floorDiv(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonInt(this.value / other.value).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonInt(this.value * other.value.toInt().toBigInteger()).ok()
            }
            is Series -> {
                other.times(this)
            }
            else -> fail("Cannot divide $typeName by ${other.typeName}")
        }
    }

    override fun negate(): OperationResult<PythonDataStructure> =
        if (value == null) {
            PythonInt(null).withWarn("Could not determine the negative value of unknown number")
        } else {
            PythonInt(-value).ok()
        }

    override fun positive(): OperationResult<PythonDataStructure> = this.ok()

    override fun boolValue(): Boolean? = if (value == null) null else value != BigInteger.ZERO

    override infix fun equal(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other) { a, b -> a == b }

    override infix fun greaterThan(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other) { a, b -> a > b }

    override infix fun greaterThanEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        compare(other) { a, b -> a >= b }

    override infix fun lessThan(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other) { a, b -> a < b }

    override infix fun lessThanEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other) { a, b -> a <= b }

    override infix fun notEqual(other: PythonDataStructure): OperationResult<PythonDataStructure> = compare(other) { a, b -> a != b }

    private fun compare(
        other: PythonDataStructure,
        operation: (BigInteger, BigInteger) -> Boolean,
    ): OperationResult<PythonDataStructure> =
        if (other is PythonInt) {
            if (value != null && other.value != null) {
                PythonBool(operation(value, other.value)).ok()
            } else {
                PythonBool(null).withWarn("unable to get the values of integers for comparing")
            }
        } else {
            PythonBool(false).withWarn("Comparing different types: $typeName and ${other.typeName}")
        }
}
