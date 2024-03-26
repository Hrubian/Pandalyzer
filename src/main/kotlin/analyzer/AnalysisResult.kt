package analyzer

data class AnalysisResult(
    val ok: Boolean, // todo maybe rather sealed class?
    val warnings: List<String>,
    val hints: List<String>,
)