package analyzer

import analyzer.Pandalyzer.analyzeWith
import python.OperationResult
import python.PythonType
import python.PythonType.Alias
import python.PythonType.BoolOperator
import python.PythonType.BoolOperator.And
import python.PythonType.BoolOperator.Or
import python.PythonType.CompareOperator
import python.PythonType.Expression.Attribute
import python.PythonType.Expression.BinaryOperation
import python.PythonType.Expression.BoolOperation
import python.PythonType.Expression.Call
import python.PythonType.Expression.Compare
import python.PythonType.Expression.Constant
import python.PythonType.Expression.Constant.IntConstant
import python.PythonType.Expression.Constant.NoneConstant
import python.PythonType.Expression.Constant.StringConstant
import python.PythonType.Expression.Dictionary
import python.PythonType.Expression.Name
import python.PythonType.Expression.PythonList
import python.PythonType.Expression.Subscript
import python.PythonType.ExpressionContext
import python.PythonType.ExpressionContext.Load
import python.PythonType.Mod.Module
import python.PythonType.Operator
import python.PythonType.Operator.Add
import python.PythonType.Operator.Div
import python.PythonType.Operator.Mult
import python.PythonType.Operator.Sub
import python.PythonType.Statement
import python.PythonType.Statement.Assign
import python.PythonType.Statement.Break
import python.PythonType.Statement.Continue
import python.PythonType.Statement.ExpressionStatement
import python.PythonType.Statement.ForLoop
import python.PythonType.Statement.FunctionDef
import python.PythonType.Statement.IfStatement
import python.PythonType.Statement.Import
import python.PythonType.Statement.ImportFrom
import python.PythonType.Statement.Return
import python.PythonType.Statement.WhileLoop
import python.arguments.ResolvedArguments.Companion.resolve
import python.datastructures.PythonDataStructure
import python.datastructures.createImportStruct
import python.datastructures.defaults.PythonBool
import python.datastructures.defaults.PythonDict
import python.datastructures.defaults.PythonFunc
import python.datastructures.defaults.PythonInt
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString

object Pandalyzer {
    fun analyze(
        module: Module,
        context: AnalysisContext,
    ): AnalysisContext = module.body.foldStatements(context)

    fun analyze(
        functionDef: FunctionDef,
        context: AnalysisContext,
    ): AnalysisContext {
        val (resolvedArgs, newContext) = functionDef.args.resolve(context)
        val func =
            PythonFunc(
                name = functionDef.name,
                body = functionDef.body,
                arguments = resolvedArgs,
            )
        return newContext.map {
            addStruct(functionDef.name, func)
        }
    }

    fun analyze(
        returnStatement: Return,
        context: AnalysisContext,
    ): AnalysisContext = returnStatement.value.analyzeWith(context).getRetValue().let { AnalysisContext.Returned(it) }
    // FIXME: getRetValue returns

    fun analyze(
        assign: Assign,
        context: AnalysisContext,
    ): AnalysisContext {
        // todo check if the assignment is type hint
        val newContext = assign.value.analyzeWith(context)
        // todo assign value and return resulting context
        val identifier = (assign.targets.first() as Name).identifier
        return newContext.map {
            addStruct(identifier, newContext.getRetValue())
        }
    }

    fun analyze(
        forLoop: ForLoop,
        context: AnalysisContext,
    ): AnalysisContext = context.fail("The loops are not supported (ForLoop)")

    fun analyze(
        whileLoop: WhileLoop,
        context: AnalysisContext,
    ): AnalysisContext = context.fail("The loops are not supported (WhileLoop)")

    fun analyze(
        ifStatement: IfStatement,
        context: AnalysisContext,
    ): AnalysisContext =
        ifStatement.test.analyzeWith(context).let { newContext ->
            val retValue = newContext.getRetValue()
            if (retValue is PythonBool) {
                if (retValue.value) {
                    ifStatement.body.foldStatements(newContext)
                } else {
                    ifStatement.orElse.foldStatements(newContext)
                }
            } else {
                AnalysisContext.combineNondeterministic(
                    first = ifStatement.body.foldStatements(newContext),
                    second = ifStatement.orElse.foldStatements(newContext),
                )
            }
        }

