package st.misa.menzomat.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** Every string must exist in both languages with the same placeholders, so none shows up untranslated. */
class StringResourcesParityTest {
    private val resources = File("src/commonMain/composeResources")

    private fun entries(locale: String): Map<String, Set<String>> {
        val xml = File(resources, "$locale/strings.xml").readText()
        return Regex("""<(string|plurals) name="([^"]+)"[^>]*>(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(xml)
            .associate { match ->
                val placeholders = Regex("""%\d+\$[sd]""").findAll(match.groupValues[3]).map { it.value }.toSet()
                match.groupValues[2] to placeholders
            }
    }

    @Test
    fun `serbian and english define the same strings`() {
        val serbian = entries("values")
        val english = entries("values-en")
        assertEquals(serbian.keys.sorted(), english.keys.sorted())
    }

    @Test
    fun `serbian and english use the same placeholders`() {
        val serbian = entries("values")
        val english = entries("values-en")
        serbian.forEach { (name, placeholders) -> assertEquals(placeholders, english[name], "placeholders of $name") }
    }
}
