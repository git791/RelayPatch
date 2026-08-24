package dev.relaypatch.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for RelayPatch.
 *
 * Annotated with [@HiltAndroidApp] to trigger Hilt's code generation and set up
 * the application-level DI container (SingletonComponent). This must be declared
 * as `android:name=".RelayPatchApp"` in AndroidManifest.xml.
 *
 * Do not perform heavy initialization here — all singleton creation is deferred
 * to Hilt's lazy injection to keep app startup fast (TECHNICAL_DOC.md §6,
 * demo-device startup performance note).
 */
@HiltAndroidApp
class RelayPatchApp : Application()
