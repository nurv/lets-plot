package jetbrains.livemap.api

import jetbrains.datalore.base.async.Async
import jetbrains.datalore.base.async.Asyncs.constant
import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.projectionGeometry.*
import jetbrains.datalore.base.values.Color
import jetbrains.gis.geoprotocol.*
import jetbrains.gis.tileprotocol.TileLayer
import jetbrains.gis.tileprotocol.TileService
import jetbrains.gis.tileprotocol.socket.Socket
import jetbrains.gis.tileprotocol.socket.SocketBuilder
import jetbrains.gis.tileprotocol.socket.SocketHandler
import jetbrains.gis.tileprotocol.socket.TileWebSocketBuilder
import jetbrains.livemap.DevParams
import jetbrains.livemap.LiveMapSpec
import jetbrains.livemap.MapLocation
import jetbrains.livemap.entities.geometry.LonLatGeometry
import jetbrains.livemap.mapobjects.*
import jetbrains.livemap.mapobjects.MapLayerKind.*
import jetbrains.livemap.mapobjects.Utils.splitMapBarChart
import jetbrains.livemap.mapobjects.Utils.splitMapPieChart
import jetbrains.livemap.projections.*
import kotlin.math.abs

@DslMarker
annotation class LiveMapDsl {}

@LiveMapDsl
class LiveMapBuilder {
    lateinit var size: DoubleVector
    lateinit var geocodingService: GeocodingService
    lateinit var tileService: TileService

    var zoom: Int? = null
    var interactive: Boolean = true
    var mapLocation: MapLocation? = null
    var level: FeatureLevel? = null
    var parent: MapRegion? = null
    var layers: MutableList<MapLayer> = ArrayList()

    var projectionType: ProjectionType = ProjectionType.MERCATOR
    var isLoopX: Boolean = true
    var isLoopY: Boolean = false

    var mapLocationConsumer: (DoubleRectangle) -> Unit = { _ -> Unit }
    var devParams: Map<String, Any> = HashMap()


    fun params(vararg values: Pair<String, Any>) {
        devParams = mapOf(*values)
    }

    fun build(): LiveMapSpec {
        return LiveMapSpec(
            size = size,
            zoom = zoom,
            isInteractive = interactive,
            layers = layers,

            level = level,
            location = mapLocation,
            parent = parent,

            projectionType = projectionType,
            isLoopX = isLoopX,
            isLoopY = isLoopY,

            geocodingService = geocodingService,

            mapLocationConsumer = mapLocationConsumer,

            devParams = DevParams(devParams),

            // deprecated
            isClustering = false,
            isEnableMagnifier = false,
            isLabels = true,
            isScaled = false,
            isTiles = true,
            isUseFrame = true
        )
    }
}

@LiveMapDsl
class LayersBuilder {
    val items = ArrayList<MapLayer>()
}

@LiveMapDsl
class Points {
    val items = ArrayList<MapPoint>()
}

@LiveMapDsl
class Paths {
    val items = ArrayList<MapPath>()
}

@LiveMapDsl
class Polygons {
    val items = ArrayList<MapPolygon>()
}

@LiveMapDsl
class Lines {
    val items = ArrayList<MapLine>()
}

@LiveMapDsl
class Bars {
    val factory = BarsFactory()
}

@LiveMapDsl
class Pies {
    val factory = PiesFactory()
}

@LiveMapDsl
class Texts {
    val items = ArrayList<MapText>()
}

@LiveMapDsl
class PointBuilder {
    var animation: Int? = null
    var label: String? = null
    var shape: Int? = null
    var lat: Double? = null
    var lon: Double? = null
    var radius: Double? = null
    var fillColor: Color? = null
    var strokeColor: Color? = null
    var strokeWidth: Double? = null
    var index: Int? = null
    var mapId: String? = null
    var regionId: String? = null
    fun build(): MapPoint {
        return MapPoint(
            index!!,
            mapId,
            regionId,
            explicitVec<LonLat>(lon!!, lat!!),
            label!!,
            animation!!,
            shape!!,
            radius!!,
            fillColor!!,
            strokeColor!!,
            strokeWidth!!
        )
    }
}

@LiveMapDsl
class PathBuilder {
    var index: Int? = null
    var mapId: String? = null
    var regionId: String? = null

    var lineDash: List<Double>? = null
    var strokeColor: Color? = null
    var strokeWidth: Double? = null
    var coordinates: List<Vec<LonLat>>? = null

