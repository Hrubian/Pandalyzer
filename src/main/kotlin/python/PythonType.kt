package python

import analyzer.AnalysisContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("_type")
sealed interface PythonType {
    @Serializable
    sealed interface Mod : PythonType {

        @Serializable
        @SerialName("Module")
        data class Module (
            val body: List<Statement>
            //todo type_ignore
        ) : Mod

        //todo missing Interactive, Expression, FunctionType
    }

    @Serializable
    sealed interface Statement : PythonType {

        @Serializable
        @SerialName("FunctionDef")
        data class FunctionDef(
            val name: String,
//            val args: List //todo arguments
            val body: List<Statement>,
            //todo others
        ) : Statement

        @Serializable
        @SerialName("Return")
        data class Return(
            val value: Expression,
        ) : Statement

        @Serializable
        @SerialName("Assign")
        data class Assign (
            val targets: List<Expression>,
            val value: Expression,
            //todo type_comment?
        ) : Statement

        @Serializable
        @SerialName("For")
        data class ForLoop (
            val target: Expression,
            @SerialName("iter")
            val iterator: Expression,
            val body: Statement,
            val orElse: Statement
            //todo type_comment?
        ) : Statement

        @Serializable
        @SerialName("While")
        data class WhileLoop (
            val test: Expression,
            val body: Statement,
            val orElse: Statement
        ) : Statement

        @Serializable
        @SerialName("If")
        data class IfStatement (
            val test: Expression,
            val body: List<Statement>,
            val orElse: List<Statement>,
        ) : Statement

        @Serializable
        @SerialName("Import")
        data class Import (
            val names: List<Alias>,
        ) : Statement

        @Serializable
        @SerialName("ImportFrom")
        data class ImportFrom (
            val module: String?,
            val names: List<Alias>,
            val level: Int // todo do we need it?
        ) : Statement

        @Serializable
        @SerialName("Expr")
        data class ExpressionStatement(
            @SerialName("value")
            val expression: Expression
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
        //todo boolop, namedExpr

        @Serializable
        @SerialName("BinOp")
        data class BinaryOperation (
            val left: Expression,
            val right: Expression,
            val operator: Operator
        ) : Expression

        @Serializable
        @SerialName("Constant")
        data class Constant (
            val value: String//todo what is n, s
        ) : Expression

        @Serializable
        @SerialName("Call")
        data class Call (
            val func: Expression,
            @SerialName("args")
            val arguments: List<Expression>,
            //todo keywords
        ) : Expression

        @Serializable
        @SerialName("Name")
        data class Name (
            @SerialName("id")
            val identifier: String
        ) : Expression

        @Serializable
        @SerialName("Attribute")
        data class Attribute (
            val value: Expression,
            val attr: String
        ) : Expression

        @Serializable
        @SerialName("Subscript")
        data class Subscript (
            val value: Expression,
            val slice: Expression,
        ) : Expression

        @Serializable
        @SerialName("Compare")
        data class Compare (
            val left: Expression,
            //todo
        ) : Expression

        @Serializable
        @SerialName("List")
        data class PythonList (
            @SerialName("elts")
            val elements: List<Expression>,
        ) : Expression

        @Serializable
        @SerialName("Dict")
        data class Dictionary (
            val keys: List<Expression>,
            val values: List<Expression>,
        ) : Expression
    }


    @Serializable
    sealed interface BoolOperation : PythonType {
        @Serializable
        @SerialName("And")
        data object And : BoolOperation
        @Serializable
        @SerialName("Or")
        data object Or : BoolOperation
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
    }

    @Serializable
    @SerialName("alias")
    data class Alias ( //todo what about col_offset, lineno...?
        @SerialName("asname")
        val aliasName: String?,
        val name: String
    ) : PythonType
}