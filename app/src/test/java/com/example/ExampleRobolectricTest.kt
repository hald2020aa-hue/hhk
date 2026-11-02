package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RepoExtractorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches TachiRepo`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TachiRepo", appName)
    }

    @Test
    fun `parseIndexMinJson extracts extensions and filters nsfw entries`() {
        val service = RepoExtractorService()
        val sampleJson = """
            [
              {
                "name": "Tachiyomi: MangaLek",
                "pkg": "eu.kanade.tachiyomi.extension.ar.mangalek",
                "apk": "tachiyomi-ar.mangalek-v1.4.28.apk",
                "lang": "ar",
                "code": 28,
                "version": "1.4.28",
                "nsfw": 0,
                "sources": [
                  {
                    "name": "MangaLek",
                    "lang": "ar",
                    "id": "12345",
                    "baseUrl": "https://lekmanga.net"
                  }
                ]
              },
              {
                "name": "Tachiyomi: RestrictedSource",
                "pkg": "eu.kanade.tachiyomi.extension.en.restricted",
                "apk": "tachiyomi-en.restricted-v1.4.1.apk",
                "lang": "en",
                "code": 1,
                "version": "1.4.1",
                "nsfw": 1,
                "sources": []
              }
            ]
        """.trimIndent()

        val parsed = service.parseIndexMinJson(
            jsonStr = sampleJson,
            owner = "Dranixx",
            repo = "tachiyomi-extensions-apk",
            branch = "repo"
        )
        assertEquals(1, parsed.size)
        assertEquals("MangaLek", parsed.first().cleanDisplayName)
        assertEquals("ar", parsed.first().lang)
        assertTrue(parsed.first().apkWebUrl.contains("Dranixx/tachiyomi-extensions-apk"))
    }
}
