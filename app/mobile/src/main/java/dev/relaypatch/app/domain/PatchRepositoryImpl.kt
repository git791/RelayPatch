package dev.relaypatch.app.domain

import dev.relaypatch.app.data.db.PatchDao
import dev.relaypatch.app.data.model.CaptureBundle
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.data.model.PatchStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatchRepositoryImpl @Inject constructor(
    private val patchDao: PatchDao,
) : PatchRepository {

    override fun getAllPatches(): Flow<List<Patch>> =
        patchDao.getAllPatches()

    override suspend fun createPatch(bundle: CaptureBundle): Patch {
        val patch = Patch(
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            errorText = bundle.errorText,
            spokenIntent = bundle.spokenIntent,
            diffText = "",
            status = PatchStatus.DRAFTING,
            targetFileHint = null,
        )
        patchDao.insertPatch(patch)
        return patch
    }

    override suspend fun updateStatus(id: String, status: PatchStatus) {
        patchDao.updateStatus(id, status)
    }

    override fun getReadyPatches(): Flow<List<Patch>> =
        patchDao.getReadyPatches()

    override suspend fun getPatchById(id: String): Patch? =
        patchDao.getPatchById(id)
}
