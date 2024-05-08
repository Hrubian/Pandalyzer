package analyzer

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import python.PythonEntity
import python.datastructures.FieldName
import python.datastructures.FieldType

@Serializable
data class AnalysisResult(
    val result: String,
    val warnings: List<Message>,
    val errors: List<Message>,
    val globalDataStructures: Map<Identifier, String>,
    val outputFiles: AnalysisMetadataResult,
) {
    fun toJson(): String = Json.encodeToString(this)

    fun toHumanReadable(): String =
        buildString {
            appendLine("Summary of analysis: $result")

            append("Global data structures (${globalDataStructures.size}):\n")
            globalDataStructures.forEach { (ident, struct) -> append("$ident: $struct \n") }
            append('\n')

            append("Warnings (${warnings.size}):\n")
            warnings.forEachIndexed { i, warn -> appendLine("$i: ${warn.summarize()}") }
            appendLine()

            append("Errors (${errors.size}):\n")
            errors.forEachIndexed { i, err -> appendLine("$i: ${err.summarize()}") }
            appendLine()

            appendLine(outputFiles.toHumanReadable())
        }
}

@Serializable
data class AnalysisMetadataResult(
    val files: Map<String, List<Map<FieldName, FieldType>>>,
) {
    fun toHumanReadable(): String =
        buildString {
            appendLine("Output files (${files.size}): ")
            files.forEach { (filename, fileWrites) ->
                appendLine("File $filename: ")
                summarizeFile(fileWrites)
                append('\n')
            }
        }

    private fun StringBuilder.summarizeFile(fileWrites: List<Map<FieldName, FieldType>>) {
        if (fileWrites.size > 1) {
            appendLine(
                "    Warning: There are multiple options how the" +
                    " resulting file looks like. We will show all of them",
            )
            fileWrites.forEachIndexed { index, dataframe ->
                appendLine("    Option #$index")
                dataframe.forEach { (columnName, columnType) ->
                    appendLine("        $columnName : $columnType")
                }
            }
        } else {
            fileWrites.single().forEach { (columnName, columnType) ->
                appendLine("    $columnName : $columnType")
            }
        }
    }
}

@Serializable
data class Message(
    val text: String,
    val sourceStatement: String,
    val startLine: Int,
    val endLine: Int,
    val columnStart: Int,
    val columnEnd: Int,
) {
    fun summarize(): String =
        "$sourceStatement " +
            if (startLine == endLine) {
                "on line $startLine "
            } else {
                "from line $startLine to line $endLine "
            } +
            "columns $columnStart - $columnEnd: $text"

    companion object {
        fun createMessage(
            text: String,
            sourceStatement: PythonEntity.Statement,
        ): Message =
            Message(
                text = text,
                sourceStatement = sourceStatement.javaClass.simpleName,
                startLine = sourceStatement.startLine,
                endLine = sourceStatement.endLine,
                columnStart = sourceStatement.columnStart,
                columnEnd = sourceStatement.columnEnd,
            )
    }
}
