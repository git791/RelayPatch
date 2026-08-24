package dev.relaypatch.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.data.model.PatchStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PatchDao {

    @Query("SELECT * FROM patches ORDER BY createdAt DESC")
    fun getAllPatches(): Flow<List<Patch>>

    @Query("SELECT * FROM patches WHERE id = :id LIMIT 1")
    suspend fun getPatchById(id: String): Patch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatch(patch: Patch)

    @Update
    suspend fun updatePatch(patch: Patch)

    @Query("SELECT * FROM patches WHERE status = 'READY' ORDER BY createdAt ASC")
    fun getReadyPatches(): Flow<List<Patch>>

    @Query("UPDATE patches SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: PatchStatus)
}
