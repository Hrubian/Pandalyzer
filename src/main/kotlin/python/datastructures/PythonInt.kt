package python.datastructures

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.fail
import python.ok
import java.math.BigInteger

@JvmInline
value class PythonInt(val value: BigInteger) : PythonDataStructure {
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> {
        TODO("Not yet implemented")
    }

    override fun invoke(args: List<PythonDataStructure>, outerContext: AnalysisContext): OperationResult<PythonDataStructure> {
        TODO("Not yet implemented")
    }

    override operator fun plus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt) {
            PythonInt(this.value + other.value).ok()
        } else
            fail("Cannot sum a $typeName with ${other.typeName}")
    }

    override operator fun minus(other: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (other is PythonInt) {
            PythonInt(this.value - other.value).ok()
        } else
            fail("Cannot subtract a ${other.typeName} from $typeName")
    }

    override operator fun times(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        if (other is PythonInt) {
            PythonInt(this.value * other.value).ok()
        } else
            fail("Cannot multiply a $typeName with $typeName")

    override operator fun div(other: PythonDataStructure): OperationResult<PythonDataStructure> =
        if (other is PythonInt && other.value != BigInteger.ZERO) {
            PythonInt(this.value / other.value).ok()
        } else
            fail("Cannot divide $typeName by ${other.typeName}")

}
