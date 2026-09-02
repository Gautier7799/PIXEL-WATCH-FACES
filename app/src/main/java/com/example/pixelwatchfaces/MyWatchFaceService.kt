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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MyWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        
        val renderer = MyRenderer(
            context = this,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.HARDWARE
        )

        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }

    class MyRenderer(
        context: android.content.Context,
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        currentUserStyleRepository: CurrentUserStyleRepository,
        canvasType: Int
    ) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
        surfaceHolder,
        currentUserStyleRepository,
        watchState,
        canvasType,
        clearWithBackgroundTintBeforeRenderToo = true
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        
        // إعدادات فرشاة الرسم للوقت
        private val textPaint = Paint().apply {
            color = Color.parseColor("#4CAF50") // لون أخضر جذاب
            textSize = 70f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        // إعدادات الخلفية
        private val backgroundPaint = Paint().apply {
            color = Color.BLACK
        }

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
            // 1. رسم الخلفية السوداء (لتوفير البطارية)
            canvas.drawRect(bounds, backgroundPaint)

            // 2. حساب منتصف الشاشة
            val centerX = bounds.exactCenterX()
            val centerY = bounds.exactCenterY()

            // 3. كتابة الوقت في منتصف الشاشة
            val timeString = zonedDateTime.format(timeFormatter)
            canvas.drawText(
                timeString,
                centerX,
                centerY + (textPaint.textSize / 3), // تعديل بسيط ليتوسط النص رأسياً
                textPaint
            )
        }

        override fun renderHighlightLayer(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: SharedAssets
        ) {
            // يستخدم عند الضغط المطول لتعديل الواجهة (سنتركه فارغاً حالياً)
        }
    }
}
