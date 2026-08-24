package dev.relaypatch.app.domain.inference

import dev.relaypatch.app.data.model.CaptureBundle
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deterministic offline [LocalLLM] implementation for unit tests and
 * demo fallback when the on-device model file is unavailable.
 *
 * Always returns a realistic unified diff that adds a null-check guard before
 * a user-lookup call — the golden-path scenario from TECHNICAL_DOC.md §6.
 *
 * A 1 500 ms delay is added to simulate realistic NPU inference latency so
 * that UI loading states and transitions behave correctly in tests.
 */
@Singleton
class FakeLLM @Inject constructor() : LocalLLM {

    override suspend fun generateDiff(bundle: CaptureBundle): Result<String> {
        delay(SIMULATED_INFERENCE_DELAY_MS)
        return Result.success(HARDCODED_DIFF)
    }

    private companion object {
        const val SIMULATED_INFERENCE_DELAY_MS = 1_500L

        val HARDCODED_DIFF: String = """
--- a/app/src/main/java/dev/relaypatch/app/data/repository/UserRepository.kt
+++ b/app/src/main/java/dev/relaypatch/app/data/repository/UserRepository.kt
@@ -42,7 +42,11 @@ class UserRepository @Inject constructor(
     suspend fun getDisplayName(userId: String): String {
-        val user = userDao.findById(userId)
-        return user.displayName
+        val user = userDao.findById(userId)
+        if (user == null) {
+            throw IllegalStateException("User not found for id: ${'$'}userId")
+        }
+        return user.displayName
     }
 
     suspend fun updateLastSeen(userId: String) {
""".trimIndent()
    }
}
