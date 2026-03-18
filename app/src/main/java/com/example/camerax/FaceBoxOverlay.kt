package com.example.camerax

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

/**
 * A transparent overlay [View] that draws green bounding boxes around detected faces.
 *
 * This view is placed on top of the CameraX [androidx.camera.view.PreviewView] so that its
 * transparent background lets the live camera feed show through while the rectangles are
 * rendered on top.  Coordinates are supplied in camera-frame space and are scaled to the
 * view's own dimensions on every draw pass.
 *
 * Typical usage:
 * ```kotlin
 * val faceOverlay = findViewById<FaceBoxOverlay>(R.id.faceOverlay)
 * detector.process(image)
 *     .addOnSuccessListener { faces ->
 *         faceOverlay.setFaces(faces, image.width, image.height)
 *     }
 * ```
 *
 * @param context The [Context] used to inflate this view.
 * @param attrs   The attribute set from XML, or `null` if the view is created programmatically.
 */
class FaceBoxOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var faceList: List<Face> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    /**
     * Updates the list of detected faces and triggers a redraw.
     *
     * Call this method from the ML Kit success listener every time a new set of faces is
     * detected.  The bounding-box coordinates inside each [Face] object are expressed in
     * camera-frame pixels; [width] and [height] are the dimensions of that camera frame so
     * that [onDraw] can scale the boxes to the view's own dimensions.
     *
     * @param faces  The list of [Face] objects returned by the ML Kit face detector.
     * @param width  The pixel width of the camera frame that produced [faces].
     * @param height The pixel height of the camera frame that produced [faces].
     */
    fun setFaces(faces: List<Face>, width: Int, height: Int) {
        faceList = faces
        imageWidth = width
        imageHeight = height
        invalidate()
    }

    /**
     * Draws a green rectangle around each face in [faceList].
     *
     * Bounding-box coordinates are first scaled from camera-frame space to view space using
     * independent horizontal ([scaleX]) and vertical ([scaleY]) scale factors.  Each
     * rectangle is then expanded by 50 % around its center to give a slightly generous margin
     * around the detected face region.
     *
     * This method is called automatically by the Android framework after [setFaces] calls
     * [invalidate]; do not invoke it directly.
     *
     * @param canvas The [Canvas] onto which the boxes will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0) return

        val scaleX = width.toFloat() / imageWidth.toFloat()
        val scaleY = height.toFloat() / imageHeight.toFloat()

        for (face in faceList) {
            val bounds = face.boundingBox

            val centerX = bounds.centerX() * scaleX
            val centerY = bounds.centerY() * scaleY

            val expandedWidth = bounds.width() * scaleX * 1.5f
            val expandedHeight = bounds.height() * scaleY * 1.5f

            val left = centerX - (expandedWidth / 2)
            val top = centerY - (expandedHeight / 2)
            val right = centerX + (expandedWidth / 2)
            val bottom = centerY + (expandedHeight / 2)

            canvas.drawRect(left, top, right, bottom, boxPaint)
        }
    }
}