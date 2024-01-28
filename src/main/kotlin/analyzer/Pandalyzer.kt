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
import python.datastructures.PythonList

class Pandalyzer {

    fun analyze(module: Module, context: AnalysisContext): AnalysisContext =
        module.body.foldStatements(context)

    fun analyze(functionDef: FunctionDef, context: AnalysisContext): AnalysisContext = context.map {
        val func = PythonFunc(
            name = functionDef.name,
            body = functionDef.body,
            positionArguments = listOf() //todo add arguments
        )
        addStruct(functionDef.name, func)
    }

    fun analyze(returnStatement: Return, context: AnalysisContext): AnalysisContext =
        returnStatement.value.analyzeWith(context).map {
            dropLevel()
        }


    fun analyze(assign: Assign, context: AnalysisContext): AnalysisContext {
        //todo check if the assignment is type hint
        val newContext = assign.value.analyzeWith(context)
        //todo assign value and return resulting context
        val identifier = (assign.targets.first() as Name).identifier
        return newContext.map {
            addStruct(identifier, newContext.getReturnValue())
        }
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
                Add -> leftContext.getReturnValue() + (rightContext.getReturnValue())
                Mult -> leftContext.getReturnValue() * (rightContext.getReturnValue())
                Sub -> leftContext.getReturnValue() - (rightContext.getReturnValue())
                Div -> leftContext.getReturnValue() / (rightContext.getReturnValue())
            }.let { returnResult(it) }
        }
    }

    fun analyze(constant: Constant, context: AnalysisContext): AnalysisContext = context.map {
        returnValue(
            when(constant) {
                is Constant.BoolConstant -> PythonBool(constant.value)
                is Constant.IntConstant -> PythonInt(constant.value)
                is Constant.StringConstant -> PythonString(constant.value)
                is Constant.NoneConstant -> PythonNone
            }
        )
    }

    fun analyze(call: Call, context: AnalysisContext): AnalysisContext {
        val callable = call.func.analyzeWith(context).getReturnValue()
        val args = call.arguments.fold(context) { currContext, arg -> arg.analyzeWith(currContext) }
        callable.callWithArgs(args, context)
        //todo check args, create inner context, add the args as known structures and call the func
        TODO("Not yet implemented")
    }

    fun analyze(name: Name, context: AnalysisContext): AnalysisContext = context.map {
        val struct = context.getStructure(name.identifier)
        if (struct == null) {
            fail("The name $name is not known.")
        } else {
            returnValue(struct)
        }
    }

    fun analyze(attribute: Attribute, context: AnalysisContext): AnalysisContext = context.map {
        val valResultContext = attribute.value.analyzeWith(context)
        valResultContext.getReturnValue().attribute(attribute.attr).let {
            returnResult(it)
        }
    }

    fun analyze(subscript: Subscript, context: AnalysisContext): AnalysisContext = context.map {
        val newContext = subscript.value.analyzeWith(context)
        val sliceContext = subscript.slice.analyzeWith(newContext)
        newContext.getReturnValue().subscript(sliceContext.getReturnValue()).let {
            returnResult(it)
        }
    }

    fun analyze(compare: Compare, context: AnalysisContext): AnalysisContext {
        TODO()
    }

    fun analyze(pythonList: PythonType.Expression.PythonList, context: AnalysisContext): AnalysisContext {
        when (pythonList.context) {
            is Load -> {
                val elements = pythonList.elements.map { it.analyzeWith(context).run { getReturnValue() }}
                return context.map { returnValue(PythonList(elements.toMutableList())) }
            }
            is Store -> {
                TODO("Not implemented yet")
            }
            is Delete -> {
                TODO("Del not implemented")
            }
        }
    }

    fun analyze(dictionary: Dictionary, context: AnalysisContext): AnalysisContext = context.map {
        returnValue(
            PythonDict(
                values = dictionary.keys
                    .zip(dictionary.values)
                    .associate { (key, value) ->
                        key.analyzeWith(context).getReturnValue() to value.analyzeWith(context).getReturnValue() //todo pass on context
                    }
                    .toMutableMap()
            )
        )
    }

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
        is Attribute -> analyze(this, context)
        is BinaryOperation -> analyze(this, context)
        is Call -> analyze(this, context)
        is Compare -> analyze(this, context)
        is Constant -> analyze(this, context)
        is Dictionary -> analyze(this, context)
        is Name -> analyze(this, context)
        is PythonType.Expression.PythonList -> analyze(this, context)
        is Subscript -> analyze(this, context)
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
        is BoolOperation -> analyze(this, context)
        is PythonType.BoolOperator -> error("Called BoolOperator")
        is PythonType.Operator -> error("Called Operator")
        is PythonType.CompareOperator -> error("Called CompareOperator")
        is PythonType.ExpressionContext -> error("Called ExpressionContext")
        is Alias -> error("Called Alias")
    }
}