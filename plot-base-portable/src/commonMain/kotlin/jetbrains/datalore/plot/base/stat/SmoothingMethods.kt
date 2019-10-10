package jetbrains.datalore.plot.base.stat

import jetbrains.datalore.plot.base.stat.regression.RegressionEvaluator

expect object SmoothingMethods {
    fun lm(valuesX: List<Double?>, valuesY: List<Double?>, confidenceLevel: Double): RegressionEvaluator
    fun loess(valuesX: List<Double?>, valuesY: List<Double?>, confidenceLevel: Double): RegressionEvaluator
}