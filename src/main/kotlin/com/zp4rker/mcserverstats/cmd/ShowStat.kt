package com.zp4rker.mcserverstats.cmd

import com.zp4rker.discore.command.Command
import com.zp4rker.discore.extenstions.awaitMessages
import com.zp4rker.discore.extenstions.awaitReactions
import com.zp4rker.discore.extenstions.embed
import com.zp4rker.discore.util.unicodify
import com.zp4rker.mcserverstats.channels
import com.zp4rker.mcserverstats.data.ServerStat
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

/**
 * @author zp4rker
 */
object ShowStat : Command(aliases = arrayOf("showstat"), usage = "showstat <ip> <type>") {
    override fun handle(args: Array<String>, message: Message, channel: TextChannel) {
        // get ip
        val ip = if (args.isNotEmpty()) {
            args[0]
        } else {
            channel.sendMessage(embed {
                title { text = "Please enter in the server IP." }
            }).complete()

            channel.awaitMessages({ it.author == message.author }).first().contentRaw
        }

        // validate ip
        if (!ip.contains(".")) {
            channel.sendMessage(embed {
                title { text = "Invalid IP!" }
                description = "Try again with a valid IP."

                color = "#ec644b"
            }).queue()
            return
        }

        // get type
        val typeString = if (args.size > 1) {
            args.copyOfRange(1, args.size - 1).joinToString("")
        } else {
            channel.sendMessage(embed {
                title { text = "Please select one of these types of stats:" }
                description = "status, onlineplayers, maxplayers or motd"
            }).complete()

            channel.awaitMessages({ it.author == message.author }).first().contentRaw
        }

        // validate type
        val type = ServerStat.Type.values().find { it.name.equals(typeString, true) } ?: run {
            channel.sendMessage(embed {
                title { text = "Invalid Type!" }
                description = "Try again with a valid type. Valid types: status, onlineplayers, maxplayers or motd"

                color = "#ec644b"
            }).queue()
            return
        }

        // get location
        val m = channel.sendMessage(embed {
            title { text = "Where should this stat be displayed?" }
            description = """
                React with :one: for it to be displayed in the topic of this channel.
                React with :two: for it to be displayed as a channel name.
            """.trimIndent()
        }).complete()
        val r = channel.awaitReactions({
            it.retrieveUsers().complete().any { u -> u == message.author } && it.messageId == m.id &&
                    (it.reactionEmote.name == ":one:".unicodify() || it.reactionEmote.name == ":two:".unicodify())
        }).first()
        val topic = r.reactionEmote.name == ":one:".unicodify()

        // prepare location
        if (topic) {
            channels[channel.id] = ServerStat(ip, type)
        } else {
            channel.guild.createVoiceChannel("Server Stat").addRolePermissionOverride(channel.guild.publicRole.idLong, 0, 1048576).complete().let { vc ->
                channels[vc.id] = ServerStat(ip, type)
            }
        }

        // confirmation
        channel.sendMessage(embed {
            title { text = "Added Server Stat" }
            description = "Please allow upto 30 seconds for it to sync."
            if (!topic) {
                description = "$description Feel free to move the channel around and/or modify its permissions."
            }
        }).queue()
    }
}