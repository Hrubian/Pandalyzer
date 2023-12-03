package analyzer

import dataframe.PandasStructure

typealias Identifier = String

data class AnalysisContext(
    val pandasStructures: Map<Identifier, PandasStructure>,
    val returnValue: Any?
) {
    companion object {
        fun createEmpty(): AnalysisContext = AnalysisContext(
            pandasStructures = emptyMap(),
            returnValue = null
        )
    }
}
