import analyzer.AnalyzerMetadata
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
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
    val treatWarningsAsErrors: Boolean,
    val verbose: Boolean,
) {
    companion object {
        private val options =
            Options().apply {
                addOption("v", "verbose", false, "Enable verbose mode")
                addOption("i", "input", true, "Input file path (mandatory)")
                addOption("o", "output", true, "Output file path")
                addOption("f", "format", true, "Output file format")
                addOption("w", "werr", false, "Treat warnings as errors")
                addOption("c", "config", true, "Config file location (default is $DEFAULT_CONFIG_FILE)")
                addOption("h", "help", false, "Prints this info")
            }
        private val parser = DefaultParser()

        fun parse(args: Array<String>): ProgramArguments {
            try {
                val commandLine = parser.parse(options, args)
                if (commandLine.hasOption("help")) {
                    printHelpAndExit()
                }
                return ProgramArguments(
                    inputFile =
                        commandLine.getOptionValue("input")
                            ?: printHelpAndExit("The input parameter is mandatory"),
                    outputStream =
                        commandLine.getOptionValue("output")?.let { FileOutputStream(it) }
                            ?: System.out,
                    outputFormat = OutputFormat.fromString(commandLine.getOptionValue("format")),
                    metadata =
                        AnalyzerMetadata.fromConfigFile(
                            commandLine.getOptionValue("help")
                                ?: DEFAULT_CONFIG_FILE,
                        ),
                    treatWarningsAsErrors = commandLine.hasOption("verbose"),
                    verbose = commandLine.hasOption("verbose"),
                )
            } catch (ex: Exception) {
                println("Error while loading program arguments: ${ex.message}")
                exitProcess(ExitWays.BadArgs.exitCode)
            }
        }

        private fun printHelpAndExit(message: String = ""): Nothing {
            println(message)
            val helpFormatter = HelpFormatter()
            helpFormatter.printHelp("pandalyzer", options)
            exitProcess(ExitWays.OK.exitCode)
        }

        private const val DEFAULT_CONFIG_FILE = "./config.toml"
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
