package analyzer

import python.PythonType
import python.PythonType.Alias
import python.PythonType.BoolOperator.And
import python.PythonType.BoolOperator.Or
import python.PythonType.Expression.*
import python.PythonType.ExpressionContext.*
import python.PythonType.Mod.Module
import python.PythonType.Operator.*
import python.PythonType.Statement.*
import python.datastructures.*

class Pandalyzer {

    fun analyze(module: Module, context: AnalysisContext): AnalysisContext =
        module.body.foldStatements(context)

    fun analyze(functionDef: FunctionDef, context: AnalysisContext): AnalysisContext = context.map {
        addFunc(functionDef)
    }

    fun analyze(returnStatement: Return, context: AnalysisContext): AnalysisContext =
        returnStatement.value.analyzeWith(context).map {
            //todo handle levels and removal of imports and current level
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

    fun analyze(import: Import, context: AnalysisContext): AnalysisContext = context.map {
        import.names.forEach { (aliasName, name) -> addImport(name, aliasName ?: name) }
    }


    fun analyze(importFrom: ImportFrom, context: AnalysisContext): AnalysisContext = context.map {
        importFrom.names.forEach { (aliasName, name) ->
//            addImport()
            //todo
        }
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
        return rightContext.map {
            when (binaryOperation.operator) {
                Add -> leftContext.returnValue!!.sumWith(rightContext.returnValue!!) //todo remove !!
                Mult -> leftContext.returnValue!!.multiplyWith(rightContext.returnValue!!)
                Sub -> leftContext.returnValue!!.subtract(rightContext.returnValue!!)
                Div -> leftContext.returnValue!!.divideBy(rightContext.returnValue!!)
            }.let { returnResult(it) }
        }
    }

    fun analyze(constant: Constant, context: AnalysisContext): AnalysisContext = context.map {
        returnValue(
            when(constant) {
                is Constant.BoolConstant -> PythonBool(constant.value)
                is Constant.IntConstant -> PythonInt(constant.value)
                is Constant.StringConstant -> PythonString(constant.value)
                is Constant.NoneConstant -> null
            }
        )
    }

    fun analyze(call: Call, context: AnalysisContext, struct: PythonDataStructure): AnalysisContext {
        call.func
        TODO("Not yet implemented")
    }

    fun analyze(name: Name, context: AnalysisContext): AnalysisContext = context.map {
        returnValue(context.pythonDataStructures[name.identifier] ?: TODO("we need to fail here due to unknown name"))
    }


    fun analyze(attribute: Attribute, context: AnalysisContext): AnalysisContext = context.map {
        returnValue
    }

    fun analyze(subscript: Subscript, context: AnalysisContext): AnalysisContext {
        subscript.value.analyzeWith(context).map {

        }
        TODO("Not yet implemented")
    }

    fun analyze(compare: Compare, context: AnalysisContext): AnalysisContext {

    }

    fun analyze(pythonList: PythonList, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(dictionary: Dictionary, context: AnalysisContext): AnalysisContext {
        TODO("Not yet implemented")
    }

    fun analyze(and: And, context: AnalysisContext): AnalysisContext { error("Called And") }

    fun analyze(or: Or, context: AnalysisContext): AnalysisContext { error("Called Or") }

    fun analyze(add: Add, context: AnalysisContext): AnalysisContext { error("Called Add") }

    fun analyze(sub: Sub, context: AnalysisContext): AnalysisContext { error("Called Sub") }

    fun analyze(mult: Mult, context: AnalysisContext): AnalysisContext { error("Called Mult") }

    fun analyze(alias: Alias, context: AnalysisContext): AnalysisContext { error("Called alias") }

    fun analyze(delete: Delete,  context: AnalysisContext): AnalysisContext { error("Called delete") }

    fun analyze(load: Load, context: AnalysisContext): AnalysisContext { error("Called load") }

    fun analyze(store: Store, context: AnalysisContext): AnalysisContext { error("Called store") }

    fun analyze(boolOp: BoolOperation, context: AnalysisContext): AnalysisContext = when (boolOp.operator) {
        is And -> {
            boolOp.values.fold(
                initial = context.map { returnValue(PythonBool(true)) },
                operation = { context, value -> } //todo fold only before the value is "false" then stop and return current context with false
            )
        }
        is Or -> {
            // todo fold only before the value is true
            boolOp.values.fold(
                initial = context.map { returnValue(PythonBool(false)) },
                operation = { context, value -> } // todo fold only before the value is true, then stop and return current context with true

            )
        }
    }

    fun List<PythonType.Statement>.foldStatements(initialContext: AnalysisContext): AnalysisContext =
        this.fold(
            initial = initialContext,
            operation = { acc, statement -> acc.map { returnValue(null) }.let { statement.analyzeWith(it) } }
        )

    fun PythonType.analyzeWith(context: AnalysisContext) = when (this) {
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
        is Delete -> analyze(this, context)
        is Load -> analyze(this, context)
        is Store -> analyze(this, context)
        is BoolOperation -> analyze(this, context)
    }
}