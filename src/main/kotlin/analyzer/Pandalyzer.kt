package analyzer

import python.OperationResult
import python.PythonEntity
import python.PythonEntity.BoolOperator.And
import python.PythonEntity.BoolOperator.Or
import python.PythonEntity.CompareOperator
import python.PythonEntity.Expression.Attribute
import python.PythonEntity.Expression.BinaryOperation
import python.PythonEntity.Expression.BoolOperation
import python.PythonEntity.Expression.Call
import python.PythonEntity.Expression.Compare
import python.PythonEntity.Expression.Constant.BoolConstant
import python.PythonEntity.Expression.Constant.IntConstant
import python.PythonEntity.Expression.Constant.NoneConstant
import python.PythonEntity.Expression.Constant.StringConstant
import python.PythonEntity.Expression.Dictionary
import python.PythonEntity.Expression.Name
import python.PythonEntity.Expression.PythonList
import python.PythonEntity.Expression.Subscript
import python.PythonEntity.Expression.UnaryOperation
import python.PythonEntity.ExpressionContext
import python.PythonEntity.ExpressionContext.Load
import python.PythonEntity.Mod.Module
import python.PythonEntity.Operator.Add
import python.PythonEntity.Operator.Div
import python.PythonEntity.Operator.FloorDiv
import python.PythonEntity.Operator.Mult
import python.PythonEntity.Operator.Sub
import python.PythonEntity.Statement.Assign
import python.PythonEntity.Statement.Break
import python.PythonEntity.Statement.Continue
import python.PythonEntity.Statement.ExpressionStatement
import python.PythonEntity.Statement.ForLoop
import python.PythonEntity.Statement.FunctionDef
import python.PythonEntity.Statement.IfStatement
import python.PythonEntity.Statement.Import
import python.PythonEntity.Statement.ImportFrom
import python.PythonEntity.Statement.Return
import python.PythonEntity.Statement.WhileLoop
import python.PythonEntity.UnaryOperator.Invert
import python.PythonEntity.UnaryOperator.Not
import python.PythonEntity.UnaryOperator.UnaryMinus
import python.PythonEntity.UnaryOperator.UnaryPlus
import python.addWarnings
import python.arguments.ResolvedArguments.Companion.resolve
import python.datastructures.NondeterministicDataStructure
import python.datastructures.PythonDataStructure
import python.datastructures.UnresolvedStructure
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
        analyzeStatements(module.body, context)
    }

    fun analyze(
        functionDef: FunctionDef,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        val (resolvedArgs, warnings) = functionDef.args.resolve(context)
        val func =
            PythonFunc(
                name = functionDef.name,
                body = functionDef.body,
                arguments = resolvedArgs,
                functionDef = functionDef,
            )
        context.upsertStruct(functionDef.name, func)
        context.addWarnings(warnings, functionDef)
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        assign: Assign,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        val (value, warn) =
            assign.value.analyzeWith(context).orElse {
                context.addError(it, assign)
//            return StatementAnalysisResult.Ended
                UnresolvedStructure(it) // todo does it work?
            }
        context.addWarnings(warn, assign)
        when (val target = assign.targets.single()) {
            is Name -> context.upsertStruct(target.identifier, value)
            is Attribute -> {
                val (_, warns) =
                    target.value.analyzeWith(context).map { it.storeAttribute(target.attr, it) }.orElse {
                        context.addError(it, assign)
                        return StatementAnalysisResult.Ended
                    }
                context.addWarnings(warns, assign)
            }
            is Subscript -> {
                val (subscriptTarget, warns) =
                    target.value.analyzeWith(context).orElse {
                        context.addError(it, assign)
                        return StatementAnalysisResult.Ended
                    }
                context.addWarnings(warns, assign)
                val (_, sliceWarns) =
                    target.slice.analyzeWith(context).map { subscriptTarget.storeSubscript(it, value) }
                        .orElse {
                            context.addError(it, assign)
                            return StatementAnalysisResult.Ended
                        }
                context.addWarnings(sliceWarns, assign)
            }
            else -> {
                context.addError("Unsupported assign target $target", assign)
            }
        }
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        ifStatement: IfStatement,
        context: AnalysisContext,
    ): StatementAnalysisResult {
        val (ifResult, warn) =
            ifStatement.test.analyzeWith(context).orElse {
                context.addError(it, ifStatement)
                return StatementAnalysisResult.Ended
            }
        context.addWarnings(warn, ifStatement)
        if ((ifResult is PythonBool).not()) {
            context.addWarning("If statement with test value of type ${ifResult.typeName}", ifStatement)
        }
        val ifValue = ifResult.boolValue()

        return if (ifValue != null) {
            if (ifValue) {
                analyzeStatements(ifStatement.body, context)
            } else {
                analyzeStatements(ifStatement.orElse, context)
            }
        } else {
            context.addWarning("Unable to recognize the bool value in the if statement test - branching.", ifStatement)
            val clonedContext = context.fork()
            val bodyResult = analyzeStatements(ifStatement.body, context)
            val orElseResult = analyzeStatements(ifStatement.orElse, clonedContext)
            context.join(clonedContext)
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
        val importStruct = createImportStruct(importFrom.module!!, importFrom.module)
        importFrom.names.forEach { (aliasName, name) ->
            when (val result = importStruct.attribute(name)) {
                is OperationResult.Ok -> context.upsertStruct(aliasName ?: name, result.result)
                is OperationResult.Warning ->
                    context.upsertStruct(aliasName ?: name, result.result)
                        .also { context.addWarnings(result.messages, importFrom) }
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
        when (val result = expressionStatement.expression.analyzeWith(context)) {
            is OperationResult.Error -> context.addError(result.reason, expressionStatement)
            is OperationResult.Ok -> {}
            is OperationResult.Warning -> context.addWarnings(result.messages, expressionStatement)
        }
        return StatementAnalysisResult.Ended
    }

    fun analyze(
        binaryOperation: BinaryOperation,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val (left, lWarn) = binaryOperation.left.analyzeWith(context).orElse { return fail(it) }
        val (right, rWarn) = binaryOperation.right.analyzeWith(context).orElse { return fail(it) }

        return when (binaryOperation.operator) {
            Add -> left + right
            Div -> left / right
            Mult -> left * right
            Sub -> left - right
            FloorDiv -> left floorDiv right
        }.addWarnings(lWarn + rWarn)
    }

    fun analyze(
        call: Call,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val callable = call.func.analyzeWith(context)
        val (args, aWarn) = call.arguments.map { arg -> arg.analyzeWith(context).orElse { return fail(it) } }.unzip()
        val (keywords, kWarn) =
            call.keywords.map { arg ->
                arg.identifier to
                    arg.value.analyzeWith(context)
                        .orElse { return fail(it) }
            }
                .unzip()
                .let {
                    val identifiers = it.first
                    val (values, warnings) = it.second.unzip()
                    identifiers.zip(values) to warnings.flatten()
                }

        return callable.map { it.invoke(args, keywords, context) }.addWarnings(aWarn.flatten() + kWarn)
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
        val (firstLeft, warnings) = compare.left.analyzeWith(context).orElse { return fail(it) }
        return analyzeInner(firstLeft, compare.comparators, compare.operators, context)
            .addWarnings(warnings)
    }

    private fun analyzeInner(
        left: PythonDataStructure,
        rightRest: List<PythonEntity.Expression>,
        operators: List<CompareOperator>,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val rightExpr = rightRest.first()
        val (right, warnings) = rightExpr.analyzeWith(context).orElse { return fail(it) }

        val (result, resultWarnings) =
            when (operators.first()) {
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
            }.orElse { return fail(it) }
        if (rightRest.size == 1) {
            return result.ok().addWarnings(warnings + resultWarnings)
        }

        return when (result.boolValue()) {
            true -> analyzeInner(right, rightRest.drop(1), operators.drop(1), context)
            false -> PythonBool(false).ok()
            null -> {
                val forkedContext = context.fork()
                val (forkedResult, forkWarnings) =
                    analyzeInner(right, rightRest.drop(1), operators.drop(1), forkedContext)
                        .orElse { return fail(it) }
                context.join(forkedContext)
                NondeterministicDataStructure(PythonBool(false), forkedResult)
                    .withWarn("Unable to recognize the bool value in the bool operation - branching.")
                    .addWarnings(forkWarnings)
            }
        }.addWarnings(warnings + resultWarnings)
    }

    fun analyze(
        pythonList: PythonList,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        when (pythonList.context) {
            is Load -> {
                val (elements, warns) = pythonList.elements.map { el -> el.analyzeWith(context).orElse { return fail(it) } }.unzip()
                return python.datastructures.defaults.PythonList(elements.toMutableList()).ok().addWarnings(warns.flatten())
            }
            is ExpressionContext.Store -> {
                TODO("Store not implemented")
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
        val result =
            dictionary.keys.zip(dictionary.values).associate { (key, value) ->
                key.analyzeWith(context).orElse { return fail(it) } to value.analyzeWith(context).orElse { return fail(it) }
            }
        val warnings = result.flatMap { it.key.second + it.value.second }
        val map = result.map { it.key.first to it.value.first }.toMap()
        return PythonDict(map.toMutableMap()).ok().addWarnings(warnings)
    }

    fun analyze(
        boolOp: BoolOperation,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val expr = boolOp.values.firstOrNull() ?: return PythonBool(true).ok()
        val (result, warnings) = expr.analyzeWith(context).orElse { return fail(it) }
        val nextBoolOp = boolOp.copy(values = boolOp.values.drop((1)))

        return when (boolOp.operator) {
            is And -> {
                when (result.boolValue()) {
                    true -> analyze(nextBoolOp, context)
                    false -> PythonBool(false).ok().addWarnings(warnings)
                    null -> {
                        val forkedContext = context.fork()
                        val (forkedResult, forkedWarns) = analyze(nextBoolOp, forkedContext).orElse { return fail(it) }
                        context.join(forkedContext)
                        NondeterministicDataStructure(PythonBool(false), PythonBool(forkedResult.boolValue()))
                            .withWarn("Unable to recognize the bool value in the bool operation - branching.")
                            .addWarnings(warnings + forkedWarns)
                    }
                }
            }
            is Or -> {
                when (result.boolValue()) {
                    true -> PythonBool(true).ok().addWarnings(warnings)
                    false -> analyze(nextBoolOp, context)
                    null -> {
                        val forkedContext = context.fork()
                        val (forkedResult, forkedWarns) = analyze(nextBoolOp, forkedContext).orElse { return fail(it) }
                        context.join(forkedContext)
                        NondeterministicDataStructure(PythonBool(true), PythonBool(forkedResult.boolValue()))
                            .withWarn("\"Unable to recognize the bool value in the bool operation - branching.\"")
                            .addWarnings(warnings + forkedWarns)
                    }
                }
            }
        }
    }

    fun analyze(
        unaryOperation: UnaryOperation,
        context: AnalysisContext,
    ): OperationResult<PythonDataStructure> {
        val (result, warns) = unaryOperation.operand.analyzeWith(context).orElse { return fail(it) }
        return when (unaryOperation.operator) {
            Invert -> TODO("Invert operator not implemented")
            Not -> PythonBool(result.boolValue()?.not()).ok()
            UnaryMinus -> result.negate()
            UnaryPlus -> result.positive()
        }.addWarnings(warns)
    }

    fun PythonEntity.Expression.analyzeWith(context: AnalysisContext): OperationResult<PythonDataStructure> =
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

    fun PythonEntity.Statement.analyzeWith(context: AnalysisContext): StatementAnalysisResult =
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
