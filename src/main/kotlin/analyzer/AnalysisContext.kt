package analyzer

import python.OperationResult
import python.PythonType
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.builtinFunctions
import python.datastructures.pandas.dataframe.DataFrame

typealias Identifier = String

sealed interface AnalysisContext {
    fun upsertStruct(
        name: Identifier,
        value: PythonDataStructure,
    ): PythonDataStructure?

    fun getStruct(name: Identifier): PythonDataStructure?

    fun addWarning(
        message: String,
        sourceStatement: PythonType.Statement,
    )

    fun addWarnings(
        messages: List<String>,
        sourceStatement: PythonType.Statement,
    ) = messages.forEach { addWarning(it, sourceStatement) }

    fun addError(
        message: String,
        sourceStatement: PythonType.Statement,
    )

    fun getGlobalContext(): AnalysisContext

    fun fork(): AnalysisContext

    fun join(other: AnalysisContext)

    fun getDataframeFromMetadata(filename: String): DataFrame?

    fun storeDataframeToMetadata(filename: String, dataFrame: DataFrame): OperationResult<PythonNone>

    companion object {
        fun buildEmpty(metadata: AnalyzerMetadata): GlobalAnalysisContext =
            GlobalAnalysisContext(
                pythonDataStructures = mutableMapOf(),
                errors = mutableListOf(),
                metadata = metadata,
                warnings = mutableListOf(),
            )

        fun buildWithBuiltins(metadata: AnalyzerMetadata): GlobalAnalysisContext =
            buildEmpty(metadata).apply { builtinFunctions.forEach { upsertStruct(it.key, it.value) } }

        fun buildForFunction(outerContext: AnalysisContext): FunctionAnalysisContext = FunctionAnalysisContext(outerContext)
    }
}

data class Message(val text: String, val sourceStatement: PythonType.Statement) {
    fun summarize(): String =
        "${sourceStatement.javaClass.simpleName} " +
            if (sourceStatement.startLine == sourceStatement.endLine) {
                "on line ${sourceStatement.startLine} "
            } else {
                "from line ${sourceStatement.startLine} to line ${sourceStatement.endLine} "
            } +
            "columns ${sourceStatement.columnStart} - ${sourceStatement.columnEnd}: $text"
}

data class GlobalAnalysisContext(
    private val pythonDataStructures: MutableMap<Identifier, PythonDataStructure>,
    private val warnings: MutableList<Message>,
    private val metadata: AnalyzerMetadata,
    private val errors: MutableList<Message>,
) : AnalysisContext {
    override fun upsertStruct(
        name: Identifier,
        value: PythonDataStructure,
    ) = pythonDataStructures.put(name, value)

    override fun getStruct(name: Identifier) = pythonDataStructures[name]

    override fun addWarning(
        message: String,
        sourceStatement: PythonType.Statement,
    ) {
        warnings.add(Message(message, sourceStatement))
    }

    override fun addError(
        message: String,
        sourceStatement: PythonType.Statement,
    ) {
        errors.add(Message(message, sourceStatement))
    }

    override fun getGlobalContext(): AnalysisContext = this

    override fun fork(): AnalysisContext =
        GlobalAnalysisContext(
            pythonDataStructures = pythonDataStructures.map { it.key to it.value.clone() }.toMap().toMutableMap(),
            warnings = mutableListOf(),
            metadata = metadata,
            errors = mutableListOf(),
        )

    override fun join(other: AnalysisContext) {
        val otherGlobal = other as GlobalAnalysisContext
        warnings.addAll(otherGlobal.warnings)
        errors.addAll(otherGlobal.errors)
        (pythonDataStructures.keys + other.pythonDataStructures.keys).associateWithTo(pythonDataStructures) { key ->
            val first = pythonDataStructures[key]
            val second = other.pythonDataStructures[key]
            if (first != null && second != null) {
                if (first == second) first else NondeterministicDataStructure(first, second)
            } else if (first != null) { // second is null
                NondeterministicDataStructure(first, PythonNone)
            } else { // first is null
                NondeterministicDataStructure(second!!, PythonNone)
            }
        }
    }

    override fun getDataframeFromMetadata(filename: String): DataFrame? = metadata.getDataFrameOrNull(filename)

    override fun storeDataframeToMetadata(filename: String, dataFrame: DataFrame): OperationResult<PythonNone> =
        metadata.storeDataframe(filename, dataFrame)

    fun summarize(): String =
        buildString {
            append("Summary of analysis: ")
            append(if (errors.isEmpty()) "OK" else "NOT OK")
            append('\n')

            append("Global data structures (${pythonDataStructures.size}):\n")
            pythonDataStructures.forEach { (ident, struct) -> append("$ident: $struct \n") }
            append('\n')

            append("Warnings (${warnings.size}):\n")
            warnings.forEachIndexed { i, warn -> append("$i: ${warn.summarize()}\n") }
            append('\n')

            append("Errors (${errors.size}):\n")
            errors.forEachIndexed { i, error -> append("$i: ${error.summarize()}\n") }
            append('\n')

            append(metadata.summarize())
        }
}

data class FunctionAnalysisContext(
    private val outerContext: AnalysisContext,
    private val pythonDataStructures: MutableMap<Identifier, PythonDataStructure> = mutableMapOf(),
) : AnalysisContext {
    private val globalContext = getGlobalContext()

    override fun upsertStruct(
        name: Identifier,
        value: PythonDataStructure,
    ): PythonDataStructure? = pythonDataStructures.put(name, value)

    override fun getStruct(name: Identifier): PythonDataStructure? = pythonDataStructures[name] ?: globalContext.getStruct(name)

    override fun addWarning(
        message: String,
        sourceStatement: PythonType.Statement,
    ) {
        outerContext.addWarning(message, sourceStatement)
    }

    override fun addError(
        message: String,
        sourceStatement: PythonType.Statement,
    ) {
        outerContext.addError(message, sourceStatement)
    }

    override fun getGlobalContext(): AnalysisContext = outerContext.getGlobalContext()

    override fun fork(): AnalysisContext =
        FunctionAnalysisContext(
            outerContext = outerContext.fork(),
            pythonDataStructures = pythonDataStructures.map { it.key to it.value.clone() }.toMap().toMutableMap(),
        )

    override fun join(other: AnalysisContext) {
        outerContext.join((other as FunctionAnalysisContext).outerContext)
        (pythonDataStructures.keys + other.pythonDataStructures.keys).associateWithTo(pythonDataStructures) { key ->
            val first = pythonDataStructures[key]
            val second = other.pythonDataStructures[key]
            if (first != null && second != null) {
                if (first == second) first else NondeterministicDataStructure(first, second)
            } else if (first != null) { // second is null
                NondeterministicDataStructure(first, PythonNone)
            } else { // first is null
                NondeterministicDataStructure(second!!, PythonNone)
            }
        }
    }

    override fun getDataframeFromMetadata(filename: String): DataFrame? = globalContext.getDataframeFromMetadata(filename)

    override fun storeDataframeToMetadata(filename: String, dataFrame: DataFrame) =
        globalContext.storeDataframeToMetadata(filename, dataFrame)
}
