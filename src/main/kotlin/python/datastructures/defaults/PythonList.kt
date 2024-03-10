package python.datastructures.defaults

import python.datastructures.PythonDataStructure

@JvmInline
value class PythonList(
    val items: MutableList<PythonDataStructure>,
) : PythonDataStructure
