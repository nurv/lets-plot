/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.stat

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.StatContext
import jetbrains.datalore.plot.base.stat.CorrelationUtil.correlationMatrix
import jetbrains.datalore.plot.base.stat.math3.correlationPearson
import kotlin.math.abs


class CorrelationStat : BaseStat(DEF_MAPPING) {
    var correlationMethod = DEF_CORRELATION_METHOD
    var type = DEF_TYPE

    override fun apply(data: DataFrame, statCtx: StatContext, messageConsumer: (s: String) -> Unit): DataFrame {
        if (correlationMethod != Method.PEARSON)
            throw IllegalArgumentException(
                "Unsupported correlation method: $correlationMethod (only pearson is currently available)"
            )

        val cm = correlationMatrix(data, type, ::correlationPearson)
        val vals = cm.getNumeric(Stats.CORR)
        val abs: List<Double> = vals.map { abs(it!!) }

        return cm.builder().putNumeric(Stats.CORR_ABS, abs).build()
    }

    override fun consumes(): List<Aes<*>> {
        return listOf()
    }

    enum class Method {
        PEARSON,
        SPEARMAN,
        KENDALL
    }

    enum class Type {
        FULL,
        UPPER,
        LOWER
    }

    companion object {

        private val DEF_MAPPING: Map<Aes<*>, DataFrame.Variable> = mapOf(
            Aes.X to Stats.X,
            Aes.Y to Stats.Y,
            Aes.COLOR to Stats.CORR,
            Aes.SIZE to Stats.CORR_ABS,
            Aes.LABEL to Stats.CORR
        )

        private val DEF_CORRELATION_METHOD = Method.PEARSON
        private val DEF_TYPE = Type.FULL

    }
}