    fun analyze(
        import: Import,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            import.names.forEach { (aliasName, name) ->
                addStruct(aliasName ?: name, createImportStruct(name, aliasName ?: name))
            }
        }

    fun analyze(
        importFrom: ImportFrom,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            val importStruct = createImportStruct(importFrom.module!!, importFrom.module) // tood resolve "!!"
            importFrom.names.forEach { (aliasName, name) ->
                when (val result = importStruct.attribute(name)) {
                    is OperationResult.Ok -> addStruct(aliasName ?: name, result.result)
                    is OperationResult.Warning -> addStruct(aliasName ?: name, result.result).also { addWarning(result.message) }
                    is OperationResult.Error ->
                        addWarning(
                            "The package ${importFrom.module} does not contain $name.",
                        ) // todo change back to fail
                }
            }
        }

    fun analyze(
        expressionStatement: ExpressionStatement,
        context: AnalysisContext,
    ): AnalysisContext = expressionStatement.expression.analyzeWith(context) // todo what about the return value? (maybe it should stay)

    fun analyze(
        arg: Break,
        context: AnalysisContext,
    ): AnalysisContext = context.fail(reason = "The loops are not supported (Break statement)")

    fun analyze(
        arg: Continue,
        context: AnalysisContext,
    ): AnalysisContext = context.fail(reason = "The loops are not supported (Continue statement)")

    fun analyze(
        binaryOperation: BinaryOperation,
        context: AnalysisContext,
    ): AnalysisContext {
        val leftContext = binaryOperation.left.analyzeWith(context)
        val rightContext = binaryOperation.right.analyzeWith(leftContext)
        return rightContext.map {
            when (binaryOperation.operator) {
                Add -> leftContext.getRetValue() + (rightContext.getRetValue())
                Mult -> leftContext.getRetValue() * (rightContext.getRetValue())
                Sub -> leftContext.getRetValue() - (rightContext.getRetValue())
                Div -> leftContext.getRetValue() / (rightContext.getRetValue())
            }.let { returnResult(it) }
        }
    }

    fun analyze(
        constant: Constant,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            returnValue(
                when (constant) {
                    is Constant.BoolConstant -> PythonBool(constant.value)
                    is IntConstant -> PythonInt(constant.value)
                    is StringConstant -> PythonString(constant.value)
                    is NoneConstant -> PythonNone
                },
            )
        }

    fun analyze(
        call: Call,
        context: AnalysisContext,
    ): AnalysisContext {
        val callable = call.func.analyzeWith(context).getRetValue()
//        val args = call.arguments.fold(context) { currContext, arg -> arg.analyzeWith(currContext) }
        val args = call.arguments.map { it.analyzeWith(context).getRetValue() } // todo pass context from one to other
        val keywords = call.keywords.map { it.identifier to it.value.analyzeWith(context).getRetValue() } // todo
        return context.map {
            returnResult(callable.invoke(args, keywords, context))
        }
    }

    fun analyze(
        name: Name,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            val struct = context.getStructure(name.identifier)
            if (struct == null) {
                fail("The name $name is not known.")
            } else {
                returnValue(struct)
            }
        }

    fun analyze(
        attribute: Attribute,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            val valResultContext = attribute.value.analyzeWith(context)
            valResultContext.getRetValue().attribute(attribute.attr).let {
                returnResult(it)
            }
        }

    fun analyze(
        subscript: Subscript,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            val newContext = subscript.value.analyzeWith(context)
            val sliceContext = subscript.slice.analyzeWith(newContext)
            newContext.getRetValue().subscript(sliceContext.getRetValue()).let {
                returnResult(it)
            }
        }