    var animation: Int? = null
    var speed: Double? = null
    var flow: Double? = null

    var geodesic: Boolean? = null

    fun build(): MapPath {
        val coord = coordinates.takeIf { !geodesic!! } ?: createArcPath(coordinates!!)

        return MapPath(
            index!!, mapId, regionId,
            coord
                .run { LonLatRing(this) }
                .run { LonLatPolygon(listOf(this)) }
                .run { LonLatMultiPolygon(listOf(this)) }
                .run { LonLatGeometry.create(this) },
            animation!!, speed!!,
            flow!!, lineDash!!, strokeColor!!,
            strokeWidth!!
        )
    }
}



@LiveMapDsl
class PolygonsBuilder {
    var index: Int? = null
    var mapId: String? = null
    var regionId: String? = null

    var lineDash: List<Double>? = null
    var strokeColor: Color? = null
    var strokeWidth: Double? = null
    var fillColor: Color? = null
    var coordinates: List<Vec<LonLat>>? = null

    fun build(): MapPolygon {

        return MapPolygon(
            index!!, mapId, regionId,
            lineDash!!, strokeColor!!, strokeWidth!!,
            fillColor!!,
            coordinates
                ?.run { listOf(Ring(this)) }
                ?.run { listOf(Polygon(this)) }
                ?.run { MultiPolygon(this) }
                ?.run(TypedGeometry.Companion::create)

        )
    }
}

@LiveMapDsl
class LineBuilder {
    var index: Int? = null
    var mapId: String? = null
    var regionId: String? = null

    var lon: Double? = null
    var lat: Double? = null
    var lineDash: List<Double>? = null
    var strokeColor: Color? = null
    var strokeWidth: Double? = null


    fun build(): MapLine {

        return MapLine(
            index!!, mapId, regionId,
            explicitVec(lon!!, lat!!), lineDash!!, strokeColor!!,
            strokeWidth!!
        )
    }
}

@LiveMapDsl
class TextBuilder {
    var index: Int = 0
    var mapId: String = ""
    var regionId: String = ""

    var lon: Double = 0.0
    var lat: Double = 0.0

    var fillColor: Color = Color.BLACK
    var strokeColor: Color = Color.TRANSPARENT
    var strokeWidth: Double = 0.0

    var label: String = ""
    var size: Double = 10.0
    var family: String = "Arial"
    var fontface: String = ""
    var hjust: Double = 0.0
    var vjust: Double = 0.0
    var angle: Double = 0.0

    fun build(): MapText {
        return MapText(
            index, mapId, regionId,
            explicitVec(lon, lat),
            fillColor, strokeColor, strokeWidth,
            label, size, family, fontface, hjust, vjust, angle
        )
    }
}

@LiveMapDsl
class BarsFactory {
    private val myItems = ArrayList<ChartSource>()

    fun add(source: ChartSource) {
        myItems.add(source)
    }

    fun produce(): List<MapBar> {
        val maxAbsValue = myItems
            .asSequence()
            .mapNotNull { it.values }
            .flatten()
            .maxBy { abs(it) }
            ?: error("")

        return myItems.flatMap { splitMapBarChart(it, abs(maxAbsValue)) }
    }
}

@LiveMapDsl
class PiesFactory {
    private val myItems = ArrayList<ChartSource>()

    fun add(source: ChartSource) {
        myItems.add(source)
    }

    fun produce(): List<MapPieSector> {
        return myItems.flatMap { splitMapPieChart(it) }
    }
}

@LiveMapDsl
class ChartSource {
    var lon: Double = 0.0
    var lat: Double = 0.0
    var radius: Double = 0.0

    var strokeColor: Color = Color.BLACK
    var strokeWidth: Double = 0.0

    var indices: List<Int> = emptyList()
    var values: List<Double> = emptyList()
    var colors: List<Color> = emptyList()
}

@LiveMapDsl
class Location {
    var name: String? = null
        set(v) {
            field = v; mapLocation = v?.let { MapLocation.create(MapRegion.withName(it)) }
        }
    var osmId: String? = null
        set(v) {
            field = v; mapLocation = v?.let { MapLocation.create(MapRegion.withId(it)) }
        }

    var coordinate: Vec<LonLat>? = null
        set(v) {
            field = v; mapLocation = v?.let { MapLocation.create(GeoRectangle(it.x, it.y, it.x, it.y)) }
        }

