package python.arguments

import org.junit.jupiter.api.Test
import python.PythonType
import python.PythonType.Arguments
import python.PythonType.Statement.FunctionDef

class ArgumentMatcherTest {

    @Test
    fun `positional arguments`() {
        val function = FunctionDef(
            name = "test_function",
            args = Arguments(

            )
        )

        val call = PythonType.Expression.Call(

        )
    }
}