package python.datastructures

import python.OperationResult
import python.ok

@JvmInline
value class PythonString(val value: String) : PythonDataStructure {
    override fun sumWith(struct: PythonDataStructure): OperationResult<PythonDataStructure> {
        return if (struct is PythonInt) {
            PythonString(value + struct.value).ok()
        } else {
            super.sumWith(struct)
        }
    }
}
