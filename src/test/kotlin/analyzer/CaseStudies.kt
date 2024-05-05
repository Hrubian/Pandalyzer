package analyzer

import ProgramArguments
import org.junit.jupiter.api.Test
import python.PythonTree

class CaseStudies {

    @Test
    fun `01`() = runCaseStudy(1) { context ->
        println((context as GlobalAnalysisContext).summarize())
    }

    @Test
    fun `02`() = runCaseStudy(2) { context ->
        println((context as GlobalAnalysisContext).summarize())

    }

    @Test
    fun `03`() = runCaseStudy(3) { context ->
        println((context as GlobalAnalysisContext).summarize())

    }

    @Test
    fun `04`() = runCaseStudy(4) { context ->
        println((context as GlobalAnalysisContext).summarize())

    }

    @Test
    fun `05`() = runCaseStudy(5) { context ->
        println((context as GlobalAnalysisContext).summarize())

    }

    private fun runCaseStudy(caseStudyNumber: Int, assertBlock: (AnalysisContext) -> Unit) {
        val basePath = "./case_studies/${caseStudyNumber.toString().padStart(2, '0')}"
        val scriptPath = "$basePath/script.py"
        val configPath = "$basePath/config.toml"

        val args = ProgramArguments(
            inputFile = scriptPath,
            outputStream = System.out,
            outputFormat = OutputFormat.HumanReadable,
            metadata = AnalyzerMetadata.fromConfigFile(configPath),
            treatWarningsAsErrors = false,
            verbose = false,
        )
        val context = AnalysisContext.buildWithBuiltins(args.metadata)
        val tree = PythonTree.fromFile(args.inputFile)
        Pandalyzer.analyze(tree.root, context)
        assertBlock(context)
    }
}