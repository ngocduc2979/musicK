package database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicDatabase(
    context: Context, name: String,
    factory: SQLiteDatabase.CursorFactory?, version: Int):
    SQLiteOpenHelper(context, name, factory, version) {

    fun querryData(sql: String?) {
        val database = writableDatabase
        database.execSQL(sql)
    }

    fun getData(sql: String): Cursor {
        val database = readableDatabase
        return database.rawQuery(sql, null)
    }

    override fun onCreate(p0: SQLiteDatabase?) {

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}