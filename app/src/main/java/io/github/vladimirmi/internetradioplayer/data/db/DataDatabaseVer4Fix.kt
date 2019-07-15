package io.github.vladimirmi.internetradioplayer.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.github.vladimirmi.internetradioplayer.di.Scopes

/**
 * Helper for fix, when in version 2.3.0-2.3.1 stations.db was renamed to data.db by mistake
 */
class DataDatabaseVer4Fix {

    private val helper by lazy {
        object : SQLiteOpenHelper(Scopes.context, "data.db", null, 4) {
            override fun onCreate(db: SQLiteDatabase?) {
            }

            override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            }
        }
    }
    private val context: Context = Scopes.context
    private lateinit var db: SQLiteDatabase

    fun open() = try {
        val databaseList = context.databaseList()
        if (databaseList.contains("data.db")) {
            db = helper.readableDatabase
            true
        } else false
    } catch (e: Exception) {
        false
    }

    fun close() {
        helper.close()
    }

    fun getStations(): List<ContentValues> {
        val c = db.rawQuery("SELECT * FROM `station`", null)
        if (!c.moveToFirst()) return emptyList()
        val values = arrayListOf<ContentValues>()
        while (!c.isAfterLast) {
            values.add(c.toStationValue())
            c.moveToNext()
        }
        c.close()
        return values
    }

    fun getGroups(): List<ContentValues> {
        val c = db.rawQuery("SELECT * FROM `group`", null)
        if (!c.moveToFirst()) return emptyList()
        val values = arrayListOf<ContentValues>()
        while (!c.isAfterLast) {
            values.add(c.toGroupValue())
            c.moveToNext()
        }
        c.close()
        return values
    }

    private fun Cursor.toStationValue() = ContentValues().also {
        it.put("id", getString(0))
        it.put("name", getString(1))
        it.put("uri", getString(2))
        it.put("encoding", getString(4))
        it.put("bitrate", getString(5))
        it.put("sample", getString(6))
        it.put(DatabaseUtils.sqlEscapeString("order"), getInt(7))
        it.put("group_id", getString(8))
        it.put("equalizerPreset", getString(9))
        it.put("description", getString(10))
        it.put("genre", getString(11))
        it.put("language", getString(12))
        it.put("location", getString(13))
        it.put("url", getString(14))
    }

    private fun Cursor.toGroupValue() = ContentValues().also {
        it.put("id", getString(0))
        it.put("name", getString(1))
        it.put("expanded", getInt(2) != 0)
        it.put(DatabaseUtils.sqlEscapeString("order"), getInt(3))
        it.put("equalizerPreset", getString(4))
    }
}