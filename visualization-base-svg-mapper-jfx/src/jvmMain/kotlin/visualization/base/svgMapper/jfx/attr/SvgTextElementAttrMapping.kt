package jetbrains.datalore.visualization.base.svgMapper.jfx.attr

import javafx.geometry.VPos
import javafx.scene.text.Text
import jetbrains.datalore.visualization.base.svg.SvgConstants
import jetbrains.datalore.visualization.base.svg.SvgConstants.SVG_TEXT_DY_CENTER
import jetbrains.datalore.visualization.base.svg.SvgConstants.SVG_TEXT_DY_TOP
import jetbrains.datalore.visualization.base.svg.SvgTextContent
import jetbrains.datalore.visualization.base.svg.SvgTextElement

internal object SvgTextElementAttrMapping : SvgShapeMapping<Text>() {
    override fun setAttribute(target: Text, name: String, value: Any?) {
        when (name) {
            SvgTextElement.X.name -> target.x = asDouble(value)
            SvgTextElement.Y.name -> target.y = asDouble(value)
            SvgTextContent.TEXT_ANCHOR.name -> {
                val svgTextAnchor = value as String?
                revalidatePositionAttributes(svgTextAnchor, target)
            }
            SvgTextContent.TEXT_DY.name -> {
                when (value) {
                    SVG_TEXT_DY_TOP -> target.textOrigin = VPos.TOP
                    SVG_TEXT_DY_CENTER -> target.textOrigin = VPos.CENTER
                    else -> throw IllegalStateException("Unexpected text 'dy' value: $value")
                }
            }

            SvgTextContent.FILL.name,
            SvgTextContent.FILL_OPACITY.name,
            SvgTextContent.STROKE.name,
            SvgTextContent.STROKE_OPACITY.name,
            SvgTextContent.STROKE_WIDTH.name -> super.setAttribute(target, name, value)

            else -> super.setAttribute(target, name, value)
        }
    }

    fun revalidatePositionAttributes(svgTextAnchor: String?, target: Text) {
        val width = target.boundsInLocal.width
        SvgConstants.SVG_TEXT_ANCHOR_END
        when (svgTextAnchor) {
            SvgConstants.SVG_TEXT_ANCHOR_END -> {
                target.translateX = -width
            }
            SvgConstants.SVG_TEXT_ANCHOR_MIDDLE -> {
                target.translateX = -width / 2
            }
            else -> {
                target.translateX = 0.0
            }
        }
    }
}