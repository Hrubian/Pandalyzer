package python

import analyzer.AnalysisContext

interface IAnalyzer {
    fun analyze(module: PythonType.Mod.Module, context: AnalysisContext) : AnalysisContext
    fun analyze(functionDef: PythonType.Statement.FunctionDef, context: AnalysisContext) : AnalysisContext
    fun analyze(arg: PythonType.Statement.Return, context: AnalysisContext) : AnalysisContext
    fun analyze(assign: PythonType.Statement.Assign, context: AnalysisContext) : AnalysisContext
    fun analyze(forLoop: PythonType.Statement.ForLoop, context: AnalysisContext) : AnalysisContext
    fun analyze(whileLoop: PythonType.Statement.WhileLoop, context: AnalysisContext) : AnalysisContext
    fun analyze(ifStatement: PythonType.Statement.IfStatement, context: AnalysisContext) : AnalysisContext
    fun analyze(import: PythonType.Statement.Import, context: AnalysisContext) : AnalysisContext
    fun analyze(importFrom: PythonType.Statement.ImportFrom, context: AnalysisContext) : AnalysisContext
    fun analyze(expressionStatement: PythonType.Statement.ExpressionStatement, context: AnalysisContext) : AnalysisContext
    fun analyze(arg: PythonType.Statement.Break, context: AnalysisContext) : AnalysisContext
    fun analyze(arg: PythonType.Statement.Continue, context: AnalysisContext) : AnalysisContext
    fun analyze(binaryOperation: PythonType.Expression.BinaryOperation, context: AnalysisContext) : AnalysisContext
    fun analyze(constant: PythonType.Expression.Constant, context: AnalysisContext) : AnalysisContext
    fun analyze(call: PythonType.Expression.Call, context: AnalysisContext) : AnalysisContext
    fun analyze(name: PythonType.Expression.Name, context: AnalysisContext) : AnalysisContext
    fun analyze(attribute: PythonType.Expression.Attribute, context: AnalysisContext) : AnalysisContext
    fun analyze(subscript: PythonType.Expression.Subscript, context: AnalysisContext) : AnalysisContext
    fun analyze(compare: PythonType.Expression.Compare, context: AnalysisContext) : AnalysisContext
    fun analyze(pythonList: PythonType.Expression.PythonList, context: AnalysisContext) : AnalysisContext
    fun analyze(dictionary: PythonType.Expression.Dictionary, context: AnalysisContext) : AnalysisContext
    fun analyze(and: PythonType.BoolOperation.And, context: AnalysisContext) : AnalysisContext
    fun analyze(or: PythonType.BoolOperation.Or, context: AnalysisContext) : AnalysisContext
    fun analyze(add: PythonType.Operator.Add, context: AnalysisContext) : AnalysisContext
    fun analyze(sub: PythonType.Operator.Sub, context: AnalysisContext) : AnalysisContext
    fun analyze(mult: PythonType.Operator.Mult, context: AnalysisContext) : AnalysisContext
    fun analyze(alias: PythonType.Alias, context: AnalysisContext) : AnalysisContext
}