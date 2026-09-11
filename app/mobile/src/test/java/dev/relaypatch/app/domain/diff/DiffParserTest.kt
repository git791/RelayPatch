package dev.relaypatch.app.domain.diff

import dev.relaypatch.app.domain.diff.tokenizers.KotlinTokenizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffParserTest {

    @Test
    fun `isValidUnifiedDiff returns true for valid diff`() {
        val validDiff = """
            --- a/app/src/main/java/dev/relaypatch/app/UserRepository.kt
            +++ b/app/src/main/java/dev/relaypatch/app/UserRepository.kt
            @@ -42,3 +42,4 @@
                 fun getUser() {
            -        return null
            +        return User()
                 }
        """.trimIndent()
        
        assertTrue(DiffParser.isValidUnifiedDiff(validDiff))
    }

    @Test
    fun `isValidUnifiedDiff returns false for invalid diff`() {
        val invalidDiff = "Just some text, not a diff"
        assertFalse(DiffParser.isValidUnifiedDiff(invalidDiff))
    }

    @Test
    fun `parse correctly categorizes unified diff lines`() {
        val diff = """
            --- a/Test.kt
            +++ b/Test.kt
            @@ -10,3 +10,4 @@
             fun example() {
            -    println("old")
            +    println("new")
             }
        """.trimIndent()

        val parsed = DiffParser.parse(diff)

        // 2 file headers + 1 hunk header + 3 context/added/removed lines = 6 lines
        assertEquals(6, parsed.size)
        
        assertEquals(DiffParser.LineType.HEADER, parsed[0].lineType)
        assertEquals(DiffParser.LineType.HEADER, parsed[1].lineType)
        assertEquals(DiffParser.LineType.HUNK_HEADER, parsed[2].lineType)
        
        assertEquals(DiffParser.LineType.CONTEXT, parsed[3].lineType)
        assertEquals(" fun example() {", parsed[3].content)
        
        assertEquals(DiffParser.LineType.DELETION, parsed[4].lineType)
        assertEquals("-    println(\"old\")", parsed[4].content)
        
        assertEquals(DiffParser.LineType.ADDITION, parsed[5].lineType)
        assertEquals("+    println(\"new\")", parsed[5].content)
    }

    @Test
    fun `KotlinTokenizer categorizes keywords and strings correctly`() {
        val line = "fun greet(name: String) = \"Hello\""
        val tokens = KotlinTokenizer.tokenize(line)

        // Basic verification that we broke out keywords and strings
        val types = tokens.map { it.type }
        assertTrue(types.contains(TokenType.KEYWORD)) // 'fun'
        assertTrue(types.contains(TokenType.STRING))  // '"Hello"'
    }
}
