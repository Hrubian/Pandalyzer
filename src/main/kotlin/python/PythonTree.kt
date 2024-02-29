package python

import kotlinx.serialization.json.Json
import java.io.File

@JvmInline
value class PythonTree private constructor(val root: PythonType.Mod.Module) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private const val CONVERTER_PATH = "/python_converter.py"

        fun fromFile(filename: String): PythonTree {
            val converterCode = object {}.javaClass.getResource(CONVERTER_PATH)!!.readBytes()
            val tmpFile = File.createTempFile("converterCode", "py").also { it.deleteOnExit() }
            tmpFile.writeBytes(converterCode)

            return ProcessBuilder(listOf("python3", tmpFile.absolutePath, "-i", filename))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .inputStream.readAllBytes()
                .let { json.decodeFromString<PythonType.Mod.Module>(String(it)) }
                .let { PythonTree(it) }
                .also { println(it) }
        }
    }
}
