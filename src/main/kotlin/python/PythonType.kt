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
sealed interface PythonType {
    @Serializable
    sealed interface Mod : PythonType {
        @Serializable
        @SerialName("Module")
        data class Module(
            val body: List<Statement>,
            // todo type_ignore
        ) : Mod

        // todo missing Interactive, Expression, FunctionType
    }

    @Serializable
    sealed interface Statement : PythonType {
        @Serializable
        @SerialName("FunctionDef")
        data class FunctionDef(
            val name: String,
            val args: Arguments,
//            val args: List //todo arguments
            val body: List<Statement>,
            // todo others
        ) : Statement

        @Serializable
        @SerialName("Return")
        data class Return(
            val value: Expression,
        ) : Statement

        @Serializable
        @SerialName("Assign")
        data class Assign(
            val targets: List<Expression>,
            val value: Expression,
            // todo type_comment?
        ) : Statement

        @Serializable
        @SerialName("For")
        data class ForLoop(
            val target: Expression,
            @SerialName("iter")
            val iterator: Expression,
            val body: Statement,
            val orElse: Statement,
            // todo type_comment?
        ) : Statement

        @Serializable
        @SerialName("While")
        data class WhileLoop(
            val test: Expression,
            val body: Statement,
            val orElse: Statement,
        ) : Statement

        @Serializable
        @SerialName("If")
        data class IfStatement(
            val test: Expression,
            val body: List<Statement>,
            @SerialName("orelse")
            val orElse: List<Statement>,
        ) : Statement

        @Serializable
        @SerialName("Import")
        data class Import(
            val names: List<Alias>,
        ) : Statement

        @Serializable
        @SerialName("ImportFrom")
        data class ImportFrom(
            val module: String?,
            val names: List<Alias>,
            val level: Int, // todo do we need it?
        ) : Statement

        @Serializable
        @SerialName("Expr")
        data class ExpressionStatement(
            @SerialName("value")
            val expression: Expression,
        ) : Statement

        @Serializable
        @SerialName("Break")
        data object Break : Statement

        @Serializable
        @SerialName("Continue")
        data object Continue : Statement
    }

    @Serializable
    sealed interface Expression : PythonType {
        // todo boolop, namedExpr

        @Serializable
        @SerialName("BoolOp")
        data class BoolOperation(
            val operator: BoolOperator,
            val values: List<Expression>,
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
    sealed interface BoolOperator : PythonType {
        @Serializable
        @SerialName("And")
        data object And : BoolOperator

        @Serializable
        @SerialName("Or")
        data object Or : BoolOperator
    }

    @Serializable
    sealed interface Operator : PythonType {
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
    }

    @Serializable
    @SerialName("alias")
    data class Alias( // todo what about col_offset, lineno...?
        @SerialName("asname")
        val aliasName: String?,
        val name: String,
    ) : PythonType

    @Serializable
    sealed interface ExpressionContext : PythonType {
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
    sealed interface CompareOperator : PythonType {
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
    ) : PythonType

    @Serializable
    data class Arg(
        @SerialName("arg")
        val identifier: String,
    ) : PythonType

    @Serializable
    data class KeywordArg(
        @SerialName("arg")
        val identifier: String,
        val value: Expression,
    ) : PythonType
}
