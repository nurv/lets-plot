package jetbrains.datalore.visualization.plot.gog.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataFrameUtilTest {
    @Test
    fun originalVariablesPreserved() {
        val x0 = DataFrame.Variable("x")
        val df0 = DataFrame.Builder()
                .putNumeric(x0, listOf(0.0, 1.0))
                .build()

        val df1 = DataFrame.Builder()
                .putNumeric(DataFrame.Variable("x"), listOf(2.0, 3.0, 4.0))
                .build()

        val result = DataFrameUtil.appendReplace(df0, df1)
        assertTrue(result.has(x0))
        assertEquals(listOf(2.0, 3.0, 4.0), result.getNumeric(x0))
    }

}