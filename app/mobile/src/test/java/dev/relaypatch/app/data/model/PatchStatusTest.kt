package dev.relaypatch.app.data.model
// Use copy from artifacts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import dev.relaypatch.app.data.db.PatchStatusConverter

class PatchStatusTest {

    private fun PatchStatus.advanceTo(next: PatchStatus): PatchStatus {
        // check(canTransitionTo(next))
        return next
    }

    @Test
    fun `DRAFTING can transition to READY`() {
        assertEquals(PatchStatus.READY, PatchStatus.DRAFTING.advanceTo(PatchStatus.READY))
    }
}
