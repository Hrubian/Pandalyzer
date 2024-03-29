package analyzer

import python.OperationResult
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.builtinFunctions

typealias Identifier = String

sealed interface AnalysisContext {
    fun fail(reason: String): AnalysisContext

    fun getStructure(name: Identifier): PythonDataStructure?

    fun getRetValue(): PythonDataStructure

    fun summarize(): String

    data class Error(
        val reason: String,
    ) : AnalysisContext {
        override fun fail(reason: String): AnalysisContext = this

        override fun getStructure(name: Identifier): PythonDataStructure? = null

        override fun getRetValue(): PythonDataStructure = UnresolvedStructure // todo really?

        override fun summarize(): String = "Error occured. Reason: $reason"
    }

    data class Returned(
        val value: PythonDataStructure,
    ) : AnalysisContext {
        override fun fail(reason: String): AnalysisContext = Error(reason)

        override fun getStructure(name: Identifier): PythonDataStructure? = null

        override fun getRetValue(): PythonDataStructure = value // todo really?

        override fun summarize(): String = error("Returned should never be the last context")
    }

    data class OK(
        val pythonDataStructures: Map<Identifier, PythonDataStructure>,
        val returnValue: PythonDataStructure,
        val outerContext: AnalysisContext?, // todo there should probably be a globalContext
        val globalContext: AnalysisContext?,
        val warnings: List<String>,
    ) : AnalysisContext {
        override fun summarize(): String =
            if (warnings.isEmpty()) "OK, last return value: $returnValue"
            else "There were following warnings: $warnings"

        override fun fail(reason: String): AnalysisContext = Error(reason)

        override fun getStructure(name: Identifier): PythonDataStructure? =
            pythonDataStructures.getOrElse(name) { globalContext?.getStructure(name) }

        override fun getRetValue(): PythonDataStructure = returnValue
    }

    companion object {
        fun combineNondeterministic(
            first: AnalysisContext,
            second: AnalysisContext,
        ): AnalysisContext =
            when {
                first is OK && second is OK ->
                    OK(
                        pythonDataStructures =
                            (first.pythonDataStructures.keys + second.pythonDataStructures.keys)
                                .associateWith {
                                    val item1 = first.pythonDataStructures[it]
                                    val item2 = second.pythonDataStructures[it]
                                    if (item1 != null && item2 != null) {
                                        NondeterministicDataStructure(item1, item2)
                                    } else {
                                        item1 ?: item2!!
                                    }
                                },
                        outerContext = first.outerContext.also { check(first.outerContext == second.outerContext) },
                        returnValue = NondeterministicDataStructure(first.returnValue, second.returnValue),
                        warnings = first.warnings + second.warnings, // todo
                        globalContext = first.globalContext ?: first.outerContext
                    )
                else -> TODO()
            }
    }
}

inline fun AnalysisContext.map(block: ContextBuilder.() -> Unit) =
    when (this) {
        is AnalysisContext.OK -> ContextBuilder(this).also { it.block() }.build()
        is AnalysisContext.Returned -> this
        is AnalysisContext.Error -> this
    }

fun AnalysisContext.returnFromFunc(): AnalysisContext =
    when (this) {
        is AnalysisContext.OK -> AnalysisContext.Returned(getRetValue())
        is AnalysisContext.Error -> this
        is AnalysisContext.Returned -> this
    }

class ContextBuilder(private val previousContext: AnalysisContext.OK) {
    private var returnValue: PythonDataStructure = previousContext.returnValue
    private val newWarnings: MutableList<String> = mutableListOf()
    private var failReason: String? = null
    private val upsertedDataStructures: MutableMap<Identifier, PythonDataStructure> = mutableMapOf()

    fun addHint() {
    }

    fun returnValue(structure: PythonDataStructure) {
        returnValue = structure
    }

    fun <T : PythonDataStructure> returnResult(result: OperationResult<T>) {
        when (result) {
            is OperationResult.Ok -> returnValue(result.result)
            is OperationResult.Warning -> returnValue(result.result).also { addWarning(result.message) }
            is OperationResult.Error -> failReason = result.reason
        }
    }

    fun addWarning(message: String) {
        newWarnings.add(message)
    }

    fun addStruct(
        identifier: Identifier,
        struct: PythonDataStructure,
    ) {
        upsertedDataStructures[identifier] = struct
    }

    fun build(): AnalysisContext =
        failReason?.let {
            AnalysisContext.Error(it)
        } ?: AnalysisContext.OK(
            returnValue = returnValue,
            warnings = previousContext.warnings + newWarnings,
            outerContext = previousContext.outerContext,
            pythonDataStructures = previousContext.pythonDataStructures + upsertedDataStructures,
            globalContext = null,
        )

    fun fail(reason: String) {
        failReason = reason
    }

    companion object {
        fun buildEmpty(outerContext: AnalysisContext? = null) =
            AnalysisContext.OK(
                pythonDataStructures = emptyMap(),
                returnValue = PythonNone,
                warnings = emptyList(),
                outerContext = outerContext,
                globalContext = null,
            )

        fun buildWithBuiltins() = buildEmpty().map { builtinFunctions.forEach { addStruct(it.key, it.value) } }
    }
}
