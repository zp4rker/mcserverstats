package com.zp4rker.mcserverstats

import com.zp4rker.discore.API
import com.zp4rker.discore.LOGGER
import com.zp4rker.discore.bot
import com.zp4rker.discore.extenstions.event.expect
import com.zp4rker.discore.extenstions.event.on
import com.zp4rker.discore.storage.BotConfig
import com.zp4rker.discore.util.loadYamlOrDefault
import com.zp4rker.mcserverstats.cmd.ShowStat
import com.zp4rker.mcserverstats.data.ServerStat
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.ReadyEvent
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

        commands = listOf(ShowStat)

        quit = { db.commit(); db.close() }
    }

    API.expect<ReadyEvent> {
        fixedRateTimer(period = TimeUnit.SECONDS.toMillis(30)) {
            LOGGER.debug("${channels.size} - ${channels.keys.joinToString()}")
            channels.forEach { (id, stat) ->
                async.submit {
                    val channel = API.getGuildChannelById(id) ?: run {
                        channels.remove(id)
                        return@submit
                    }

                    stat.getStat().let { stat ->
                        if (channel is TextChannel) {
                            if (channel.topic != stat) channel.manager.setTopic(stat).queue()
                        } else if (channel is VoiceChannel) {
                            if (channel.name != stat) channel.manager.setName(stat.let { if (it.length > 32) "${it.take(29)}..." else it }).queue()
                        }
                    }
                }
            }
        }
    }

    fixedRateTimer(period = TimeUnit.MINUTES.toMillis(5)) { db.commit() }
}