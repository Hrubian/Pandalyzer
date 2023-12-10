package analyzer

import python.IAnalyzer
import python.PythonType
import python.PythonType.Alias
import python.PythonType.BoolOperation.And
import python.PythonType.BoolOperation.Or
import python.PythonType.Expression.*
import python.PythonType.Mod.Module
import python.PythonType.Operator.*
import python.PythonType.Statement.*

class Pandalyzer : IAnalyzer {

    override fun analyze(module: Module, context: AnalysisContext): AnalysisContext =
        module.body.foldStatements(context)

    override fun analyze(functionDef: FunctionDef, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(arg: Return, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(assign: Assign, context: AnalysisContext): AnalysisContext {
        //todo check if the assignment is type hint
        val newContext = assign.value.analyzeWith(this, context)
        //todo assign value and return resulting context
    }

    override fun analyze(forLoop: ForLoop, context: AnalysisContext): AnalysisContext =
        context.fail("The loops are not supported (ForLoop)")

    override fun analyze(whileLoop: WhileLoop, context: AnalysisContext): AnalysisContext =
        context.fail("The loops are not supported (WhileLoop)")

    override fun analyze(ifStatement: IfStatement, context: AnalysisContext): AnalysisContext =
        ifStatement.test.analyzeWith(this, context).let { newContext ->
            AnalysisContext.combineNondeterministic(
                first = ifStatement.body.foldStatements(newContext),
                second = ifStatement.orElse.foldStatements(newContext),
            )
        }

    override fun analyze(import: Import, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(importFrom: ImportFrom, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(expressionStatement: ExpressionStatement, context: AnalysisContext): AnalysisContext =
        expressionStatement.expression.analyzeWith(this, context) //todo what about the return value? (maybe it should stay)

    override fun analyze(arg: Break, context: AnalysisContext): AnalysisContext =
        context.fail(reason = "The loops are not supported (Break statement)")

    override fun analyze(arg: Continue, context: AnalysisContext): AnalysisContext =
        context.fail(reason = "The loops are not supported (Continue statement)")

    override fun analyze(binaryOperation: BinaryOperation, context: AnalysisContext): AnalysisContext {
        val leftContext = binaryOperation.left.analyzeWith(this, context)
        val rightContext = binaryOperation.right.analyzeWith(this, leftContext)
        return mapContext(rightContext) {
            when (binaryOperation.operator) {
                Add -> leftContext.returnValue + rightContext.returnValue
                Mult -> leftContext.returnValue * rightContext.returnValue
                Sub -> leftContext.returnValue - rightContext.returnValue //todo implement operators
            }.let { returnValue(it) }
        }
    }

    override fun analyze(constant: Constant, context: AnalysisContext): AnalysisContext = mapContext(context) {
        returnValue(constant)
    }

    override fun analyze(call: Call, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(name: Name, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(attribute: Attribute, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(subscript: Subscript, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(compare: Compare, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(pythonList: PythonList, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(dictionary: Dictionary, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(and: And, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(or: Or, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(add: Add, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(sub: Sub, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(mult: Mult, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    override fun analyze(alias: Alias, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    private fun List<PythonType.Statement>.foldStatements(initialContext: AnalysisContext): AnalysisContext =
        this.fold(
            initial = initialContext,
            operation = { acc, statement -> statement.analyzeWith(this@Pandalyzer, acc) } //todo remove return values
        )

}