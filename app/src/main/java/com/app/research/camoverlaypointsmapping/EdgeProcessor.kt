package com.app.research.camoverlaypointsmapping

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.unit.IntSize
import timber.log.Timber
import kotlin.math.min

class EdgeProcessor(
    val displayMatrix: IntSize,
    val graphic: (Graphic) -> Unit,
    val imageMatrix: (imageProxy: ImageProxy) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        //Fill Center Landscape Height 480, Width 640 //Portrait Height 480, Width 640
        //Fit Center Landscape Height 480, Width 640 //Portrait Height 480, Width 640

        val viewHeight = displayMatrix.height
        val viewWidth = displayMatrix.width

        val scaleFactor = min((viewWidth / mediaImage.width), (viewHeight / mediaImage.height))
        Timber.d("Scale Factor : $scaleFactor")

        val rectGraphic = RectGraphic(
            left = mediaImage.width*.09f,
            top = mediaImage.height*.09f,
            right = mediaImage.width.toFloat(),
            bottom = mediaImage.height.toFloat(),
            strokeWidth = 10f,
            color = androidx.compose.ui.graphics.Color.Red
        )

        graphic(rectGraphic)

        try {
            imageMatrix(imageProxy)
        } catch (t: Throwable) {
            Timber.e(t, "Error in imageMatrix callback")
        } finally {
            imageProxy.close()
        }

    }
}