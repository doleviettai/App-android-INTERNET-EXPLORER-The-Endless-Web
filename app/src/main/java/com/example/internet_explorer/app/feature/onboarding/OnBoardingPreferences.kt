package com.example.internet_explorer.app.feature.onboarding

import android.content.Context

private const val PREFS_NAME = "onboarding_prefs"
private const val KEY_COMPLETED = "onboarding_completed"

/**
 * Chỉ lưu 1 cờ boolean "đã xem Onboarding chưa", dùng SharedPreferences vì đây là
 * tuỳ chọn hiển thị của app trên máy, khác hẳn World State (progress điều tra) --
 * World State cố tình chưa persist ở Phase 0 (mất khi tắt app), còn cờ này thì nên nhớ.
 */
object OnboardingPreferences {
    fun hasCompletedOnboarding(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_COMPLETED, false)
    }

    fun setCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_COMPLETED, true).apply()
    }
}