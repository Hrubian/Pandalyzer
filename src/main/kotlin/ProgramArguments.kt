import analyzer.AnalyzerMetadata
import analyzer.DataFrame
import analyzer.DataFrameFields

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
                        "id" to DataFrameFields.IntType,
                        "nickname" to DataFrameFields.StringType,
                        "fullname" to DataFrameFields.StringType,
                        "vyska" to DataFrameFields.IntType,
                        "bydliste" to DataFrameFields.StringType,
                    )
                ),
                "mesta.csv" to DataFrame(
                    fields = mapOf(
                        "name" to DataFrameFields.StringType,
                        "vzdalenost_od_prahy" to DataFrameFields.StringType,
                        "datum_zalozeni" to DataFrameFields.StringType,
                    )
                )
            )
        )
    )
}
