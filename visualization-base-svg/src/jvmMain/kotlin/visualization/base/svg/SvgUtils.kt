package jetbrains.datalore.visualization.base.svg

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

@Throws(IOException::class)
fun SvgUtils.buildDataUrl(bufferedImage: BufferedImage): String {
    var bytes: ByteArray? = null
    ByteArrayOutputStream().use { baos ->
        ImageIO.write(bufferedImage, "png", baos)
        bytes = baos.toByteArray()
    }
    val base64String = Base64.getEncoder().encodeToString(bytes)
    return pngDataURI(base64String)
}

private fun SvgUtils.pngDataURI(base64EncodedPngImage: String): String {
    return StringBuilder("data:image/png;base64,")
            .append(base64EncodedPngImage)
            .toString()
}