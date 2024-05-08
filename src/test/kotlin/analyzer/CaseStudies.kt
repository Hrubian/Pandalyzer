package analyzer

import ProgramArguments
import org.junit.jupiter.api.Test
import python.PythonTree

class CaseStudies {
    @Test
    fun `01`() = runCaseStudy(1)

    @Test
    fun `02`() = runCaseStudy(2)

    @Test
    fun `03`() = runCaseStudy(3)

    @Test
    fun `04`() = runCaseStudy(4)

    @Test
    fun `05`() = runCaseStudy(5)

    private fun runCaseStudy(caseStudyNumber: Int) {
        val basePath = "./case_studies/${caseStudyNumber.toString().padStart(2, '0')}"
        val scriptPath = "$basePath/script.py"
        val configPath = "$basePath/config.toml"

        val args =
            ProgramArguments(
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
        println((context).summarize(OutputFormat.HumanReadable))
    }
}
