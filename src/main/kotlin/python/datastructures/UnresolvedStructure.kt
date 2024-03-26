package python.datastructures

data class UnresolvedStructure(val reason: String) : PythonDataStructure {
    override fun clone(): PythonDataStructure = this
}
