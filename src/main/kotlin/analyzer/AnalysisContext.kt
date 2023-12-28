package analyzer

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.PythonType

typealias Identifier = String

sealed interface AnalysisContext {

    fun fail(reason: String): AnalysisContext

    data class Error(
        val reason: String
    ) : AnalysisContext {
        override fun fail(reason: String): AnalysisContext = this
    }

    data class OK(
        val initialStructures: Map<Identifier, PythonDataStructure>,
        val pythonDataStructures: Map<Identifier, PythonDataStructure>,
        val knownFunctionDefs: Map<Identifier, PythonType.Statement.FunctionDef>,
        val returnValue: PythonDataStructure?,
        val functionStack: List<Identifier>,
        val knownImports: Map<String, String>, //todo manage levels
        val warnings: Sequence<String>,
    ) : AnalysisContext {

        fun summarize(): AnalysisResult {
            TODO()
        }

        override fun fail(reason: String): AnalysisContext = Error(reason)

        companion object {
            fun combineNondeterministic(first: AnalysisContext, second: AnalysisContext): AnalysisContext {
                TODO("Not yet implemented")
            }

        }
    }

}



inline fun AnalysisContext.map(block: ContextBuilder.() -> Unit) =
    when (this) {
        is AnalysisContext.OK -> ContextBuilder(this).also {it.block() }.build()
        is AnalysisContext.Error -> this
    }

class ContextBuilder(private val previousContext: AnalysisContext.OK) {

    private var returnValue: PythonDataStructure? = previousContext.returnValue
    private val newWarnings: List<String> = mutableListOf()
    private var failReason: String? = null
    fun addHint() {

    }

    fun returnValue(structure: PythonDataStructure?) {
        returnValue = structure
    }

    fun<T : PythonDataStructure> returnResult(result: OperationResult<T>) {
        when (result) {
            is OperationResult.Ok -> returnValue(result.result)
            is OperationResult.Warning -> returnValue(result.result).also { addWarning(result.message) }
            is OperationResult.Error ->
        }
    }

    fun addWarning(message: String) = newWarnings.addLast(message)

    fun addFunc(def: PythonType.Statement.FunctionDef) {

    }


    fun build(): AnalysisContext =
        if (failReason != null) {
            AnalysisContext.Error(failReason!!) //todo solve non-nullability
        } else {
            AnalysisContext.OK(
                returnValue = returnValue,
                warnings = previousContext.warnings + newWarnings,

                )
        }

    fun wasKnownFunction(name: Identifier): Boolean {

    }

    fun addImport(name: Identifier, alias: Identifier) {

    }

    companion object {
        fun buildEmpty() = AnalysisContext(
            initialStructures = emptyMap(),
            pythonDataStructures = emptyMap(),
            knownFunctionDefs = emptyMap(),
            returnValue = null,
            functionStack = emptyList(),
            knownImports = emptyMap(),
            warnings = emptySequence(),
        )
    }

}
