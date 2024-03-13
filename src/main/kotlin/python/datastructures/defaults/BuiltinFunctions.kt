package python.datastructures.defaults

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.datastructures.PythonDataStructure
import python.fail
import python.ok

fun interface PythonInvokable : PythonDataStructure {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext
    ): OperationResult<PythonDataStructure>

}


    val builtinFunctions = mapOf(
        "abs" to PythonInvokable { args, _ ->
            (args.singleOrNull() as? PythonInt)?.value?.abs()?.let { PythonInt(it) }?.ok() ?:
            fail("Cannot apply abs function to $args")
        },

        "print" to PythonInvokable { _, _, _ -> PythonNone.ok() },

        "dict" to PythonInvokable { _, _, _-> PythonDict(mutableMapOf()).ok() },

        "len" to PythonInvokable { args, _, _ ->
            when (val arg = args.singleOrNull()) {
                is PythonString -> PythonInt(arg.value.length.toBigInteger()).ok()
                is PythonList -> PythonInt(arg.items.size.toBigInteger()).ok()
                else -> fail("Cannot apply len function to $args")
            }
        },

        "list" to PythonInvokable { _, _, _ -> PythonList(mutableListOf()).ok() }, //todo what about version with args

    )