    fun analyze(
        compare: Compare,
        context: AnalysisContext,
    ): AnalysisContext {
        check(compare.comparators.size == compare.operators.size)
        var resultContext = compare.left.analyzeWith(context)
        for (index: Int in 0..<compare.comparators.size) {
            val left = resultContext.getRetValue()
            resultContext = compare.comparators[index].analyzeWith(resultContext)
            val right = resultContext.getRetValue()
            when (compare.operators[index]) {
                CompareOperator.Equal -> TODO()
                CompareOperator.GreaterThan -> TODO()
                CompareOperator.GreaterThanEqual -> TODO()
                CompareOperator.In -> TODO()
                CompareOperator.Is -> TODO()
                CompareOperator.IsNot -> TODO()
                CompareOperator.LessThan -> TODO()
                CompareOperator.LessThanEqual -> TODO()
                CompareOperator.NotEqual -> TODO()
                CompareOperator.NotIn -> TODO()
            }
        }
        return resultContext
    }

    fun analyze(
        pythonList: PythonList,
        context: AnalysisContext,
    ): AnalysisContext {
        when (pythonList.context) {
            is Load -> {
                val elements = pythonList.elements.map { it.analyzeWith(context).run { getRetValue() } }
                return context.map { returnValue(python.datastructures.defaults.PythonList(elements.toMutableList())) }
            }
            is ExpressionContext.Store -> {
                TODO("Not implemented yet")
            }
            is ExpressionContext.Delete -> {
                TODO("Del not implemented")
            }
        }
    }

    fun analyze(
        dictionary: Dictionary,
        context: AnalysisContext,
    ): AnalysisContext =
        context.map {
            returnValue(
                PythonDict(
                    values =
                        dictionary.keys
                            .zip(dictionary.values)
                            .associate { (key, value) ->
                                key.analyzeWith(context).getRetValue() to value.analyzeWith(context).getRetValue() // todo pass on context
                            }
                            .toMutableMap(),
                ),
            )
        }

    fun analyze(
        boolOp: BoolOperation,
        context: AnalysisContext,
    ): AnalysisContext =
        when (boolOp.operator) {
            is And -> {
                boolOp.values.fold(
                    initial = context.map { returnValue(PythonBool(true)) },
                    operation = { ctx, value ->
                        TODO()
                    }, // todo fold only before the value is "false" then stop and return current context with false
                )
            }
            is Or -> {
                // todo fold only before the value is true
                boolOp.values.fold(
                    initial = context.map { returnValue(PythonBool(false)) },
                    operation = {
                            ctx,
                            value,
                        ->
                        TODO()
                    }, // todo fold only before the value is true, then stop and return current context with true
                )
            }
        }

    fun List<Statement>.foldStatements(initialContext: AnalysisContext): AnalysisContext =
        this.fold(
            initial = initialContext,
            operation = { acc, statement -> acc.map { returnValue(PythonNone) }.let { statement.analyzeWith(it) } },
        )

    fun List<PythonType.Expression>.foldExpressions(initialContext: AnalysisContext): Pair<Sequence<PythonDataStructure>, AnalysisContext> =
        this.fold(
            initial = emptySequence<PythonDataStructure>() to initialContext,
            operation = { (defaultsSoFar, currentContext), current ->
                val resultContext = current.analyzeWith(currentContext)
                defaultsSoFar + resultContext.getRetValue() to resultContext
            },
        )

    fun PythonType.analyzeWith(context: AnalysisContext) =
        when (this) {
            is Module -> analyze(this, context)
            is Attribute -> analyze(this, context)
            is BinaryOperation -> analyze(this, context)
            is Call -> analyze(this, context)
            is Compare -> analyze(this, context)
            is Constant -> analyze(this, context)
            is Dictionary -> analyze(this, context)
            is Name -> analyze(this, context)
            is PythonList -> analyze(this, context)
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
            is BoolOperator -> error("Called BoolOperator")
            is Operator -> error("Called Operator")
            is CompareOperator -> error("Called CompareOperator")
            is ExpressionContext -> error("Called ExpressionContext")
            is Alias -> error("Called Alias")
            is PythonType.Arg -> error("Called Arg")
            is PythonType.Arguments -> error("Called Arguments")
            is PythonType.KeywordArg -> error("Called KeywordArg")
        }
}
