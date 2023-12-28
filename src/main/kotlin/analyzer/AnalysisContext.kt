package analyzer

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.PythonType

typealias Identifier = String

data class AnalysisContext(
    val initialStructures: Map<Identifier, PythonDataStructure>,
    val pythonDataStructures: Map<Identifier, PythonDataStructure>,
    val knownFunctionDefs: Map<Identifier, PythonType.Statement.FunctionDef>,
    val returnValue: PythonDataStructure?,
    val functionStack: List<Identifier>,
    val knownImports: Map<String, String>, //todo manage levels
) {

    fun summarize(): AnalysisResult {
        TODO()
    }

    fun fail(reason: String): AnalysisContext {

    }

    companion object {
        val empty: AnalysisContext = AnalysisContext(
            initialStructures = emptyMap(),
            pythonDataStructures = emptyMap(),
            knownFunctionDefs = emptyMap(),
            returnValue = null,
            functionStack = emptyList(),
        )

        fun combineNondeterministic(first: AnalysisContext, second: AnalysisContext): AnalysisContext {
            TODO("Not yet implemented")
        }

    }
}

inline fun AnalysisContext.map(block: ContextBuilder.() -> Unit) =
    ContextBuilder(this).also { it.block() }.build()

class ContextBuilder(previousContext: AnalysisContext) {

    fun addHint() {

    }

    fun returnValue(structure: PythonDataStructure?) {

    }

    fun<T : PythonDataStructure> returnResult(result: OperationResult<T>) {

    }

    fun addFunc(name: Identifier, body: List<PythonType.Statement>) { //todo args

    }

    fun build(): AnalysisContext =
        AnalysisContext(

        )

    fun wasKnownFunction(name: Identifier): Boolean {

    }

    fun addImport(name: Identifier, alias: Identifier) {

    }

}
