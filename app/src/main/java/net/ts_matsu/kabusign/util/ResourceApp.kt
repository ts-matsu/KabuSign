package net.ts_matsu.kabusign.util

import android.app.Application
import androidx.room.Room
import net.ts_matsu.kabusign.model.data.KabuSignDatabase

class ResourceApp: Application() {

    companion object {
        lateinit var instance: Application
            private set

        lateinit var database: KabuSignDatabase
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            KabuSignDatabase::class.java,
            "kabusign.db"
        ).build()

    }
}