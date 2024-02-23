package python.datastructures

import analyzer.Identifier
import python.datastructures.pandas.PandasImport

interface ImportStruct : PythonDataStructure

fun createImportStruct(
    libName: Identifier,
    alias: Identifier = libName,
    ): ImportStruct {

    if (libName == "pandas") {
        return PandasImport
    }
    else {
        error("Unknown import $libName")
    }
}
