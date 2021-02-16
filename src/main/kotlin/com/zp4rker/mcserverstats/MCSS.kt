package com.zp4rker.mcserverstats

import com.zp4rker.discore.API
import com.zp4rker.discore.bot
import com.zp4rker.discore.storage.BotConfig
import com.zp4rker.discore.util.loadYamlOrDefault
import com.zp4rker.mcserverstats.data.ServerStat
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

/**
 * @author zp4rker
 */
val config = loadYamlOrDefault<BotConfig>(File("config.yml"))
val db = DBMaker.fileDB("serverStats.db").make()
val channels = db.hashMap("channels", Serializer.STRING, ServerStat.Serializer).createOrOpen()

private val async = Executors.newCachedThreadPool()

fun main() {
    bot {
        name = "MC Server Stats"

        token = config.token
        prefix = config.prefix

        activity = Activity.watching("lots of Minecraft servers...")

        quit = { db.close() }
    }

    fixedRateTimer(period = TimeUnit.SECONDS.toMillis(30)) {
        channels.forEach { (id, stat) ->
            async.submit {
                val channel = API.getGuildChannelById(id) ?: run {
                    channels.remove(id)
                    return@submit
                }

                stat.getStat().let {
                    if (channel is TextChannel) {
                        if (channel.topic != it) channel.manager.setTopic(it).queue()
                    } else if (channel is VoiceChannel) {
                        if (channel.name != it) channel.manager.setName(it).queue()
                    }
                }
            }
        }
    }

    fixedRateTimer(period = TimeUnit.MINUTES.toMillis(5)) { db.commit() }
}