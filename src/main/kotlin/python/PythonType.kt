package python

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("_type")
sealed class PythonType {
    @Serializable
    sealed class Mod : PythonType() {

        @Serializable
        @SerialName("Module")
        data class Module (
            val body: List<Statement>
            //todo type_ignore
        ) : Mod()

        //todo missing Interactive, Expression, FunctionType
    }

    @Serializable
    sealed class Statement {

        @Serializable
        @SerialName("FunctionDef")
        data class FunctionDef(
            val name: String,
//            val args: List //todo arguments
            val body: Statement
            //todo others
        ) : Statement()

        @Serializable
        @SerialName("Return")
        data class Return(
            val value: Expression,
        ) : Statement()

        @Serializable
        @SerialName("Assign")
        data class Assign (
            val targets: List<Expression>,
            val value: Expression,
            //todo type_comment?
        ) : Statement()

        @Serializable
        @SerialName("For")
        data class ForLoop (
            val target: Expression,
            @SerialName("iter")
            val iterator: Expression,
            val body: Statement,
            val orElse: Statement
            //todo type_comment?
        ) : Statement()

        @Serializable
        @SerialName("While")
        data class WhileLoop (
            val test: Expression,
            val body: Statement,
            val orElse: Statement
        ) : Statement()

        @Serializable
        @SerialName("If")
        data class IfStatement (
            val test: Expression,
            val body: Statement,
            val orElse: Statement
        ) : Statement()
        //todo missing with, match try, raise

        @Serializable
        @SerialName("Import")
        data class Import (
            val names: List<Alias>,
        ) : Statement()

        @Serializable
        @SerialName("ImportFrom")
        data class ImportFrom (
            val module: String?,
            val names: List<Alias>,
            val level: Int // todo do we need it?
        ) : Statement()

        @Serializable
        @SerialName("Expr")
        data class ExpressionStatement(
            @SerialName("value")
            val expression: Expression
        ) : Statement()

        @Serializable
        @SerialName("Break")
        class Break : Statement()

        @Serializable
        @SerialName("Continue")
        class Continue : Statement()
    }

    @Serializable
    sealed class Expression {
        //todo boolop, namedExpr

        @Serializable
        @SerialName("BinOp")
        data class BinaryOperation (
            val left: Expression,
            val right: Expression,
            val operator: Operator
        ) : Expression()

        @Serializable
        @SerialName("Constant")
        data class Constant (
            val value: Int //todo what is n, s
        ) : Expression()

        @Serializable
        @SerialName("Call")
        data class Call (
            val func: Expression,
            @SerialName("args")
            val arguments: List<Expression>,
            //todo keywords
        ) : Expression()

    }

    @Serializable
    sealed class ExpressionContext {

    }

    @Serializable
    sealed class BoolOperation {

    }

    @Serializable
    sealed class Operator {
        class Add : Statement()
        class Sub : Statement()
    }




    // old implementation
    @Serializable
    @SerialName("FunctionDef")
    data class FunctionDef (
        val name: String,
    ) : PythonType()

    @Serializable
    @SerialName("Import")
    data class Import (
        val names: List<PythonType> //todo maybe Aliases?
    ) : PythonType()

    @Serializable
    @SerialName("alias")
    data class Alias ( //todo what about col_offset, lineno...?
        @SerialName("asname")
        val aliasName: String,
        val name: String
    )


}