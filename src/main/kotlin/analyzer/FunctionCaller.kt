package analyzer


class FunctionCaller(private val analyzer: Pandalyzer) {

    fun callWithContext(functionName: Identifier, context: AnalysisContext): AnalysisContext {
        val function = context.getFunction(functionName)
        if (function != null) {
            return function.body.fold(
                initial = context,
                operation = { acc, statement ->
                    acc.map { returnValue(null) }.let {
                        with (analyzer) {
                            statement.analyzeWith(it) //todo not nice :(
                        }
                    } //todo copy-paste code
                }
            )
        } else {
            return dispatchPandasFunc(functionName, context)
        }
    }

    private fun dispatchPandasFunc(functionName: Identifier, context: AnalysisContext): AnalysisContext = when (functionName) {
        "merge" ->
        else -> context.fail("Unknown function $functionName")
    }

}