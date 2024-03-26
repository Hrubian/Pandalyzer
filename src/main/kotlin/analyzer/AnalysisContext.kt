package analyzer

import python.datastructures.PythonDataStructure
import python.datastructures.defaults.builtinFunctions

typealias Identifier = String

sealed interface AnalysisContext {

    fun upsertStruct(name: Identifier, value: PythonDataStructure): PythonDataStructure?

    fun getStruct(name: Identifier): PythonDataStructure?

    fun addWarning(message: String)

    fun addError(message: String)

    fun getGlobalContext(): AnalysisContext

    fun clone(): AnalysisContext

    fun merge(other: AnalysisContext)

    companion object {
        fun buildEmpty(): GlobalAnalysisContext =
            GlobalAnalysisContext(
                pythonDataStructures = mutableMapOf(),
                errors = mutableListOf(),
                warnings = mutableListOf()
            )

        fun buildWithBuiltins(): GlobalAnalysisContext = buildEmpty().apply { builtinFunctions.forEach { upsertStruct(it.key, it.value) } }

        fun buildForFunction(outerContext: AnalysisContext): FunctionAnalysisContext =
            FunctionAnalysisContext(outerContext)
    }
}

data class GlobalAnalysisContext(
    private val pythonDataStructures: MutableMap<Identifier, PythonDataStructure>,
    private val warnings: MutableList<String>,
    private val errors: MutableList<String>
    ): AnalysisContext {
    override fun upsertStruct(name: Identifier, value: PythonDataStructure) = pythonDataStructures.put(name, value)

    override fun getStruct(name: Identifier) = pythonDataStructures[name]

    override fun addWarning(message: String) { warnings.add(message) }

    override fun addError(message: String) { errors.add(message) }

    override fun getGlobalContext(): AnalysisContext = this

    override fun clone(): AnalysisContext =
        GlobalAnalysisContext(
            pythonDataStructures = pythonDataStructures.map { it.key to it.value.clone() }.toMap().toMutableMap(),
            warnings = mutableListOf(),
            errors = mutableListOf()
        )

    override fun merge(other: AnalysisContext) {
        val otherGlobal = other as GlobalAnalysisContext
        warnings.addAll(otherGlobal.warnings)
        errors.addAll(otherGlobal.errors)
        TODO("merge python structs")
    }

    fun summarize(): String = buildString {
        append("Summary of analysis: ")
        append(if (errors.isEmpty()) "OK" else "NOT OK")
        append('\n')

        append("Global data structures (${pythonDataStructures.size}):\n")
        pythonDataStructures.forEach { (ident, struct) -> append("$ident: $struct \n") }
        append('\n')

        append("Warnings (${warnings.size}):\n")
        warnings.forEachIndexed { i, warn -> append("$i: $warn\n") }
        append('\n')

        append("Errors (${errors.size}):\n")
        errors.forEachIndexed { i, error -> append("$i: $error\n") }
    }
}

data class FunctionAnalysisContext(
    private val outerContext: AnalysisContext,
    private val pythonDataStructures: MutableMap<Identifier, PythonDataStructure> = mutableMapOf()
): AnalysisContext {
    private val globalContext = getGlobalContext()

    override fun upsertStruct(name: Identifier, value: PythonDataStructure): PythonDataStructure? =
        pythonDataStructures.put(name, value)

    override fun getStruct(name: Identifier): PythonDataStructure? =
        pythonDataStructures[name] ?: globalContext.getStruct(name)

    override fun addWarning(message: String) {
        outerContext.addWarning(message)
    }

    override fun addError(message: String) {
        outerContext.addError(message)
    }

    override fun getGlobalContext(): AnalysisContext = outerContext.getGlobalContext()

    override fun clone(): AnalysisContext =
        FunctionAnalysisContext(
            outerContext = outerContext.clone(),
            pythonDataStructures = pythonDataStructures.map { it.key to it.value.clone() }.toMap().toMutableMap()
        )

    override fun merge(other: AnalysisContext) {
        outerContext.merge((other as FunctionAnalysisContext).outerContext)
        TODO("merge python structs")
    }
}