    internal var mapLocation: MapLocation? = null
    internal var hint: GeocodingHint? = null
}

@LiveMapDsl
class GeocodingHint {
    var level: FeatureLevel? = null
    var parent: MapRegion? = null
}


@LiveMapDsl
class Projection {
    var kind = ProjectionType.MERCATOR
    var loopX = true
    var loopY = false
}

@LiveMapDsl
class LiveMapTileServiceBuilder {
    var host = "localhost"
    var port: Int? = null
    var theme = TileService.Theme.COLOR

    fun build(): TileService {
        return TileService(TileWebSocketBuilder(host, port), theme)
    }
}

@LiveMapDsl
class LiveMapGeocodingServiceBuilder {
    private val subUrl = "/map_data/geocoding"

    var host = "localhost"
    var port: Int? = null

    fun build(): GeocodingService {
        return GeocodingService(GeoTransportImpl(host, port, subUrl))
    }
}

fun liveMapConfig(block: LiveMapBuilder.() -> Unit) = LiveMapBuilder().apply(block)

fun LiveMapBuilder.layers(block: LayersBuilder.() -> Unit) {
    layers.addAll(LayersBuilder().apply(block).items)
}

fun LayersBuilder.points(block: Points.() -> Unit) {
    items.add(MapLayer(POINT, Points().apply(block).items))
}

fun LayersBuilder.paths(block: Paths.() -> Unit) {
    items.add(MapLayer(PATH, Paths().apply(block).items))
}

fun LayersBuilder.polygons(block: Polygons.() -> Unit) {
    items.add(MapLayer(POLYGON, Polygons().apply(block).items))
}

fun LayersBuilder.hLines(block: Lines.() -> Unit) {
    items.add(MapLayer(H_LINE, Lines().apply(block).items))
}

fun LayersBuilder.vLines(block: Lines.() -> Unit) {
    items.add(MapLayer(V_LINE, Lines().apply(block).items))
}

fun LayersBuilder.bars(block: Bars.() -> Unit) {
    items.add(MapLayer(BAR, Bars().apply(block).factory.produce()))
}

fun LayersBuilder.pies(block: Pies.() -> Unit) {
    items.add(MapLayer(PIE, Pies().apply(block).factory.produce()))
}

fun LayersBuilder.texts(block: Texts.() -> Unit) {
    items.add(MapLayer(TEXT, Texts().apply(block).items))
}

fun point(block: PointBuilder.() -> Unit) = PointBuilder().apply(block)

fun path(block: PathBuilder.() -> Unit) = PathBuilder().apply(block)

fun LiveMapBuilder.location(block: Location.() -> Unit) {
    Location().apply(block).let { location ->
        level = location.hint?.level
        parent = location.hint?.parent
        mapLocation = location.mapLocation
    }
}

fun Location.geocodingHint(block: GeocodingHint.() -> Unit) {
    GeocodingHint().apply(block).let {
        hint = it
    }
}

fun LiveMapBuilder.projection(block: Projection.() -> Unit) {
    Projection().apply(block).let {
        projectionType = it.kind
        isLoopX = it.loopX
        isLoopY = it.loopY
    }
}

fun internalTiles(block: LiveMapTileServiceBuilder.() -> Unit): TileService {
    return LiveMapTileServiceBuilder()
        .apply {
            theme = TileService.Theme.COLOR
            host = "10.0.0.127"
            port = 3933
        }
        .apply(block).build()
}

fun liveMapTiles(block: LiveMapTileServiceBuilder.() -> Unit) = LiveMapTileServiceBuilder().apply(block).build()

fun liveMapGeocoding(block: LiveMapGeocodingServiceBuilder.() -> Unit): GeocodingService {
    return LiveMapGeocodingServiceBuilder().apply(block).build()
}

val dummyGeocodingService: GeocodingService = GeocodingService(
    object : GeoTransport {
        override fun send(request: GeoRequest): Async<GeoResponse> {
            TODO("not implemented")
        }
    }
)

val dummyTileService: TileService = object : TileService(DummySocketBuilder(), Theme.COLOR) {
    override fun getTileData(bbox: Rect<LonLat>, zoom: Int): Async<List<TileLayer>> {
        return constant(emptyList<TileLayer>())
    }
}


internal class DummySocketBuilder : SocketBuilder {
    override fun build(handler: SocketHandler): Socket {
        return object : Socket {
            override fun connect() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun close() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun send(msg: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
    }
}

