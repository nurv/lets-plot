package jetbrains.datalore.visualization.plot.config

import jetbrains.datalore.visualization.plot.base.Aes
import jetbrains.datalore.visualization.plot.base.DataFrame
import jetbrains.datalore.visualization.plot.builder.assemble.GuideOptions
import jetbrains.datalore.visualization.plot.builder.assemble.TypedScaleProviderMap
import jetbrains.datalore.visualization.plot.builder.coord.CoordProvider
import jetbrains.datalore.visualization.plot.builder.theme.Theme
import jetbrains.datalore.visualization.plot.config.Option.Plot.COORD
import jetbrains.datalore.visualization.plot.config.Option.Plot.THEME
import jetbrains.datalore.visualization.plot.config.PlotConfigUtil.createGuideOptionsMap
import jetbrains.datalore.visualization.plot.config.theme.ThemeConfig
import jetbrains.datalore.visualization.plot.config.transform.PlotSpecTransform
import jetbrains.datalore.visualization.plot.config.transform.encode.DataSpecEncodeTransforms
import jetbrains.datalore.visualization.plot.config.transform.migration.MoveGeomPropertiesToLayerMigration

class PlotConfigClientSide private constructor(opts: Map<String, Any>) : PlotConfig(opts) {

    internal val theme: Theme = ThemeConfig(getMap(THEME)).theme
    internal val coordProvider: CoordProvider
    internal val guideOptionsMap: Map<Aes<*>, GuideOptions>

    override val isClientSide: Boolean
        get() = true

    init {

        val coord = CoordConfig.create(get(COORD)!!)
        var coordProvider = coord.coord
        if (!hasOwn(COORD)) {
            // if coord wasn't set explicitly then geom can provide its own preferred coord system
            for (layerConfig in layerConfigs) {
                val geomProvider = layerConfig.geomProvider
                if (geomProvider.hasPreferredCoordinateSystem()) {
                    coordProvider = geomProvider.preferredCoordinateSystem
                }
            }
        }
        this.coordProvider = coordProvider
        guideOptionsMap = createGuideOptionsMap(createScaleConfigs())
    }

    override fun createLayerConfig(
            layerOptions: Map<*, *>, sharedData: DataFrame?, plotMapping: Map<*, *>,
            scaleProviderByAes: TypedScaleProviderMap): LayerConfig {

        return LayerConfig(
                layerOptions,
                sharedData!!,
                plotMapping,
                StatProto(),
                scaleProviderByAes, true)
    }

    companion object {
        fun processTransform(plotSpec: MutableMap<String, Any>): MutableMap<String, Any> {
            @Suppress("NAME_SHADOWING")
            var plotSpec = plotSpec
            val isGGBunch = isGGBunchSpec(plotSpec)

            // migration to new schema of plot specs
            // needed to support 'saved output' in old format
            // remove after reasonable period of time (24 Sep, 2018)
            val migrations = PlotSpecTransform.builderForRawSpec()
                    .change(MoveGeomPropertiesToLayerMigration.specSelector(isGGBunch), MoveGeomPropertiesToLayerMigration())
                    .build()
            plotSpec = migrations.apply(plotSpec)

            return DataSpecEncodeTransforms.clientSideDecode(isGGBunch).apply(plotSpec)
        }

        internal fun create(plotSpec: Map<String, Any>): PlotConfigClientSide {
            return PlotConfigClientSide(plotSpec)
        }
    }
}