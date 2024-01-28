package python.datastructures

import analyzer.Identifier

data class ImportStruct(
    val libName: Identifier,
    val alias: Identifier = libName,

) : PythonDataStructure
