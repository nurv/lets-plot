package jetbrains.datalore.visualization.plot.gog.config

import jetbrains.datalore.visualization.plot.gog.DemoAndTest
import jetbrains.datalore.visualization.plot.gog.config.Option.GeomName
import kotlin.test.Test


class EdgeCasesTest {

    @Test
    fun histogramWithOnlyNullsInXSerie() {
        val data = "{" +
                "  'x': [null, null]" +
                "}"

        val spec = "{" +
                "   'data': " + data +
                "           ," +
                "   'layers': [" +
                "               {" +
                "                  'geom':  {" +
                "                             'name': 'histogram'" +
                "                           }" +
                "                   ," +
                "                  'mapping': {" +
                "                              'x': 'x'" +
                "                             }" +
                "               }" +
                "           ]" +
                "}"

        val opts = DemoAndTest.parseJson(spec)
        DemoAndTest.assertExceptionNotHappened { DemoAndTest.createPlot(opts) }
    }

    @Test
    fun pointWithOnlyNullsInXYSeries() {
        val data = "{" +
                "  'x': [null]," +
                "  'y': [null]" +
                "}"

        val spec = "{" +
                "   'data': " + data +
                "           ," +
                "   'layers': [" +
                "               {" +
                "                  'geom':  {" +
                "                             'name': 'point'" +
                "                           }" +
                "                   ," +
                "                  'mapping': {" +
                "                              'x': 'x'," +
                "                              'y': 'y'" +
                "                             }" +
                "               }" +
                "           ]" +
                "}"

        val opts = DemoAndTest.parseJson(spec)
        DemoAndTest.assertExceptionNotHappened { DemoAndTest.createPlot(opts) }
    }

    @Test
    fun lineWithNaNInXYSeries() {
        for (geomName in GeomName.values()) {
            if (GeomName.LIVE_MAP == geomName || GeomName.IMAGE == geomName) {
                continue
            }
            checkWithNaNInXYSeries(geomName)
        }
    }

    private fun checkWithNaNInXYSeries(geom: String) {
        val spec = "{" +
                //"   'data': " + data +
                //"           ," +
                "   'layers': [" +
                "               {" +
                "                  'geom':  {" +
                "                             'name': '" + geom + "'" +
                "                           }" +
                "                   ," +
                "                  'mapping': {" +
                "                              'x': 'x'," +
                "                              'y': 'y'" +
                "                             }" +
                "               }" +
                "           ]" +
                "}"

        val plotSpec = DemoAndTest.parseJson(spec)

        val data = mapOf(
                "x" to listOf(0.0, Double.NaN, 1.0, 2.0),
                "y" to listOf(0.0, Double.NaN, 1.0, 2.0)
        )

        plotSpec["data"] = data
        DemoAndTest.assertExceptionNotHappened("geom $geom: ") { DemoAndTest.createPlot(plotSpec) }
    }
}