package analyzer

import python.OperationResult
import python.PythonType
import python.PythonType.BoolOperator.And
import python.PythonType.BoolOperator.Or
import python.PythonType.CompareOperator
import python.PythonType.Expression.Attribute
import python.PythonType.Expression.BinaryOperation
import python.PythonType.Expression.BoolOperation
import python.PythonType.Expression.Call
import python.PythonType.Expression.Compare
import python.PythonType.Expression.Constant.BoolConstant
import python.PythonType.Expression.Constant.IntConstant
import python.PythonType.Expression.Constant.NoneConstant
import python.PythonType.Expression.Constant.StringConstant
import python.PythonType.Expression.Dictionary
import python.PythonType.Expression.Name
import python.PythonType.Expression.PythonList
import python.PythonType.Expression.Subscript
import python.PythonType.Expression.UnaryOperation
import python.PythonType.ExpressionContext
import python.PythonType.ExpressionContext.Load
import python.PythonType.Mod.Module
import python.PythonType.Operator.Add
import python.PythonType.Operator.Div
import python.PythonType.Operator.FloorDiv
import python.PythonType.Operator.Mult
import python.PythonType.Operator.Sub
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
import python.PythonType.UnaryOperator.Invert
import python.PythonType.UnaryOperator.Not
import python.PythonType.UnaryOperator.UnaryMinus
import python.PythonType.UnaryOperator.UnaryPlus
import python.arguments.ResolvedArguments.Companion.resolve
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.createImportStruct
import python.datastructures.defaults.PythonBool
import python.datastructures.defaults.PythonDict
import python.datastructures.defaults.PythonFunc
import python.datastructures.defaults.PythonInt
import python.datastructures.defaults.PythonNone
import python.datastructures.defaults.PythonString
import python.fail
import python.map
import python.ok
import python.orElse
import python.withWarn

object Pandalyzer {
    fun analyze(
        module: Module,
        context: AnalysisContext,
    ) {
        analyzeStatements(module.body, context) // todo do something with the result? :)
    }

    fun analyze(
        functionDef: FunctionDef,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        val resolvedArgs = functionDef.args.resolve(context)
        val func =
            PythonFunc(
                name = functionDef.name,
                body = functionDef.body,
                arguments = resolvedArgs,
            )
        context.upsertStruct(functionDef.name, func)
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        assign: Assign,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        // todo check if the assignment is type hint
        val identifier = (assign.targets.first() as Name).identifier
        val value =
            assign.value.analyzeWith(context).orElse {
                context.addError(it, assign)
                return StatementAnalysisResult.Ended
            }

        context.upsertStruct(identifier, value)
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        ifStatement: IfStatement,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        val ifResult =
            ifStatement.test.analyzeWith(context).orElse {
                context.addError(it, ifStatement)
                return StatementAnalysisResult.Ended
            }.also { it as? PythonBool ?: context.addWarning("If statement with test value of type ${it.typeName}", ifStatement) }
                .boolValue()

        return if (ifResult != null) {
            if (ifResult) {
                analyzeStatements(ifStatement.body, context)
            } else {
                analyzeStatements(ifStatement.orElse, context)
            }
        } else {
            context.addWarning("Unable to recognize the bool value in the if statement test - branching.", ifStatement)
            val clonedContext = context.clone()
            val bodyResult = analyzeStatements(ifStatement.body, context)
            val orElseResult = analyzeStatements(ifStatement.orElse, clonedContext)
            context.merge(clonedContext)
            StatementAnalysisResult.NondeterministicResult(bodyResult, orElseResult)
        }
    }

    fun analyze(
        import: Import,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        import.names.forEach { (aliasName, name) ->
            context.upsertStruct(aliasName ?: name, createImportStruct(name, aliasName ?: name))
        }
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        importFrom: ImportFrom,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        val importStruct = createImportStruct(importFrom.module!!, importFrom.module) // todo resolve "!!"
        importFrom.names.forEach { (aliasName, name) ->
            when (val result = importStruct.attribute(name)) {
                is OperationResult.Ok -> context.upsertStruct(aliasName ?: name, result.result)
                is OperationResult.Warning ->
                    context.upsertStruct(aliasName ?: name, result.result)
                        .also { context.addWarning(result.message, importFrom) }
                is OperationResult.Error ->
                    context.addError("The package ${importFrom.module} does not contain $name.", importFrom)
            }
        }
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        expressionStatement: ExpressionStatement,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        expressionStatement.expression.analyzeWith(context)
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        binaryOperation: BinaryOperation,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val left = binaryOperation.left.analyzeWith(context).orElse { return fail(it) }
        val right = binaryOperation.right.analyzeWith(context).orElse { return fail(it) }

        return when (binaryOperation.operator) {
            Add -> left + right
            Div -> left / right
            Mult -> left * right
            Sub -> left - right
            FloorDiv -> left floorDiv right
        }
    }

    fun analyze(
        call: Call,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val callable = call.func.analyzeWith(context)
        val args = call.arguments.map { arg -> arg.analyzeWith(context).orElse { return fail(it) } }
        val keywords = call.keywords.map { arg -> arg.identifier to arg.value.analyzeWith(context).orElse { return fail(it) } }
        return callable.map { it.invoke(args, keywords, context) }
    }

    fun analyze(
        name: Name,
        context: AnalysisContext,
    ) = context.getStruct(name.identifier)?.ok()
        ?: fail("The name $name is not known.")

