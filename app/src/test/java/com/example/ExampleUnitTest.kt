package com.example

import com.example.data.RepoExtractorService
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun parseOwnerAndRepo_handlesGitUrlAndShortSyntax() {
        val service = RepoExtractorService()
        val (owner1, repo1) = service.parseOwnerAndRepo(
            "https://github.com/Dranixx/tachiyomi-extensions-apk.git",
            "fallback"
        )
        assertEquals("Dranixx", owner1)
        assertEquals("tachiyomi-extensions-apk", repo1)

        val (owner2, repo2) = service.parseOwnerAndRepo(
            "keiyoushi/extensions",
            "fallback"
        )
        assertEquals("keiyoushi", owner2)
        assertEquals("extensions", repo2)
    }
}
