import analyzer.ContextBuilder
import analyzer.Pandalyzer
import python.PythonTree

fun main(args: Array<String>) =
    with(ProgramArguments.parse(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                Pandalyzer.analyze(tree.root, ContextBuilder.buildWithBuiltins())
            }.let { result ->
                println(result)
            }
    }