    fun analyze(
        compare: Compare,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        check(compare.comparators.size == compare.operators.size)
        var left = compare.left.analyzeWith(context).orElse { return fail(it) }
        var op = compare.operators.first()
        var right = compare.comparators.first().analyzeWith(context).orElse { return fail(it) }
        var rightIndex = 0

        while (true) {
            val result =
                when (op) {
                    CompareOperator.Equal -> left equal right
                    CompareOperator.GreaterThan -> left greaterThan right
                    CompareOperator.GreaterThanEqual -> left greaterThanEqual right
                    CompareOperator.In -> left inn right
                    CompareOperator.Is -> left iss right
                    CompareOperator.IsNot -> left isNot right
                    CompareOperator.LessThan -> left lessThan right
                    CompareOperator.LessThanEqual -> left lessThanEqual right
                    CompareOperator.NotEqual -> left notEqual right
                    CompareOperator.NotIn -> left notIn right
                }
            val boolResult = result.orElse { return fail(it) }.boolValue()
            if (boolResult != null) {
                if (boolResult) {
                    left = right
                    rightIndex++
                    op = compare.operators.getOrNull(rightIndex) ?: break
                    right = compare.comparators[rightIndex].analyzeWith(context).orElse { return fail(it) }
                } else {
                    return PythonBool(false).ok() // short-circuit
                }
            } else {
                TODO("implement non-determinism on Compare")
            }
        }

        return PythonBool(true).ok()
    }

    fun analyze(
        pythonList: PythonList,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        when (pythonList.context) {
            is Load -> {
                val elements = pythonList.elements.map { el -> el.analyzeWith(context).orElse { return fail(it) } }
                return python.datastructures.defaults.PythonList(elements.toMutableList()).ok()
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
    ): OperationResult<PythonDataStructure> {
        val map =
            dictionary.keys.zip(dictionary.values).associate { (key, value) ->
                key.analyzeWith(context).orElse { return fail(it) } to value.analyzeWith(context).orElse { return fail(it) }
            }
        return PythonDict(map.toMutableMap()).ok()
    }

    fun analyze(
        boolOp: BoolOperation,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val first = boolOp.values.getOrNull(0) ?: return fail("")
        val second = boolOp.values.getOrNull(1) ?: return fail("")
        if (boolOp.values.size != 2) {
            return fail("Only binary bool operations are supported")
        }
        val firstResult = first.analyzeWith(context).orElse { return fail(it) }.boolValue()

        return when (boolOp.operator) {
            is And -> {
                when (firstResult) {
                    true -> {
                        val secondResult = second.analyzeWith(context).orElse { return fail(it) }.boolValue()
                        PythonBool(secondResult).ok()
                    }
                    false -> return PythonBool(false).ok()
                    null -> {
                        val clonedContext = context.clone()
                        val secondResult = second.analyzeWith(clonedContext).orElse { return fail(it) }
                        context.merge(clonedContext)
                        NondeterministicDataStructure(PythonBool(false), PythonBool(secondResult.boolValue()))
                            .withWarn("\"Unable to recognize the bool value in the bool and operation - branching.\"")
                    }
                }
            }
            is Or -> {
                when (firstResult) {
                    true -> return PythonBool(true).ok()
                    false -> {
                        val secondResult = second.analyzeWith(context).orElse { return fail(it) }.boolValue()
                        PythonBool(secondResult).ok()
                    }
                    null -> {
                        val clonedContext = context.clone()
                        val secondResult = second.analyzeWith(clonedContext).orElse { return fail(it) }
                        context.merge(clonedContext)
                        NondeterministicDataStructure(PythonBool(true), PythonBool(secondResult.boolValue()))
                            .withWarn("Unable to recognize the bool value in the bool and operation - branching.")
                    }
                }
            }
        }
    }

    fun analyze(
        unaryOperation: UnaryOperation,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val result = unaryOperation.operand.analyzeWith(context).orElse { return fail(it) }
        return when (unaryOperation.operator) {
            Invert -> TODO("Invert operator not implemented")
            Not -> PythonBool(result.boolValue()?.not()).ok()
            UnaryMinus -> result.negate()
            UnaryPlus -> result.positive()
        }
    }

    fun PythonType.Expression.analyzeWith(context: AnalysisContext): OperationResult<PythonDataStructure> =
        when (this) {
            is Name -> analyze(this, context)
            is Attribute -> value.analyzeWith(context).map { it.attribute(attr) }
            is BinaryOperation -> analyze(this, context)
            is BoolOperation -> analyze(this, context)
            is Call -> analyze(this, context)
            is Compare -> analyze(this, context)
            is BoolConstant -> PythonBool(this.value).ok()
            is IntConstant -> PythonInt(this.value).ok()
            is NoneConstant -> PythonNone.ok()
            is StringConstant -> PythonString(this.value).ok()
            is Dictionary -> analyze(this, context)
            is PythonList -> analyze(this, context)
            is Subscript ->
                value.analyzeWith(context).map { vlu ->
                    slice.analyzeWith(context).map { slc -> vlu.subscript(slc) }
                }
            is UnaryOperation -> analyze(this, context)
        }

    fun PythonType.Statement.analyzeWith(context: AnalysisContext): StatementAnalysisResult =
        when (this) {
            is Assign -> analyze(this, context)
            is Break -> error("Return should be processed via analyzeStatements function")
            is Continue -> error("Return should be processed via analyzeStatements function")
            is ExpressionStatement -> analyze(this, context)
            is ForLoop -> TODO()
            is FunctionDef -> analyze(this, context)
            is IfStatement -> analyze(this, context)
            is Import -> analyze(this, context)
            is ImportFrom -> analyze(this, context)
            is Return -> error("Return should be processed via analyzeStatements function")
            is WhileLoop -> TODO()
        }
}
