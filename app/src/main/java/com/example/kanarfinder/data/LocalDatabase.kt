package com.example.kanarfinder.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kanarfinder.Stop
import com.example.kanarfinder.domain.TramStop

class LocalDatabase(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "kanarfinder.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            return instance ?: synchronized(this) {
                instance ?: LocalDatabase(context).also { instance = it }
            }
        }
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
              create table if not exists user_starred_stops (
                  id integer primary key autoincrement,
                  tram_stop_fk references tram_stops(id) not null
              );
           """.trimIndent()
        )
        db.execSQL(
            """
            create table if not exists tram_stops (
                id integer primary key autoincrement,
                line_number text not null,
                stop_name text not null 
            )
        """.trimIndent()
        )
    }

    fun seedData() {
        writableDatabase.execSQL(
            """
            insert into tram_stops (line_number, stop_name)
            values ('5', 'Poznań Główny'),
                    ('5', "Most Rocha"),
                    ("5", "Plac Bernardyński"),
                    ("6", "Rondo Rataje"),
                    ("18", "Szwedzka"),
                    ("13", "Stadion Miejski")
                    ;
        """.trimIndent()
        )
    }

    fun isStarred(tramStop: TramStop): Boolean {
        val cursor = readableDatabase.rawQuery(
            """
                select count(*) from user_starred_stops where tram_stop_fk = '${tramStop.id}'
            """.trimIndent(), null
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    fun insertStarredStop(tramStop: TramStop) {
        writableDatabase.execSQL(
            """
                insert into user_starred_stops (tram_stop_fk) values ('${tramStop.id}')
            """.trimIndent()
        )
    }

    fun deleteStarredStop(tramStop: TramStop) {
        writableDatabase.execSQL(
            """
                delete from user_starred_stops where tram_stop_fk = '${tramStop.id}'
            """.trimIndent()
        )
    }

    fun getTramStops(): List<TramStop> {
        val cursor = readableDatabase.rawQuery(
            """
                select ts.id, line_number, stop_name
                from tram_stops ts
                left join user_starred_stops uss on ts.id = uss.tram_stop_fk
                order by case when uss.id is not null then 0 else 1 end, ts.line_number;
            """.trimIndent(), null
        )
        val stops = mutableListOf<TramStop>()
        while (cursor.moveToNext()) {
            stops.add(
                TramStop(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2)
                )
            )
        }
        cursor.close()
        return stops
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // no-op
    }
}