package com.example.pixelwatchfaces

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import java.time.ZonedDateTime
import kotlin.math.min

class MyWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = AnalogRenderer(
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.HARDWARE
        )

        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = renderer
        )
    }

    class AnalogRenderer(
        surfaceHolder: SurfaceHolder,
        private val watchState: WatchState,
        currentUserStyleRepository: CurrentUserStyleRepository,
        canvasType: Int
    ) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
        surfaceHolder,
        currentUserStyleRepository,
        watchState,
        canvasType,
        clearWithBackgroundTintBeforeRenderToo = true
    ) {
        // إعدادات فرشاة رسم الخلفية
        private val backgroundPaint = Paint().apply { color = Color.BLACK }

        // إعدادات فرشاة رسم علامات الدقائق والساعات
        private val tickPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 3f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        // إعدادات فرشاة الأرقام (12, 3, 6, 9)
        private val numberPaint = Paint().apply {
            color = Color.WHITE
            textSize = 45f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true // لجعل الخط عريضاً ورياضياً
        }

        // إعدادات عقرب الساعات
        private val hourHandPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 12f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        // إعدادات عقرب الدقائق
        private val minuteHandPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 8f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        // إعدادات عقرب الثواني (باللون البرتقالي المميز)
        private val secondHandPaint = Paint().apply {
            color = Color.parseColor("#FF5722") // لون برتقالي يشبه ساعات الرياضة
            strokeWidth = 4f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        override suspend fun createSharedAssets(): SharedAssets {
            return object : SharedAssets {
                override fun onDestroy() {}
            }
        }

        override fun render(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: SharedAssets
        ) {
            val isAmbient = watchState.isAmbient.value
            
            // 1. رسم الخلفية
            canvas.drawRect(bounds, backgroundPaint)

            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()
            val radius = min(centerX, centerY)

            // 2. رسم علامات الساعات والأرقام
            for (i in 0 until 60) {
                val isHourMark = i % 5 == 0
                
                // لا نرسم علامات الساعات عند (12, 3, 6, 9) لأننا سنضع أرقاماً بدلاً منها
                val isQuarterHour = i % 15 == 0

                if (!isQuarterHour) {
                    val tickLength = if (isHourMark) 20f else 10f
                    tickPaint.strokeWidth = if (isHourMark) 6f else 3f
                    tickPaint.color = if (isAmbient) Color.DKGRAY else (if (isHourMark) Color.WHITE else Color.GRAY)
                    
                    canvas.drawLine(centerX, centerY - radius + 15f, centerX, centerY - radius + 15f + tickLength, tickPaint)
                }
                canvas.rotate(6f, centerX, centerY) // تدوير القماش بـ 6 درجات للعلامة التالية
            }

            // رسم الأرقام (12, 3, 6, 9)
            val textOffset = (numberPaint.descent() + numberPaint.ascent()) / 2
            canvas.drawText("12", centerX, centerY - radius + 55f - textOffset, numberPaint)
            canvas.drawText("6", centerX, centerY + radius - 35f - textOffset, numberPaint)
            canvas.drawText("3", centerX + radius - 45f, centerY - textOffset, numberPaint)
            canvas.drawText("9", centerX - radius + 45f, centerY - textOffset, numberPaint)

            // 3. حساب زوايا العقارب بناءً على الوقت الحالي
            val hour = zonedDateTime.hour % 12
            val minute = zonedDateTime.minute
            val second = zonedDateTime.second

            val hourRotation = (hour + minute / 60f) * 30f
            val minuteRotation = (minute + second / 60f) * 6f
            val secondRotation = second * 6f

            // 4. رسم عقرب الساعات
            canvas.save()
            canvas.rotate(hourRotation, centerX, centerY)
            // في وضع الخمول نجعله مفرغاً لتوفير البطارية، وفي الوضع العادي ممتلئ
            hourHandPaint.style = if (isAmbient) Paint.Style.STROKE else Paint.Style.FILL
            canvas.drawLine(centerX, centerY + 20f, centerX, centerY - radius * 0.5f, hourHandPaint)
            canvas.restore()

            // 5. رسم عقرب الدقائق
            canvas.save()
            canvas.rotate(minuteRotation, centerX, centerY)
            minuteHandPaint.style = if (isAmbient) Paint.Style.STROKE else Paint.Style.FILL
            canvas.drawLine(centerX, centerY + 20f, centerX, centerY - radius * 0.75f, minuteHandPaint)
            canvas.restore()

            // 6. رسم عقرب الثواني (فقط في الوضع النشط، نلغيه في وضع الخمول لتوفير البطارية)
            if (!isAmbient) {
                canvas.save()
                canvas.rotate(secondRotation, centerX, centerY)
                canvas.drawLine(centerX, centerY + 30f, centerX, centerY - radius * 0.85f, secondHandPaint)
                // دائرة صغيرة في المنتصف
                canvas.drawCircle(centerX, centerY, 8f, secondHandPaint)
                canvas.restore()
            } else {
                // دائرة بيضاء فارغة في المنتصف في وضع الخمول
                canvas.drawCircle(centerX, centerY, 8f, hourHandPaint)
            }
        }

        override fun renderHighlightLayer(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: SharedAssets
        ) {}
    }
}
