package python.datastructures

import analyzer.Identifier
import python.PythonType

data class PythonFunc(
    val name: Identifier?,
    val body: List<PythonType.Statement>,
    val positionArguments: List<String>
) : PythonDataStructure
