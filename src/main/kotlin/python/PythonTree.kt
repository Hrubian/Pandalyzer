package python

import kotlinx.serialization.json.Json
import java.io.File

@JvmInline
value class PythonTree private constructor(private val root: PythonType) {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val CONVERTER_PATH = "/python_converter.py"
        fun fromFile(filename: String): PythonTree {
            val converterCode = object{}.javaClass.getResource(CONVERTER_PATH)!!.readBytes()
            val tmpFile = File.createTempFile("converterCode", "py").also { it.deleteOnExit() }
            tmpFile.writeBytes(converterCode)

            return ProcessBuilder(listOf("python3", tmpFile.absolutePath, "-i", filename))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .inputStream.readAllBytes().toString()
                .let { json.decodeFromString<PythonType>(it) }
                .let { PythonTree(it) }
        }
    }
}