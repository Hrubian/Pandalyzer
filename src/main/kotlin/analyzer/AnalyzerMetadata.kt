package analyzer

import python.datastructures.pandas.dataframe.DataFrame

data class AnalyzerMetadata(
    val dataFrames: Map<String, DataFrame>,
)
