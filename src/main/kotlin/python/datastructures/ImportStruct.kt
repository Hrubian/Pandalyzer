package python.datastructures

import analyzer.Identifier
import python.datastructures.pandas.PandasImport

interface ImportStruct : PythonDataStructure

fun createImportStruct(
    libName: Identifier,
    alias: Identifier = libName,
): PythonDataStructure {
    return if (libName == "pandas") {
        PandasImport
    } else {
        UnresolvedStructure
//        error("Unknown import $libName")
    }
}
