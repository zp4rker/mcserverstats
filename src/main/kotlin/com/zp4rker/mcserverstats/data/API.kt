package com.zp4rker.mcserverstats.data

import org.json.JSONObject
import java.net.URL

/**
 * @author zp4rker
 *
 * uses https://api.mcservstat.us
 *
 */
object API {

    data class APIResult(
        val status: String,
        val onlinePlayers: Int? = null,
        val maxPlayers: Int? = null,
        val motd: String? = null)

    fun retrieve(ip: String): APIResult {
        val url = URL("https://api.mcsrvstat.us/2/$ip")

        val data = JSONObject(url.readText())

        return if (!data.getBoolean("online")) APIResult("Offline")
        else data.getJSONObject("players").let { players ->
            data.getJSONObject("motd").getJSONArray("clean").let { motd ->
                APIResult("Online", players.getInt("online"), players.getInt("max"), motd.joinToString(" "))
            }
        }
    }

}