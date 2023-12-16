package python.datastructures

import python.OperationResult
import python.ok
import java.math.BigInteger

@JvmInline
value class PythonInt(val value: BigInteger) : PythonDataStructure {
    override fun sumWith(struct: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (struct is PythonInt) {
            PythonInt(value + struct.value).ok()
        } else {
            super.sumWith(struct)
        }
    }
}
