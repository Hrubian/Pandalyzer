package python.datastructures.defaults

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.datastructures.pandas.series.Series
import python.fail
import python.ok
import python.withWarn

@JvmInline
value class PythonFloat(val value: Double?) : PythonDataStructure {
    override fun clone(): PythonDataStructure = this

    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonFloat(this.value + other.value.toDouble()).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonFloat(this.value + other.value).ok()
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
                PythonFloat(this.value - other.value.toDouble()).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonFloat(this.value - other.value).ok()
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
                PythonFloat(this.value * other.value.toDouble()).ok()
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
                other.div(this)
            }
            else -> fail("Cannot divide $typeName by ${other.typeName}")
        }
    }

    override fun floorDiv(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return when (other) {
            is PythonInt -> {
                if (value == null || other.value == null) return PythonInt(null).ok()
                PythonInt(this.value.toInt().toBigInteger() / other.value).ok()
            }
            is PythonFloat -> {
                if (value == null || other.value == null) return PythonFloat(null).ok()
                PythonInt((this.value * other.value).toInt().toBigInteger()).ok()
            }
            is Series -> {
                other.floorDiv(this)
            }
            else -> fail("Cannot divide $typeName by ${other.typeName}")
        }
    }

    override fun negate(): OperationResult<PythonDataStructure> =
        if (value == null) {
            PythonFloat(null).withWarn("Could not determine the negative value of unknown float")
        } else {
            PythonFloat(-value).ok()
        }

    override fun positive(): OperationResult<PythonDataStructure> = this.ok()
}
