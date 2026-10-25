package com.example.discountcards.widget

import android.content.Context
import android.content.SharedPreferences

object WidgetPrefs {
    private const val PREFS_NAME = "discount_cards_widget_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveWidgetConfig(
        context: Context,
        appWidgetId: Int,
        cardId: Long,
        showCode: Boolean,
        showStoreName: Boolean
    ) {
        getPrefs(context).edit().apply {
            putLong("card_id_$appWidgetId", cardId)
            putBoolean("show_code_$appWidgetId", showCode)
            putBoolean("show_name_$appWidgetId", showStoreName)
            remove("is_minimal_bg_$appWidgetId")
            remove("is_transparent_$appWidgetId")
            apply()
        }
    }

    fun loadWidgetConfig(context: Context, appWidgetId: Int): WidgetConfig {
        val prefs = getPrefs(context)
        return WidgetConfig(
            cardId = prefs.getLong("card_id_$appWidgetId", -1L),
            showCode = prefs.getBoolean("show_code_$appWidgetId", true),
            showStoreName = prefs.getBoolean("show_name_$appWidgetId", true)
        )
    }

    fun deleteWidgetConfig(context: Context, appWidgetId: Int) {
        getPrefs(context).edit().apply {
            remove("card_id_$appWidgetId")
            remove("show_code_$appWidgetId")
            remove("show_name_$appWidgetId")
            remove("is_minimal_bg_$appWidgetId")
            remove("is_transparent_$appWidgetId")
            apply()
        }
    }
}

data class WidgetConfig(
    val cardId: Long,
    val showCode: Boolean,
    val showStoreName: Boolean
)
