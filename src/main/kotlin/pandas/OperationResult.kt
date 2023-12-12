package pandas

sealed interface OperationResult<T : PandasStructure> {
    data class Ok<T : PandasStructure>(val result: T) : OperationResult<T>
    data class Error<T : PandasStructure>(val reason: String): OperationResult<T>
}

fun <T : PandasStructure> T.ok() = OperationResult.Ok(this)

fun <T : PandasStructure> fail(reason: String) = OperationResult.Error<T>(reason)