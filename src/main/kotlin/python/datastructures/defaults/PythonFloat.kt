// package python.datastructures.defaults
//
// import python.OperationResult
// import python.datastructures.PythonDataStructure
// import python.fail
// import python.ok
// import java.math.BigInteger
//
// @JvmInline
// value class PythonFloat(val value: Double?) : PythonDataStructure {
//    override fun clone(): PythonDataStructure = this
//
//    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
//        return if (other is PythonFloat) {
//            if (value == null || other.value == null) return PythonInt(null).ok()
//            PythonFloat(this.value + other.value).ok()
//        } else {
//            fail("Cannot sum a $typeName with ${other.typeName}")
//        }
//    }
//
//    override operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
//        return if (other is PythonInt) {
//            if (value == null || other.value == null) return PythonInt(null).ok()
//            PythonInt(this.value - other.value).ok()
//        } else {
//            fail("Cannot subtract a ${other.typeName} from $typeName")
//        }
//    }
//
//    override operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> {
//        return if (other is PythonInt) {
//            if (value == null || other.value == null) return PythonInt(null).ok()
//            PythonInt(this.value * other.value).ok()
//        } else {
//            fail("Cannot multiply a $typeName with $typeName")
//        }
//    }
//
//    override operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> {
//        return if (other is PythonInt && other.value != BigInteger.ZERO) {
//            if (value == null || other.value == null) return PythonInt(null).ok()
//            TODO("implement floats!!!")
// //            PythonInt(BigDecimal(this.value) / other.value).ok()
//        } else {
//            fail("Cannot divide $typeName by ${other.typeName}")
//        }
//    }
//
//    override fun floorDiv(other: PythonDataStructure): OperationResult<PythonDataStructure> {
//        return if (other is PythonInt && other.value != BigInteger.ZERO) {
//            if (value == null || other.value == null) return PythonInt(null).ok()
//            PythonInt(this.value / other.value).ok()
//        } else {
//            fail("Cannot floor-dive $typeName by ${other.typeName}")
//        }
//    }
//
// }
