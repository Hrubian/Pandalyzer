package analyzer

import python.PythonType
import python.PythonType.Alias
import python.PythonType.BoolOperation.And
import python.PythonType.BoolOperation.Or
import python.PythonType.Expression.*
import python.PythonType.Mod.Module
import python.PythonType.Operator.*
import python.PythonType.Statement.*

class Pandalyzer {

    fun analyze(module: Module, context: AnalysisContext): AnalysisContext =
        module.body.foldStatements(context)

    fun analyze(functionDef: FunctionDef, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(arg: Return, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(assign: Assign, context: AnalysisContext): AnalysisContext {
        //todo check if the assignment is type hint
        val newContext = assign.value.analyzeWith(context)
        //todo assign value and return resulting context
    }

    fun analyze(forLoop: ForLoop, context: AnalysisContext): AnalysisContext =
        context.fail("The loops are not supported (ForLoop)")

    fun analyze(whileLoop: WhileLoop, context: AnalysisContext): AnalysisContext =
        context.fail("The loops are not supported (WhileLoop)")

    fun analyze(ifStatement: IfStatement, context: AnalysisContext): AnalysisContext =
        ifStatement.test.analyzeWith(context).let { newContext ->
            AnalysisContext.combineNondeterministic(
                first = ifStatement.body.foldStatements(newContext),
                second = ifStatement.orElse.foldStatements(newContext),
            )
        }

    fun analyze(import: Import, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(importFrom: ImportFrom, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(expressionStatement: ExpressionStatement, context: AnalysisContext): AnalysisContext =
        expressionStatement.expression.analyzeWith(context) //todo what about the return value? (maybe it should stay)

    fun analyze(arg: Break, context: AnalysisContext): AnalysisContext =
        context.fail(reason = "The loops are not supported (Break statement)")

    fun analyze(arg: Continue, context: AnalysisContext): AnalysisContext =
        context.fail(reason = "The loops are not supported (Continue statement)")

    fun analyze(binaryOperation: BinaryOperation, context: AnalysisContext): AnalysisContext {
        val leftContext = binaryOperation.left.analyzeWith(context)
        val rightContext = binaryOperation.right.analyzeWith(leftContext)
        return mapContext(rightContext) {
            when (binaryOperation.operator) {
                Add -> leftContext.returnValue + rightContext.returnValue
                Mult -> leftContext.returnValue * rightContext.returnValue
                Sub -> leftContext.returnValue - rightContext.returnValue //todo implement operators
            }.let { returnValue(it) }
        }
    }

    fun analyze(constant: Constant, context: AnalysisContext): AnalysisContext = mapContext(context) {
        returnValue(constant)
    }

    fun analyze(call: Call, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(name: Name, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(attribute: Attribute, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(subscript: Subscript, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(compare: Compare, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(pythonList: PythonList, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(dictionary: Dictionary, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(and: And, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(or: Or, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(add: Add, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(sub: Sub, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(mult: Mult, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(alias: Alias, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    private fun List<PythonType.Statement>.foldStatements(initialContext: AnalysisContext): AnalysisContext =
        this.fold(
            initial = initialContext,
            operation = { acc, statement -> statement.analyzeWith(acc) } //todo remove return values
        )

    private fun PythonType.analyzeWith(context: AnalysisContext) = when (this) {
        is Module -> analyze(this, context)
        is Alias -> analyze(this, context)
        is And -> analyze(this, context)
        is Or -> analyze(this, context)
        is Attribute -> analyze(this, context)
        is BinaryOperation -> analyze(this, context)
        is Call -> analyze(this, context)
        is Compare -> analyze(this, context)
        is Constant -> analyze(this, context)
        is Dictionary -> analyze(this, context)
        is Name -> analyze(this, context)
        is PythonList -> analyze(this, context)
        is Subscript -> analyze(this, context)
        is Add -> analyze(this, context)
        is Mult -> analyze(this, context)
        is Sub -> analyze(this, context)
        is Assign -> analyze(this, context)
        is Break -> analyze(this, context)
        is Continue -> analyze(this, context)
        is ExpressionStatement -> analyze(this, context)
        is ForLoop -> analyze(this, context)
        is FunctionDef -> analyze(this, context)
        is IfStatement -> analyze(this, context)
        is Import -> analyze(this, context)
        is ImportFrom -> analyze(this, context)
        is Return -> analyze(this, context)
        is WhileLoop -> analyze(this, context)
    }
}