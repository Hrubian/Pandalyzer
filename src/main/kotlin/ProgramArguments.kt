import analyzer.AnalyzerMetadata
import java.io.OutputStream

data class ProgramArguments(
    val inputFile: String,
    val outputStream: OutputStream,
    val outputFormat: OutputFormat,
    val metadata: AnalyzerMetadata,
    val printHelp: Boolean,
    val treatWarningsAsErrors: Boolean,
    val verbose: Boolean,
)

enum class OutputFormat {
    HumanReadable,
    JSON;
    companion object {
        fun fromString(it: String): OutputFormat = when (it) {
            "hr" -> HumanReadable
            "json" -> JSON
            else -> HumanReadable //todo warning/error
        }

        fun default() = HumanReadable
    }
}

enum class ExitWays(exitCode: Int) {
    OK(0),
    BadArgs(1)
}

fun parseArgs(args: Array<String>): ProgramArguments {
    return TODO()
//    ProgramArguments( // todo parse from args
//        inputFile = "/Users/janhruby/IdeaProjects/Pandalyzer/test.py",
//        metadata = AnalyzerMetadata(
//            mapOf(
//                "kamaradi.csv" to DataFrame(
//                    fields = mapOf(
//                        "id" to FieldType.IntType,
//                        "nickname" to FieldType.StringType,
//                        "fullname" to FieldType.StringType,
//                        "vyska" to FieldType.IntType,
//                        "bydliste" to FieldType.StringType,
//                    )
//                ),
//                "mesta.csv" to DataFrame(
//                    fields = mapOf(
//                        "name" to FieldType.StringType,
//                        "vzdalenost_od_prahy" to FieldType.StringType,
//                        "datum_zalozeni" to FieldType.StringType,
//                    )
//                )
//            )
//        )
//    )
}
