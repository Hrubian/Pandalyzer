package python

sealed interface OperationResult<out T> {
    @JvmInline
    value class Ok<T >(val result: T) : OperationResult<T>

    data class Warning<T>(val result: T, val message: String) : OperationResult<T>

    @JvmInline
    value class Error<T>(val reason: String) : OperationResult<T>
}

fun <T> T.ok() = OperationResult.Ok(this)

fun <T> T.withWarn(message: String) = OperationResult.Warning(this, message)

fun <T> fail(reason: String) = OperationResult.Error<T>(reason)

fun <T1, T2> OperationResult<T1>.map(func: (T1) -> OperationResult<T2>): OperationResult<T2> =
    when (this) {
        is OperationResult.Ok -> func(this.result)
        is OperationResult.Warning -> func(this.result) //todo don't lose the previous warning
        is OperationResult.Error -> OperationResult.Error(this.reason)
    }
