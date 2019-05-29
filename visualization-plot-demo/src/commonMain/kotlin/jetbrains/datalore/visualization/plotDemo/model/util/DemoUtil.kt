package jetbrains.datalore.visualization.plotDemo.model.util

import jetbrains.datalore.base.random.RandomGaussian
import jetbrains.datalore.visualization.plot.base.Aesthetics
import jetbrains.datalore.visualization.plot.base.GeomContext
import jetbrains.datalore.visualization.plot.builder.assemble.GeomContextBuilder
import kotlin.random.Random

object DemoUtil {
    fun gauss(count: Int, seed: Long, mean: Double, stdDeviance: Double): List<Double> {
        val r = RandomGaussian(Random(seed))
        val list = ArrayList<Double>()
        for (i in 0 until count) {
            val next = r.nextGaussian() * stdDeviance + mean
            list.add(next)
        }
        return list
    }

    fun naturals(count: Int): List<Double> {
        val l = ArrayList<Double>()
        for (i in 0 until count) {
            l.add(i.toDouble())
        }
        return l
    }

    fun <T> zip(l1: List<T>, l2: List<T>): List<T> {
        val l = ArrayList<T>()
        val i1 = l1.iterator()
        val i2 = l2.iterator()
        while (i1.hasNext() || i2.hasNext()) {
            if (i1.hasNext()) {
                l.add(i1.next())
            }
            if (i2.hasNext()) {
                l.add(i2.next())
            }
        }
        return l
    }

    fun <T> fill(v: T, count: Int): List<T> {
        val l = ArrayList<T>()
        for (i in 0 until count) {
            l.add(v)
        }
        return l
    }

    fun add(l1: List<Double?>, l2: List<Double?>): List<Double?> {
        val result = ArrayList<Double?>()
        val l1_ = l1.iterator()
        val l2_ = l2.iterator()
        while (l1_.hasNext()) {
            val v1 = l1_.next()
            val v2 = l2_.next()
            if (v1 == null || v2 == null) {
                result.add(null)
            } else {
                result.add(v1 + v2)
            }
        }
        return result
    }

    fun sub(l1: List<Double?>, l2: List<Double?>): List<Double?> {
        val result = ArrayList<Double?>()
        val l1_ = l1.iterator()
        val l2_ = l2.iterator()
        while (l1_.hasNext()) {
            val v1 = l1_.next()
            val v2 = l2_.next()
            if (v1 == null || v2 == null) {
                result.add(null)
            } else {
                result.add(v1 - v2)
            }
        }
        return result
    }

    fun geomContext(aes: Aesthetics): GeomContext {
        return GeomContextBuilder().aesthetics(aes).build()
    }
}