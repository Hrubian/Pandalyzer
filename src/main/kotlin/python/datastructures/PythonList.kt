package python.datastructures

@JvmInline
value class PythonList(
    val items: MutableList<PythonDataStructure>
) : PythonDataStructure
