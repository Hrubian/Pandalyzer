import analyzer.Pandalyzer
import python.PythonTree


fun main(args: Array<String>) =
    with(parseArgs(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                Pandalyzer(metadata).analyze(tree)
            }.let { result ->
                println(result)
            }
    }