package analyzer

import python.OperationResult
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.PythonNone
import python.datastructures.UnresolvedStructure

typealias Identifier = String

sealed interface AnalysisContext {
    fun fail(reason: String): AnalysisContext

    fun getStructure(name: Identifier): PythonDataStructure?

    fun getRetValue(): PythonDataStructure

    data class Error(
        val reason: String,
    ) : AnalysisContext {
        override fun fail(reason: String): AnalysisContext = this

        override fun getStructure(name: Identifier): PythonDataStructure? = null

        override fun getRetValue(): PythonDataStructure = UnresolvedStructure // todo really?
    }

    data class Returned(
        val value: PythonDataStructure,
    ) : AnalysisContext {
        override fun fail(reason: String): AnalysisContext = AnalysisContext.Error(reason)

        override fun getStructure(name: Identifier): PythonDataStructure? = null

        override fun getRetValue(): PythonDataStructure = value // todo really?
    }

    data class OK(
        // old version
//        val initialStructures: Map<Identifier, PythonDataStructure>,
//        val pythonDataStructures: Map<Identifier, PythonDataStructure>,
//        val knownFunctionDefs: Map<Identifier, PythonType.Statement.FunctionDef>,
//        val returnValue: PythonDataStructure?,
//        val functionStack: List<Identifier>,
//        val knownImports: Map<String, String>, //todo manage levels
//        val warnings: Sequence<String>,
        // new version
        val pythonDataStructures: Map<Identifier, PythonDataStructure>,
        val returnValue: PythonDataStructure,
        val outerContext: AnalysisContext?,
        val warnings: List<String>,
    ) : AnalysisContext {
        fun summarize(): AnalysisResult {
            TODO()
        }

        override fun fail(reason: String): AnalysisContext = Error(reason)

        override fun getStructure(name: Identifier): PythonDataStructure? =
            pythonDataStructures.getOrElse(name) { outerContext?.getStructure(name) }

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
                                        item1 ?: item2 ?: error("This does not make sense")
                                    }
                                },
                        outerContext = first.outerContext.also { check(first.outerContext == second.outerContext) },
                    )
//                first is OK
            }
    }
}

inline fun AnalysisContext.map(block: ContextBuilder.() -> Unit) =
    when (this) {
        is AnalysisContext.OK -> ContextBuilder(this).also { it.block() }.build()
        is AnalysisContext.Returned -> this
        is AnalysisContext.Error -> this
    }

class ContextBuilder(private val previousContext: AnalysisContext.OK) {
    private var returnValue: PythonDataStructure = previousContext.returnValue
    private val newWarnings: List<String> = mutableListOf()
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

    fun addWarning(message: String): Unit = TODO() // newWarnings.addFirst(message) //todo last

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
            )
    }
}
