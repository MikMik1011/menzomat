package st.misa.menzomat.core.domain.desfire

import kotlinx.serialization.json.Json

private val exportJson = Json { prettyPrint = true }

internal fun CardDump.toJson(): String = exportJson.encodeToString(CardDump.serializer(), this)
