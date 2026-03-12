package com.example.camerax

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

class FaceBoxOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var faceList: List<Face> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    fun setFaces(faces: List<Face>, width: Int, height: Int) {
        faceList = faces
        imageWidth = width
        imageHeight = height
        invalidate()
    }

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