import analyzer.Pandalyzer
import python.PythonTree


fun main(args: Array<String>) {
    with(ProgramArguments("/Users/janhruby/IdeaProjects/Pandalyzer/test.py")) {
        PythonTree.fromFile(inputFile).let { tree ->
            Pandalyzer().analyze(tree).let { result ->
                println(result)
            }
        }
    }
}
