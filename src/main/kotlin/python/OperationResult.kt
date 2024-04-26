package python

import analyzer.AnalysisContext

sealed interface OperationResult<out T> {
    @JvmInline
    value class Ok<T>(val result: T) : OperationResult<T>

    data class Warning<T>(val result: T, val messages: List<String>) : OperationResult<T>

    @JvmInline
    value class Error<T>(val reason: String) : OperationResult<T>
}

fun <T> T.ok() = OperationResult.Ok(this)

fun <T> T.withWarn(message: String) = OperationResult.Warning(this, listOf(message))

fun <T> fail(reason: String) = OperationResult.Error<T>(reason)

fun <T1, T2> OperationResult<T1>.map(func: (T1) -> OperationResult<T2>): OperationResult<T2> =
    when (this) {
        is OperationResult.Ok -> func(this.result)
        is OperationResult.Warning -> func(this.result) // todo don't lose the previous warning
        is OperationResult.Error -> OperationResult.Error(this.reason)
    }

fun <T> OperationResult<T>.orElse(default: T) =
    when (this) {
        is OperationResult.Error -> default
        is OperationResult.Ok -> this.result
        is OperationResult.Warning -> this.result // todo don't drop the warning :)
    }

inline fun <T> OperationResult<T>.orElse(func: (String) -> T) =
    when (this) {
        is OperationResult.Error -> func(this.reason)
        is OperationResult.Ok -> this.result
        is OperationResult.Warning -> this.result // todo dropping messages
    }
