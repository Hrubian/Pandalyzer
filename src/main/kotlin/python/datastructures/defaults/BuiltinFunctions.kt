package python.datastructures.defaults

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonType
import python.addWarnings
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.PythonDataStructure
import python.fail
import python.ok
import python.orElse
import python.withWarn
import java.math.BigInteger

fun interface PythonInvokable : PythonDataStructure {
    override fun clone(): PythonDataStructure = this

    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure>
}

val builtinFunctions =
    mapOf(
        "abs" to
            PythonInvokable { args, _, _ ->
                (args.singleOrNull() as? PythonInt)?.value?.abs()?.let { PythonInt(it) }?.ok()
                    ?: fail("Cannot apply abs function to $args")
            },
        "print" to PythonInvokable { _, _, _ -> PythonNone.ok() },
        "dict" to PythonInvokable { _, _, _ -> PythonDict(mutableMapOf()).ok() },
        "len" to
            PythonInvokable { args, _, _ ->
                when (val arg = args.singleOrNull()) {
                    is PythonString -> PythonInt(arg.value?.length?.toBigInteger()).ok()
                    is PythonList -> PythonInt(arg.items?.size?.toBigInteger()).ok()
                    else -> fail("Cannot apply len function to $args")
                }
            },
        "list" to PythonInvokable { _, _, _ -> PythonList(mutableListOf()).ok() }, // todo what about version with args
        "input" to PythonInvokable { _, _, _ -> PythonString(null).withWarn("Unable to resolve result of input") },
        "int" to
            PythonInvokable { args, kwArgs, _ ->
                val schema =
                    ResolvedArguments(
                        positionalArgs = listOf(PythonType.Arg("x")),
                        arguments = listOf(PythonType.Arg("base")),
                        defaults = listOf(PythonInt(BigInteger.TEN)),
                    )
                val (matchedArgs, matchWarnings) =
                    ArgumentMatcher.match(schema, args, kwArgs.toMap())
                        .orElse { return@PythonInvokable fail("Unable to resolve 'int' function arguments.") }

                val x = matchedArgs.matchedArguments["x"]
                val base =
                    (matchedArgs.matchedArguments["base"] as? PythonInt)
                        ?: return@PythonInvokable fail("the base argument to int function must be an integer")

                if (base.value == null) {
                    return@PythonInvokable PythonInt(null).withWarn("Unable to determine the base for int function.")
                        .addWarnings(matchWarnings)
                }

                return@PythonInvokable when (x) {
                    is PythonInt -> PythonInt(x.value).ok()
                    is PythonString -> {
                        if (x.value == null) {
                            PythonInt(null).withWarn("Unable to determine the string value for int function")
                        } else {
                            val intValue = x.value.toBigIntegerOrNull() ?: return@PythonInvokable fail("Invalid number ${x.value}")
                            PythonInt(intValue).ok()
                        }
                    }
                    else -> fail("Invalid argument type for int function")
                }.addWarnings(matchWarnings)
            },
    )
