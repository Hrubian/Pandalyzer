package python

import python.datastructures.PythonDataStructure

sealed interface OperationResult<out T : PythonDataStructure> {
    @JvmInline
    value class Ok<T : PythonDataStructure>(val result: T) : OperationResult<T>
    data class Warning<T : PythonDataStructure>(val result: T, val message: String) : OperationResult<T>
    @JvmInline
    value class Error<T : PythonDataStructure>(val reason: String): OperationResult<T>
}

fun <T : PythonDataStructure> T.ok() = OperationResult.Ok(this)

fun <T : PythonDataStructure> T.withWarn(message: String) = OperationResult.Warning(this, message)

fun <T : PythonDataStructure> fail(reason: String) = OperationResult.Error<T>(reason)