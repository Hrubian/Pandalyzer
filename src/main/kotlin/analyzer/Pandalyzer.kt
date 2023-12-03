package analyzer

import python.PythonTree
import python.PythonType

class Pandalyzer(private val metadata: AnalyzerMetadata) {

    fun analyze(tree: PythonTree): AnalysisResult =
        analyze(tree, AnalysisContext.createEmpty())

    private fun analyze(tree: PythonTree, context: AnalysisContext): AnalysisResult =
        TODO()
//        when (tree.root) {
//            is PythonType.Statement.Assign -> {
//                val assignmentResult = analyze(tree.root.value, context)
//                if (assignmentResult.)
//
//            }
//            is PythonType.Expression.Call -> {
//
//            }
//        }
//        when (tree.root) {
//            is PythonType.Alias -> TODO()
//            is PythonType.BoolOperation.And -> TODO()
//            is PythonType.BoolOperation.Or -> TODO()
//            is PythonType.Expression.Attribute -> TODO()
//            is PythonType.Expression.BinaryOperation -> TODO()
//            is PythonType.Expression.Call -> TODO()
//            is PythonType.Expression.Compare -> TODO()
//            is PythonType.Expression.Constant -> TODO()
//            is PythonType.Expression.Name -> TODO()
//            is PythonType.Expression.PythonList -> TODO()
//            is PythonType.Expression.Subscript -> TODO()
//            is PythonType.Mod.Module -> TODO()
//            is PythonType.Operator.Add -> TODO()
//            is PythonType.Operator.Mult -> TODO()
//            is PythonType.Operator.Sub -> TODO()
//            is PythonType.Statement.Assign -> TODO()
//            is PythonType.Statement.Break -> TODO()
//            is PythonType.Statement.Continue -> TODO()
//            is PythonType.Statement.ExpressionStatement -> TODO()
//            is PythonType.Statement.ForLoop -> TODO()
//            is PythonType.Statement.FunctionDef -> TODO()
//            is PythonType.Statement.IfStatement -> TODO()
//            is PythonType.Statement.Import -> TODO()
//            is PythonType.Statement.ImportFrom -> TODO()
//            is PythonType.Statement.Return -> TODO()
//            is PythonType.Statement.WhileLoop -> TODO()
//        }
}