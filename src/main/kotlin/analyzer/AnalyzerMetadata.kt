package analyzer

import python.OperationResult
import python.datastructures.FieldName
import python.datastructures.FieldType
import python.datastructures.defaults.PythonNone
import python.datastructures.pandas.dataframe.DataFrame
import python.ok
import python.withWarn
import java.io.File

data class AnalyzerMetadata(
    private val data: List<Pair<Regex, Map<FieldName, FieldType>>>,
    private val storedData: MutableMap<String, Map<FieldName, FieldType>> = mutableMapOf(),
) {
    fun getDataFrameOrNull(filename: String): DataFrame? =
        data.singleOrNull { it.first.matches(filename) }?.let { DataFrame(it.second.toMutableMap()) }

    fun storeDataframe(filename: String, dataFrame: DataFrame): OperationResult<PythonNone> {
        if (dataFrame.fields == null) {
            return PythonNone.withWarn("Unable to store a dataframe to a csv file $filename as the structure is not known")
        }
        if (filename in storedData) {
            storedData[filename] = dataFrame.fields
            return PythonNone.withWarn("Writting a dataframe to a file where we have already written before.")
        }
        storedData[filename] = dataFrame.fields
        return PythonNone.ok()
    }

    fun summarize(): String = buildString {
        append("Output files (${storedData.size} summary\n")
        storedData.forEach { (filename, dataframe) ->
            append("File $filename: \n")
            dataframe.forEach { (columnName, columnType) ->
                append("    $columnName: $columnType\n")
            }
            append('\n')
        }
    }



    companion object {
        fun fromConfigFile(configFileName: String): AnalyzerMetadata {
            val resultData: MutableMap<String, Map<FieldName, FieldType>> = mutableMapOf()
            var currentFile: String? = null
            val currentColumns: MutableMap<FieldName, FieldType> = mutableMapOf()
            val file = File(configFileName).also { check(it.isFile) { "The file $configFileName does not exist" } }

            file.forEachLine { nonsanitizedLine ->
                val line =
                    nonsanitizedLine
                        .dropWhile { it.isWhitespace() }
                        .takeWhile { it != '#' }
                        .dropLastWhile { it.isWhitespace() }

                when {
                    line.startsWith('[') && line.endsWith(']') -> {
                        if (currentFile != null) {
                            check(resultData.put(currentFile!!, currentColumns.toMap()) == null)
                            currentColumns.clear()
                        }
                        currentFile =
                            line.drop(1).dropLast(1)
                                .also { check(it.isNotBlank()) { "The filename should not be blank" } }
                    }
                    line.isNotEmpty() -> {
                        val (key, value) =
                            line.split("=")
                                .map { str -> str.dropWhile { it.isWhitespace() }.dropLastWhile { it.isWhitespace() } }
                                .also { check(it.size == 2) { "There should be exactly one '=' sign" } }
                                .let { it.first() to it.last() }

                        check(key.isNotBlank()) { "The name of columns should not be blank" }
                        check(value.isNotBlank()) { "The type should not be blank" }
                        check(currentColumns.put(key, value.toFieldType()) == null) { "Redefining column with name $key" }
                    }
                }
            }
            if (currentFile != null && resultData.contains(currentFile).not()) {
                resultData[currentFile!!] = currentColumns.toMap()
            }
            return AnalyzerMetadata(resultData.toMap().mapKeys { Regex(it.key) }.toList())
        }

        private fun String.toFieldType(): FieldType =
            when (this) {
                "\"string\"" -> FieldType.StringType
                "\"int\"" -> FieldType.IntType
                "\"float\"" -> FieldType.FloatType
                "\"datetime\"" -> FieldType.DateTime
                "\"timedelta\"" -> FieldType.TimeDelta
                "\"bool\"" -> FieldType.BoolType
                else -> error("Unknown column type $this")
            }
    }
}
