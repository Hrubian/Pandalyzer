package python

import analyzer.AnalysisContext

fun interface IAnalyzable {
    fun analyzeWith(analyzer: IAnalyzer, context: AnalysisContext) : AnalysisContext
}