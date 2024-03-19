import analyzer.AnalyzerMetadata
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import kotlin.system.exitProcess

data class ProgramArguments(
    val inputFile: String,
    val outputStream: OutputStream,
    val outputFormat: OutputFormat,
    val metadata: AnalyzerMetadata,
    val printHelp: Boolean,
    val treatWarningsAsErrors: Boolean,
    val verbose: Boolean,
) {
    companion object {
        private val options =
            Options().apply {
                addOption("v", "verbose", false, "Enable verbose mode")
                addOption("i", "input", true, "Input file path")
                addOption("o", "output", true, "Output file path")
                addOption("f", "format", true, "Output file format")
                addOption("w", "werr", false, "Treat warnings as errors")
            }
        private val parser = DefaultParser()

        fun parse(args: Array<String>): ProgramArguments {
            try {
                val commandLine = parser.parse(options, args)
                return ProgramArguments(
                    inputFile = commandLine.getOptionValue("input"),
                    outputStream = commandLine.getOptionValue("output")?.let { FileOutputStream(it) } ?: System.out,
                    outputFormat = OutputFormat.fromString(commandLine.getOptionValue("format")),
                    metadata = AnalyzerMetadata(emptyMap()),
                    printHelp = false, // todo how does it work
                    treatWarningsAsErrors = commandLine.hasOption("verbose"),
                    verbose = commandLine.hasOption("verbose"),
                )
            } catch (ex: Exception) {
                exitProcess(ExitWays.BadArgs.exitCode)
            }
        }
    }
}

enum class OutputFormat {
    HumanReadable,
    JSON,
    ;

    companion object {
        fun fromString(it: String?): OutputFormat =
            when (it) {
                "hr" -> HumanReadable
                "json" -> JSON
                else -> default() // todo warning/error
            }

        fun default() = HumanReadable
    }
}

enum class ExitWays(val exitCode: Int) {
    OK(0),
    BadArgs(1),
}
