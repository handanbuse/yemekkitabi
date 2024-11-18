package roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import model.Tarif


@Dao
interface TarifDao {
    @Query("SELECT* FROM Tarif")
    fun getAll() :Flowable<List<Tarif>>

    @Query("SELECT* FROM tarif WHERE id= :id")
    fun findById(id:Int):Flowable<Tarif>

    @Insert
    fun insert(tarif: Tarif): Completable
    @Delete
    fun delete(tarif: Tarif):Completable
}
