package python.datastructures

data class NondeterministicDataStructure(
    val left: PythonDataStructure,
    val right: PythonDataStructure,
) : PythonDataStructure {
}