import analyzer.AnalyzerMetadata
import dataframe.DataFrame
import dataframe.FieldType

data class ProgramArguments(
    val inputFile: String,
    val metadata: AnalyzerMetadata,
)

fun parseArgs(args: Array<String>): ProgramArguments {
    return ProgramArguments( // todo parse from args
        inputFile = "/Users/janhruby/IdeaProjects/Pandalyzer/test.py",
        metadata = AnalyzerMetadata(
            mapOf(
                "kamaradi.csv" to DataFrame(
                    fields = mapOf(
                        "id" to FieldType.IntType,
                        "nickname" to FieldType.StringType,
                        "fullname" to FieldType.StringType,
                        "vyska" to FieldType.IntType,
                        "bydliste" to FieldType.StringType,
                    )
                ),
                "mesta.csv" to DataFrame(
                    fields = mapOf(
                        "name" to FieldType.StringType,
                        "vzdalenost_od_prahy" to FieldType.StringType,
                        "datum_zalozeni" to FieldType.StringType,
                    )
                )
            )
        )
    )
}
