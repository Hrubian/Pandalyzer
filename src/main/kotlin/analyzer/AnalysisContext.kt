package analyzer

import python.OperationResult
import python.datastructures.PythonDataStructure
import python.PythonType
import python.datastructures.PythonNone
import python.datastructures.UnresolvedStructure

typealias Identifier = String

sealed interface AnalysisContext {

    fun fail(reason: String): AnalysisContext

    fun getStructure(name: Identifier): PythonDataStructure?
    fun getReturnValue(): PythonDataStructure

    data class Error(
        val reason: String
    ) : AnalysisContext {
        override fun fail(reason: String): AnalysisContext = this
        override fun getStructure(name: Identifier): PythonDataStructure? = null
        override fun getReturnValue(): PythonDataStructure = UnresolvedStructure //todo really?
    }

    data class OK(
        //old version
//        val initialStructures: Map<Identifier, PythonDataStructure>,
//        val pythonDataStructures: Map<Identifier, PythonDataStructure>,
//        val knownFunctionDefs: Map<Identifier, PythonType.Statement.FunctionDef>,
//        val returnValue: PythonDataStructure?,
//        val functionStack: List<Identifier>,
//        val knownImports: Map<String, String>, //todo manage levels
//        val warnings: Sequence<String>,
        //new version
        val pythonDataStructures: Map<Identifier, PythonDataStructure>,
        val returnValue: PythonDataStructure,
        val outerContext: AnalysisContext?,
        val knownImports: Map<String, String>,
        val warnings: List<String>,
    ) : AnalysisContext {

        fun summarize(): AnalysisResult {
            TODO()
        }

        override fun fail(reason: String): AnalysisContext = Error(reason)

        override fun getStructure(name: Identifier): PythonDataStructure? =
            pythonDataStructures.getOrElse(name) { outerContext?.getStructure(name) }

        override fun getReturnValue(): PythonDataStructure = returnValue

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

    private var returnValue: PythonDataStructure = previousContext.returnValue
    private val newWarnings: List<String> = mutableListOf()
    private val newImports: Map<String, String> = mutableMapOf()
    private var failReason: String? = null
    fun addHint() {

    }

    fun returnValue(structure: PythonDataStructure) {
        returnValue = structure
    }

    fun dropLevel() {
        TODO()
    }

    fun<T : PythonDataStructure> returnResult(result: OperationResult<T>) {
        when (result) {
            is OperationResult.Ok -> returnValue(result.result)
            is OperationResult.Warning -> returnValue(result.result).also { addWarning(result.message) }
            is OperationResult.Error -> error("TODO")
        }
    }

    fun addWarning(message: String) = newWarnings.addLast(message)

    fun addStruct(identifier: Identifier, struct: PythonDataStructure) {
        TODO()
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

    fun addImport(name: Identifier, alias: Identifier) {

    }

    fun fail(reason: String) {
        failReason = reason
    }

    companion object {
        fun buildEmpty(outerContext: AnalysisContext? = null) = AnalysisContext.OK(
            pythonDataStructures = emptyMap(),
            returnValue = PythonNone,
            knownImports = emptyMap(),
            warnings = emptyList(),
            outerContext = outerContext,
        )
    }

}
