import analyzer.ContextBuilder
import analyzer.Pandalyzer
import org.junit.jupiter.api.Test
import python.PythonType
import python.PythonType.Expression.*
import python.PythonType.Expression.Constant.IntConstant
import python.PythonType.ExpressionContext.Load
import python.PythonType.Mod.Module
import python.PythonType.Statement.*
import python.datastructures.PythonInt
import java.math.BigInteger

class ModuleAnalysisTest {

    @Test
    fun `basic module analysis without function invocations`() {
        val module =
            Module(
                body = listOf(
                    Assign(
                        targets = listOf(Name(identifier = "a", context = Load)),
                        value = IntConstant(BigInteger.ONE)
                    ),
                    Assign(
                        targets = listOf(Name(identifier = "a", context = Load)),
                        value = IntConstant(BigInteger.TWO)
                    ),
                    Assign(
                        targets = listOf(Name(identifier = "b", context = Load)),
                        value = IntConstant(BigInteger.valueOf(3))
                    ),
                    Assign(
                        targets = listOf(Name(identifier = "result", context = Load)),
                        value = BinaryOperation(
                            left = Name(identifier = "a", context = Load),
                            right = Name(identifier = "b", context = Load),
                            operator = PythonType.Operator.Add
                        )
                    )
                )
            )

        val resultContext = Pandalyzer().analyze(module, ContextBuilder.buildEmpty())

        val result = resultContext.getStructure("result")
        assert(
            result is PythonInt &&
            result.value == BigInteger.valueOf(5)
        )
    }

    @Test
    fun `function invocation with return value works`() {
        val module =
            Module(
                body = listOf(
                    FunctionDef(
                        name = "foo",
                        body = listOf(
                            Assign(
                                targets = listOf(Name(identifier = "a", context = Load)),
                                value = IntConstant(BigInteger.TWO)
                            ),
                            Assign(
                                targets = listOf(Name(identifier = "b", context = Load)),
                                value = IntConstant(BigInteger.valueOf(3))
                            ),
                            Assign(
                                targets = listOf(Name(identifier = "result", context = Load)),
                                value = BinaryOperation(
                                    left = Name(identifier = "a", context = Load),
                                    right = Name(identifier = "b", context = Load),
                                    operator = PythonType.Operator.Add
                                )
                            ),
                            Return(
                                Name(identifier = "result", context = Load)
                            )
                        )
                    ),
                    Assign(
                        targets = listOf(Name("func_result", Load)),
                        value = Call(
                            func = Name("foo", Load),
                            arguments = emptyList()
                        )
                    )
                )
        )

        val resultContext = Pandalyzer().analyze(module, ContextBuilder.buildEmpty())

        val result = resultContext.getStructure("func_result")
        assert(
            result is PythonInt &&
            result.value == BigInteger.valueOf(5)
        )
    }

    @Test
    fun `function invocations with arguments passed works`() {
        val module =
            Module(
                body = listOf(
                    Assign(
                        targets = listOf(Name(identifier = "global_var", context = Load)),
                        value = IntConstant(BigInteger.ONE)
                    ),
                    FunctionDef(
                        name = "foo",
                        body = listOf(
                            Assign(
                                targets = listOf(Name(identifier = "local_var", context = Load)),
                                value = IntConstant(BigInteger.TWO)
                            ),
                            Assign(
                                targets = listOf(Name(identifier = "result", context = Load)),
                                value = BinaryOperation(
                                    left = Name(identifier = "a", context = Load),
                                    right = Name(identifier = "b", context = Load),
                                    operator = PythonType.Operator.Add
                                )
                            )
                        )
                    ),
                    Assign(
                        targets = listOf(Name("func_result", Load)),
                        value = Call(
                            func = Name("foo", Load),
                            arguments = listOf()
                        )
                    )
                )
            )
    }
}