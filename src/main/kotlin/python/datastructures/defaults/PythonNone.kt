package python.datastructures.defaults

import python.datastructures.PythonDataStructure

data object PythonNone : PythonDataStructure {
    override fun clone(): PythonDataStructure = PythonNone

    override fun boolValue(): Boolean = false
}
