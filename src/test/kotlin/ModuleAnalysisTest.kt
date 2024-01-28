import analyzer.ContextBuilder
import analyzer.Pandalyzer
import org.junit.jupiter.api.Test
import python.PythonType
import python.PythonType.Expression.BinaryOperation
import python.PythonType.Expression.Constant.IntConstant
import python.PythonType.Expression.Name
import python.PythonType.ExpressionContext.Load
import python.PythonType.Mod.Module
import python.PythonType.Statement.Assign
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
}