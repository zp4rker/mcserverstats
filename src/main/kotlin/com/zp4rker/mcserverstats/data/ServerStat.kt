package com.zp4rker.mcserverstats.data

import org.mapdb.DataInput2
import org.mapdb.DataOutput2

/**
 * @author zp4rker
 */
data class ServerStat(val serverIP: String, val type: Type) {

    enum class Type {
        Status,
        OnlinePlayers,
        MaxPlayers,
        Motd,
    }

    object Serializer : org.mapdb.Serializer<ServerStat> {
        override fun serialize(out: DataOutput2, value: ServerStat) {
            out.writeUTF(value.serverIP)
            out.writeUTF(value.type.name)
        }

        override fun deserialize(input: DataInput2, available: Int): ServerStat {
            val serverIP = input.readUTF()
            val type = Type.valueOf(input.readUTF())
            return ServerStat(serverIP, type)
        }
    }

    fun getStat(): String {
        val data = API.retrieve(serverIP)

        return when (type) {
            Type.Status -> "Server status: ${data.status}"
            Type.OnlinePlayers -> "Online players: ${data.onlinePlayers ?: "Unavailable"}"
            Type.MaxPlayers -> "Max players: ${data.maxPlayers ?: "Unavailable"}"
            else -> "Server motd: ${data.motd ?: "Unavailable"}"
        }
    }

}
