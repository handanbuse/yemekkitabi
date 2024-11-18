package roomdb

import androidx.room.Dao
import androidx.room.Database
import androidx.room.RoomDatabase
import model.Tarif

@Database(entities = [Tarif::class],version=1) // version =1 çünkü başka kolon eklemek istersek 2 gibi ayarlanır
        abstract class TarifDatabase : RoomDatabase(){
            abstract fun tarifDao(): TarifDao
        }