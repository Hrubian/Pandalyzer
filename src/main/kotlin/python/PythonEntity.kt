@file:UseSerializers(BigIntegerSerializer::class)

package python

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import java.math.BigInteger

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("_type")
sealed interface PythonEntity {
    @Serializable
    sealed interface Mod : PythonEntity {
        @Serializable
        @SerialName("Module")
        data class Module(
            val body: List<Statement>,
        ) : Mod
    }

    @Serializable
    sealed interface Statement : PythonEntity {
        @SerialName("lineno")
        val startLine: Int

        @SerialName("end_lineno")
        val endLine: Int

        @SerialName("col_offset")
        val columnStart: Int

        @SerialName("end_col_offset")
        val columnEnd: Int

        @Serializable
        @SerialName("FunctionDef")
        data class FunctionDef(
            val name: String,
            val args: Arguments,
            val body: List<Statement>,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("Return")
        data class Return(
            val value: Expression?,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("Assign")
        data class Assign(
            val targets: List<Expression>,
            val value: Expression,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("For")
        data class ForLoop(
            val target: Expression,
            @SerialName("iter")
            val iterator: Expression,
            val body: Statement,
            val orElse: Statement,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("While")
        data class WhileLoop(
            val test: Expression,
            val body: List<Statement>,
            val orElse: Statement? = null,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("If")
        data class IfStatement(
            val test: Expression,
            val body: List<Statement>,
            @SerialName("orelse")
            val orElse: List<Statement>,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("Import")
        data class Import(
            val names: List<Alias>,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("ImportFrom")
        data class ImportFrom(
            val module: String?,
            val names: List<Alias>,
            val level: Int,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("Expr")
        data class ExpressionStatement(
            @SerialName("value")
            val expression: Expression,
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("Break")
        data class Break(
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement

        @Serializable
        @SerialName("Continue")
        data class Continue(
            @SerialName("lineno")
            override val startLine: Int,
            @SerialName("end_lineno")
            override val endLine: Int,
            @SerialName("col_offset")
            override val columnStart: Int,
            @SerialName("end_col_offset")
            override val columnEnd: Int,
        ) : Statement
    }

    @Serializable
    sealed interface Expression : PythonEntity {
        @Serializable
        @SerialName("BoolOp")
        data class BoolOperation(
            @SerialName("op")
            val operator: BoolOperator,
            val values: List<Expression>,
        ) : Expression

        @Serializable
        @SerialName("UnaryOp")
        data class UnaryOperation(
            @SerialName("op")
            val operator: UnaryOperator,
            val operand: Expression,
        ) : Expression

        @Serializable
        @SerialName("BinOp")
        data class BinaryOperation(
            val left: Expression,
            val right: Expression,
            @SerialName("op")
            val operator: Operator,
        ) : Expression

        @Serializable
        sealed interface Constant : Expression {
            @Serializable
            @SerialName("StringConstant")
            data class StringConstant(
                val value: String,
            ) : Constant

            @Serializable
            @SerialName("IntConstant")
            data class IntConstant(
                val value: BigInteger,
            ) : Constant

            @Serializable
            @SerialName("BoolConstant")
            data class BoolConstant(
                val value: Boolean,
            ) : Constant

            @Serializable
            @SerialName("FloatConstant")
            data class FloatConstant(
                val value: Double,
            ) : Constant

            @Serializable
            @SerialName("NoneConstant")
            data object NoneConstant : Constant
        }

        @Serializable
        @SerialName("Call")
        data class Call(
            val func: Expression,
            @SerialName("args")
            val arguments: List<Expression>,
            val keywords: List<KeywordArg>,
        ) : Expression

        @Serializable
        @SerialName("Name")
        data class Name(
            @SerialName("id")
            val identifier: String,
            @SerialName("ctx")
            val context: ExpressionContext,
        ) : Expression

        @Serializable
        @SerialName("Attribute")
        data class Attribute(
            val value: Expression,
            val attr: String,
            @SerialName("ctx")
            val context: ExpressionContext,
        ) : Expression

        @Serializable
        @SerialName("Subscript")
        data class Subscript(
            val value: Expression,
            val slice: Expression,
            @SerialName("ctx")
            val context: ExpressionContext,
        ) : Expression

        @Serializable
        @SerialName("Compare")
        data class Compare(
            val left: Expression,
            @SerialName("ops")
            val operators: List<CompareOperator>,
            val comparators: List<Expression>,
        ) : Expression

        @Serializable
        @SerialName("List")
        data class PythonList(
            @SerialName("elts")
            val elements: List<Expression>,
            @SerialName("ctx")
            val context: ExpressionContext,
        ) : Expression

        @Serializable
        @SerialName("Dict")
        data class Dictionary(
            val keys: List<Expression>,
            val values: List<Expression>,
        ) : Expression
    }

    @Serializable
    sealed interface BoolOperator : PythonEntity {
        @Serializable
        @SerialName("And")
        data object And : BoolOperator

        @Serializable
        @SerialName("Or")
        data object Or : BoolOperator
    }

    @Serializable
    sealed interface Operator : PythonEntity {
        @Serializable
        @SerialName("Add")
        data object Add : Operator

        @Serializable
        @SerialName("Sub")
        data object Sub : Operator

        @Serializable
        @SerialName("Mult")
        data object Mult : Operator

        @Serializable
        @SerialName("Div")
        data object Div : Operator

        @Serializable
        @SerialName("FloorDiv")
        data object FloorDiv : Operator
    }

    @Serializable
    sealed interface UnaryOperator : PythonEntity {
        @Serializable
        @SerialName("Invert")
        data object Invert : UnaryOperator

        @Serializable
        @SerialName("Not")
        data object Not : UnaryOperator

        @Serializable
        @SerialName("UAdd")
        data object UnaryPlus : UnaryOperator

        @Serializable
        @SerialName("USub")
        data object UnaryMinus : UnaryOperator
    }

    @Serializable
    @SerialName("alias")
    data class Alias(
        @SerialName("asname")
        val aliasName: String?,
        val name: String,
    ) : PythonEntity

    @Serializable
    sealed interface ExpressionContext : PythonEntity {
        @Serializable
        @SerialName("Load")
        data object Load : ExpressionContext

        @Serializable
        @SerialName("Store")
        data object Store : ExpressionContext

        @Serializable
        @SerialName("Del")
        data object Delete : ExpressionContext
    }

    @Serializable
    sealed interface CompareOperator : PythonEntity {
        // Eq | NotEq | Lt | LtE | Gt | GtE | Is | IsNot | In | NotIn
        @Serializable
        @SerialName("Eq")
        data object Equal : CompareOperator

        @Serializable
        @SerialName("NotEq")
        data object NotEqual : CompareOperator

        @Serializable
        @SerialName("Lt")
        data object LessThan : CompareOperator

        @Serializable
        @SerialName("LtE")
        data object LessThanEqual : CompareOperator

        @Serializable
        @SerialName("Gt")
        data object GreaterThan : CompareOperator

        @Serializable
        @SerialName("GtE")
        data object GreaterThanEqual : CompareOperator

        @Serializable
        @SerialName("Is")
        data object Is : CompareOperator

        @Serializable
        @SerialName("IsNot")
        data object IsNot : CompareOperator

        @Serializable
        @SerialName("In")
        data object In : CompareOperator

        @Serializable
        @SerialName("NotIn")
        data object NotIn : CompareOperator
    }

    @Serializable
    data class Arguments(
        @SerialName("posonlyargs")
        val positionalArgs: List<Arg>,
        @SerialName("args")
        val arguments: List<Arg>,
        @SerialName("vararg")
        val variadicArg: Arg?,
        @SerialName("kwargs")
        val keywordVariadicArg: Arg? = null,
        @SerialName("kwonlyargs")
        val keywordOnlyArgs: List<Arg>,
        @SerialName("kw_defaults")
        val keywordDefaults: List<Expression?>,
        val defaults: List<Expression>,
    ) : PythonEntity

    @Serializable
    data class Arg(
        @SerialName("arg")
        val identifier: String,
    ) : PythonEntity

    @Serializable
    data class KeywordArg(
        @SerialName("arg")
        val identifier: String,
        val value: Expression,
    ) : PythonEntity
}
