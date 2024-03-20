package python.datastructures.pandas.functions

import analyzer.AnalysisContext
import analyzer.Identifier
import python.OperationResult
import python.PythonType
import python.arguments.ArgumentMatcher
import python.arguments.ResolvedArguments
import python.datastructures.FieldType
import python.datastructures.PythonDataStructure
import python.datastructures.defaults.PythonDict
import python.datastructures.defaults.PythonInvokable
import python.datastructures.defaults.PythonList
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.datastructures.pandas.dataframe.DataFrame
import python.fail
import python.map
import python.ok

object PandasDataframeFunc : PythonDataStructure {
    override fun invoke(
        args: List<PythonDataStructure>,
        keywordArgs: List<Pair<Identifier, PythonDataStructure>>,
        outerContext: AnalysisContext,
    ): OperationResult<PythonDataStructure> =
        ArgumentMatcher.match(argumentSchema, args, keywordArgs.toMap()).map {
            val data = it.matchedArguments["data"]
            when (data) {
                is PythonDict -> dataFrameFromDict(data)
                is DataFrame -> data.ok() // we are assuming that the dataframe is immutable
                else -> fail("Cannot create a Dataframe from ${data?.typeName ?: "a None"}")
            }
        }

    // we want to support also constructs like: pd.DataFrame.from_dict(...)
    override fun attribute(identifier: Identifier): OperationResult<PythonDataStructure> =
        when (identifier) {
            "from_dict" -> fromDictFunc.ok()
            else -> fail("Unknown identifier $identifier")
        }

    private val fromDictFunc =
        PythonInvokable { args, _, _ ->
            val data = args.firstOrNull() ?: return@PythonInvokable fail("")
            if (data is PythonDict) {
                return@PythonInvokable dataFrameFromDict(data)
            } else {
                fail("from_dict does not accept argument of type ${data.typeName}")
            }
        }

    private fun dataFrameFromDict(dict: PythonDict): OperationResult<DataFrame> {
        dict.values.map { (column, values) ->
            val columnName = (column as? PythonString)?.value ?: return fail("The column name has to be a string")
            val columnType =
                when (values) {
                    is PythonDict -> {
                        TODO()
                    }
                    is PythonList -> {
                        val types = values.items.map { it.typeName }.distinct()
                        when (types.size) {
                            0 -> TODO()
                            1 ->
                                when (types.single()) {
                                    "PythonString" -> FieldType.StringType
                                    "PythonInt" -> FieldType.IntType
                                    "PythonBool" -> FieldType.BoolType
                                    "PythonNone" -> FieldType.NullInt // todo
                                    else -> return fail("todo dataframe fromdict")
                                }
                            else -> TODO()
                        }
                    }
                    else -> return fail("The column of a dataframe has be either a list or a dictionary")
                }
            columnName to columnType
        }.let { return DataFrame(it.toMap()).ok() }
    }

    private val argumentSchema =
        ResolvedArguments(
            arguments =
                listOf(
                    PythonType.Arg("data"),
                    PythonType.Arg("index"),
                    PythonType.Arg("columns"),
                    PythonType.Arg("dtype"),
                    PythonType.Arg("copy"),
                ),
            defaults =
                listOf(
                    PythonNone,
                    PythonNone,
                    PythonNone,
                    PythonNone,
                    PythonNone,
                ),
            keywordDefaults = emptyList(),
            keywordOnlyArgs = emptyList(),
            variadicArg = null,
            keywordVariadicArg = null,
            positionalArgs = emptyList(),
        )
}
