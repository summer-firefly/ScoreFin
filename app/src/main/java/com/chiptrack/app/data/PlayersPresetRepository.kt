package com.chiptrack.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class NamedPlayersPreset(
    val id: String,
    val name: String,
    val defaultBuyIn: Int? = null,
    val players: List<String> = emptyList()
)

@Serializable
private data class PlayersPresetFile(
    val presets: List<NamedPlayersPreset> = emptyList()
)

/**
 * 应用内可命名预制名单，持久化到 filesDir/players_presets.json。
 * 首次若无数据，会尝试从旧版 assets/players.preset.json 迁移一条「默认名单」。
 */
class PlayersPresetRepository(context: Context) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    fun list(): List<NamedPlayersPreset> {
        migrateAssetPresetIfNeeded()
        return read().presets.sortedBy { it.name.lowercase() }
    }

    fun save(
        name: String,
        players: List<String>,
        defaultBuyIn: Int?,
        replaceId: String? = null
    ): NamedPlayersPreset {
        val cleanName = name.trim()
        require(cleanName.isNotEmpty()) { "empty name" }
        val cleanPlayers = players.map { it.trim() }.filter { it.isNotEmpty() }
        require(cleanPlayers.isNotEmpty()) { "empty players" }

        val current = read().presets.toMutableList()
        val existingById = replaceId?.let { id -> current.indexOfFirst { it.id == id } } ?: -1
        val existingByName = current.indexOfFirst { it.name.equals(cleanName, ignoreCase = true) }
        val targetIndex = when {
            existingById >= 0 -> existingById
            existingByName >= 0 -> existingByName
            else -> -1
        }
        val preset = NamedPlayersPreset(
            id = if (targetIndex >= 0) current[targetIndex].id else UUID.randomUUID().toString(),
            name = cleanName,
            defaultBuyIn = defaultBuyIn?.takeIf { it > 0 },
            players = cleanPlayers
        )
        if (targetIndex >= 0) current[targetIndex] = preset else current += preset
        write(PlayersPresetFile(current))
        return preset
    }

    fun delete(id: String): Boolean {
        val current = read().presets
        val next = current.filterNot { it.id == id }
        if (next.size == current.size) return false
        write(PlayersPresetFile(next))
        return true
    }

    private fun migrateAssetPresetIfNeeded() {
        if (file.exists()) return
        val asset = PlayersPresetLoader.load(appContext) ?: return
        val preset = NamedPlayersPreset(
            id = UUID.randomUUID().toString(),
            name = DEFAULT_MIGRATED_NAME,
            defaultBuyIn = asset.defaultBuyIn,
            players = asset.players
        )
        write(PlayersPresetFile(listOf(preset)))
    }

    private fun read(): PlayersPresetFile {
        if (!file.exists()) return PlayersPresetFile()
        return runCatching {
            json.decodeFromString<PlayersPresetFile>(file.readText())
        }.getOrElse { PlayersPresetFile() }
    }

    private fun write(data: PlayersPresetFile) {
        file.writeText(json.encodeToString(data))
    }

    companion object {
        private const val FILE_NAME = "players_presets.json"
        private const val DEFAULT_MIGRATED_NAME = "默认名单"
    }
}
