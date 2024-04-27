package python

sealed interface OperationResult<out T> {
    @JvmInline
    value class Ok<T>(val result: T) : OperationResult<T>

    data class Warning<T>(val result: T, val messages: List<String>) : OperationResult<T>

    @JvmInline
    value class Error<T>(val reason: String) : OperationResult<T>
}

fun <T> OperationResult<T>.addWarnings(warnings: List<String>): OperationResult<T> =
    if (warnings.isEmpty()) {
        this
    } else {
        when (this) {
            is OperationResult.Ok -> OperationResult.Warning(this.result, warnings)
            is OperationResult.Warning -> OperationResult.Warning(this.result, this.messages + warnings)
            is OperationResult.Error -> this
        }
    }

fun <T> T.ok(cumulatedWarnings: List<String> = emptyList()) =
    if (cumulatedWarnings.isEmpty()) {
        OperationResult.Ok(this)
    } else {
        OperationResult.Warning(this, cumulatedWarnings)
    }

fun <T> T.withWarn(message: String) = OperationResult.Warning(this, listOf(message))

fun <T> fail(reason: String) = OperationResult.Error<T>(reason)

fun <T1, T2> OperationResult<T1>.map(func: (T1) -> OperationResult<T2>): OperationResult<T2> =
    when (this) {
        is OperationResult.Ok -> func(this.result)
        is OperationResult.Warning -> func(this.result).addWarnings(this.messages)
        is OperationResult.Error -> OperationResult.Error(this.reason)
    }

inline fun <T> OperationResult<T>.orElse(func: (String) -> T): Pair<T, List<String>> =
    when (this) {
        is OperationResult.Error -> func(this.reason) to emptyList()
        is OperationResult.Ok -> this.result to emptyList()
        is OperationResult.Warning -> this.result to messages
    }
