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
        return when (type) {
            Type.Status -> "Server status: Online"
            Type.OnlinePlayers -> "Online players: 30"
            Type.MaxPlayers -> "Max players: 250"
            else -> "Server motd: A Minecraft Server"
        }
    }

}
