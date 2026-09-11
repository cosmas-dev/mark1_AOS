package com.cosmasbio.mark1.data.local

import android.content.Context

/** 리더기 이름처럼 서버 동기화가 필요 없는 값을 로컬 기기에만 저장한다. */
object ReaderPreferences {
    private const val PREFS_NAME = "reader_prefs"
    private const val KEY_READER_NAME = "reader_name"
    private const val KEY_READER_REGISTERED = "reader_registered"
    const val DEFAULT_READER_NAME = "Cosma"

    fun getReaderName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_READER_NAME, DEFAULT_READER_NAME) ?: DEFAULT_READER_NAME
    }

    fun setReaderName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_READER_NAME, name)
            .apply()
    }

    /** 리더기가 이 기기에 등록돼 있는지. 삭제 전까지는 기본으로 등록된 것으로 본다. */
    fun isReaderRegistered(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_READER_REGISTERED, true)
    }

    fun setReaderRegistered(context: Context, registered: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_READER_REGISTERED, registered)
            .apply()
    }
}
