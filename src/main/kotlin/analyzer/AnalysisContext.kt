package analyzer

import OutputFormat
import python.OperationResult
import python.PythonEntity
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonFunc
import python.datastructures.defaults.PythonInvokable
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.builtinFunctions
import python.datastructures.pandas.dataframe.DataFrame

typealias Identifier = String

/**
 * Mutable structure representing a current state in the analysis
 */
sealed interface AnalysisContext {
    /**
     * Add or update a structure [value] under the name [name]
     */
    fun upsertStruct(
        name: Identifier,
        value: PythonDataStructure,
    ): PythonDataStructure?

    /**
     * Get a structure registered under the name [name] or null if no such structure was registered yet
     */
    fun getStruct(name: Identifier): PythonDataStructure?

    /**
     * Add a new warning triggered by the analysis
     */
    fun addWarning(
        message: String,
        sourceStatement: PythonEntity.Statement,
    )

    /**
     * Add a set of warnings triggered by the analysis
     */
    fun addWarnings(
        messages: List<String>,
        sourceStatement: PythonEntity.Statement,
    ) = messages.forEach { addWarning(it, sourceStatement) }

    /**
     * Add a new error that was raised during the analysis
     */
    fun addError(
        message: String,
        sourceStatement: PythonEntity.Statement,
    )

    /**
     * Get the outer-most global context
     */
    fun getGlobalContext(): AnalysisContext

    /**
     * Creates a new, same [AnalysisContext] that can be used for non-deterministic execution
     */
    fun fork(): AnalysisContext

    /**
     * Joins the previously forked [other] [AnalysisContext], merging the nondeterministic structures
     */
    fun join(other: AnalysisContext)

    /**
     * Gets the Dataframe accessible via read_csv function under the name [filename] from the metadata
     */
    fun getDataframeFromMetadata(filename: String): DataFrame?

    /**
     * Stores the [dataFrame] to the record registered under the [filename] in the metadata
     */
    fun storeDataframeToMetadata(
        filename: String,
        dataFrame: DataFrame,
    ): OperationResult<PythonNone>

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
        sourceStatement: PythonEntity.Statement,
    ) {
        warnings.add(Message.createMessage(message, sourceStatement))
    }

    override fun addError(
        message: String,
        sourceStatement: PythonEntity.Statement,
    ) {
        errors.add(Message.createMessage(message, sourceStatement))
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

    override fun storeDataframeToMetadata(
        filename: String,
        dataFrame: DataFrame,
    ): OperationResult<PythonNone> = metadata.storeDataframe(filename, dataFrame)

    fun summarize(format: OutputFormat): String {
        val result =
            AnalysisResult(
                result = if (errors.isEmpty()) "OK" else "NOT OK",
                warnings = warnings,
                errors = errors,
                globalDataStructures =
                    pythonDataStructures
                        .filterNot { it.key in builtinFunctions && it.value is PythonInvokable }
                        .filterNot { it.value is PythonFunc }
                        .mapValues { it.toString() },
                outputFiles = metadata.summarize(),
            )
        return when (format) {
            OutputFormat.HumanReadable -> result.toHumanReadable()
            OutputFormat.JSON -> result.toJson()
        }
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
        sourceStatement: PythonEntity.Statement,
    ) {
        outerContext.addWarning(message, sourceStatement)
    }

    override fun addError(
        message: String,
        sourceStatement: PythonEntity.Statement,
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

    override fun storeDataframeToMetadata(
        filename: String,
        dataFrame: DataFrame,
    ) = globalContext.storeDataframeToMetadata(filename, dataFrame)
}
