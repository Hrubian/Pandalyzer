package analyzer

import pandas.PandasStructure
import python.PythonType

typealias Identifier = String

data class AnalysisContext(
    val initialStructures: Map<Identifier, PandasStructure>,
    val pandasStructures: Map<Identifier, PandasStructure>,
    val knownFunctionDefs: Map<Identifier, PythonType.Statement.FunctionDef>,
    val returnValue: PandasStructure?,
) {

    fun summarize(): AnalysisResult {
        TODO()
    }

    fun fail(reason: String): AnalysisContext {

    }

    companion object {
        fun createEmpty(): AnalysisContext = AnalysisContext(
            pandasStructures = emptyMap(),
            knownFunctionDefs = emptyMap(),
            returnValue = null,
        )

        fun combineNondeterministic(first: AnalysisContext, second: AnalysisContext): AnalysisContext {
            TODO("Not yet implemented")
        }

    }
}

inline fun mapContext(previousContext: AnalysisContext, block: ContextBuilder.() -> Unit) =
    ContextBuilder(previousContext).also { it.block() }.build()

class ContextBuilder(previousContext: AnalysisContext) {

    fun addHint() {

    }

    fun returnValue(structure: PandasStructure?) {

    }

    fun addFunc() {

    }

    fun build(): AnalysisContext =
        AnalysisContext(

        )

    fun wasKnownFunction(name: Identifier): Boolean {

    }

}
