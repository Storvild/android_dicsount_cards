package com.example.discountcards.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.discountcards.DiscountCardsApp
import com.example.discountcards.MainActivity
import com.example.discountcards.R
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.BarcodeGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DiscountCardsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            WidgetPrefs.deleteWidgetConfig(context, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val config = WidgetPrefs.loadWidgetConfig(context, appWidgetId)
            if (config.cardId == -1L) return

            val app = context.applicationContext as? DiscountCardsApp ?: return
            val card: DiscountCard? = runBlocking(Dispatchers.IO) {
                app.database.discountCardDao().getCardByIdDirect(config.cardId)
            }

            val views = RemoteViews(context.packageName, R.layout.widget_discount_cards)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 70)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70)

            val cellsW = ((minWidth - 10) / 73).coerceAtLeast(1).coerceAtMost(5)
            val cellsH = ((minHeight - 10) / 73).coerceAtLeast(1).coerceAtMost(5)

            val showCode = config.showCode
            val showName = config.showStoreName

            val is1x1 = cellsW == 1 && cellsH == 1
            val nameTextSizeSp = if (is1x1) 10f else 14f
            views.setTextViewTextSize(
                R.id.widget_store_name,
                TypedValue.COMPLEX_UNIT_SP,
                nameTextSizeSp
            )

            if (card != null) {
                if (showName && card.storeName.isNotBlank()) {
                    views.setTextViewText(R.id.widget_store_name, card.storeName)
                    views.setViewVisibility(R.id.widget_store_name, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_store_name, android.view.View.GONE)
                }

                if (showCode && card.cardNumber.isNotBlank()) {
                    val pxWidth = (minWidth * context.resources.displayMetrics.density).toInt().coerceAtLeast(200)
                    val pxHeight = (minHeight * context.resources.displayMetrics.density).toInt().coerceAtLeast(100)

                    val fgColor = Color.BLACK
                    val bitmap = BarcodeGenerator.generateBarcodeCached(
                        content = card.cardNumber,
                        format = card.barcodeFormat,
                        width = pxWidth,
                        height = pxHeight,
                        foregroundColor = fgColor,
                        backgroundColor = Color.TRANSPARENT
                    )

                    if (bitmap != null) {
                        views.setImageViewBitmap(R.id.widget_barcode_image, bitmap)
                        views.setViewVisibility(R.id.widget_barcode_image, android.view.View.VISIBLE)
                        views.setViewVisibility(R.id.widget_card_number_text, android.view.View.GONE)
                    } else {
                        views.setTextViewText(R.id.widget_card_number_text, card.cardNumber)
                        views.setViewVisibility(R.id.widget_barcode_image, android.view.View.GONE)
                        views.setViewVisibility(R.id.widget_card_number_text, android.view.View.VISIBLE)
                    }
                } else {
                    views.setViewVisibility(R.id.widget_barcode_image, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_card_number_text, android.view.View.GONE)
                }

                if (config.isTransparent) {
                    views.setInt(R.id.widget_root_layout, "setBackgroundColor", Color.TRANSPARENT)
                    views.setInt(R.id.widget_content_container, "setBackgroundResource", R.drawable.widget_bg_rounded)
                } else {
                    views.setInt(R.id.widget_root_layout, "setBackgroundResource", R.drawable.widget_bg_rounded)
                    views.setInt(R.id.widget_content_container, "setBackgroundColor", Color.TRANSPARENT)
                }
            }

            // Click intent to open app detail (on content container)
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("card_id", config.cardId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_content_container, mainPendingIntent)

            // Click intent to open configuration/edit activity (on settings button)
            val configIntent = Intent(context, DiscountCardsWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val configPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 100000,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_settings_button, configPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
