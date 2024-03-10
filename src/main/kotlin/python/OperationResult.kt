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
