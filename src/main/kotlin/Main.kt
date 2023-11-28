import analyzer.Pandalyzer
import python.PythonTree


fun main(args: Array<String>) {
    with(ProgramArguments("test.py")) {
        PythonTree.fromFile(inputFile).let { tree ->
            Pandalyzer().analyze(tree).let { result ->
                println(result)
            }
        }
    }
}

data class ProgramArguments(
    val inputFile: String,
)