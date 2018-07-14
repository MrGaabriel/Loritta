package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.guild

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.views.subviews.api.config.types.*
import net.dv8tion.jda.core.entities.Guild
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.*

@Path("/api/v1/guild/:guildId/config")
class UpdateServerConfigController {
	val logger by logger()

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_REST_AUTH)
	fun getConfig(req: Request, res: Response, guildId: String, @Local userIdentification: TemmieDiscordAuth.UserIdentification, @Local serverConfig: ServerConfig, @Local guild: Guild) {
		res.type(MediaType.json)

		val serverConfigJson = Gson().toJsonTree(serverConfig)

		val textChannels = JsonArray()
		for (textChannel in guild.textChannels) {
			val json = JsonObject()

			json["id"] = textChannel.id
			json["canTalk"] = textChannel.canTalk()
			json["name"] = textChannel.name

			textChannels.add(json)
		}

		serverConfigJson["textChannels"] = textChannels

		val roles = JsonArray()
		for (role in guild.roles) {
			val json = JsonObject()

			json["id"] = role.id
			json["name"] = role.name
			json["isPublicRole"] = role.isPublicRole
			json["isManaged"] = role.isManaged
			json["canInteract"] = guild.selfMember.canInteract(role)

			if (role.color != null) {
				json["color"] = jsonObject(
						"red" to role.color.red,
						"green" to role.color.green,
						"blue" to role.color.blue
				)
			}

			roles.add(json)
		}

		val members = JsonArray()
		for (member in guild.members) {
			val json = JsonObject()

			json["id"] = member.user.id
			json["name"] = member.user.name
			json["discriminator"] = member.user.discriminator
			json["avatar"] = member.user.avatarUrl

			members.add(json)
		}

		val emotes = JsonArray()
		for (emote in guild.emotes) {
			val json = JsonObject()

			json["id"] = emote.id
			json["name"] = emote.name

			emotes.add(json)
		}

		serverConfigJson["roles"] = roles
		serverConfigJson["members"] = members
		serverConfigJson["emotes"] = emotes
		serverConfigJson["permissions"] = gson.toJsonTree(guild.selfMember.permissions.map { it.name })

		val user = lorittaShards.getUserById(userIdentification.id)

		if (user != null) {
			val selfUser = jsonObject(
					"id" to userIdentification.id,
					"name" to user.name,
					"discriminator" to user.discriminator,
					"avatar" to user.effectiveAvatarUrl
			)
			serverConfigJson["selfUser"] = selfUser
		}

		serverConfigJson["guildName"] = guild.name

		res.send(serverConfigJson)
	}

	@PATCH
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.DISCORD_GUILD_REST_AUTH)
	fun patchConfig(req: Request, res: Response, guildId: String, @Local guild: Guild, @Local serverConfig: ServerConfig, @Body rawConfig: String) {
		res.type(MediaType.json)

		val payload = jsonParser.parse(rawConfig).obj
		val type = payload["type"].string
		val config = payload["config"].obj

		val payloadHandlers = mapOf(
				"server_list" to ServerListPayload::class.java,
				"moderation" to ModerationPayload::class.java,
				"autorole" to AutorolePayload::class.java,
				"welcomer" to WelcomerPayload::class.java,
				"miscellaneous" to MiscellaneousPayload::class.java,
				"economy" to EconomyPayload::class.java
		)

		val payloadHandlerClass = payloadHandlers[type]

		if (payloadHandlerClass != null) {
			val payloadHandler = payloadHandlerClass.newInstance()
			payloadHandler.process(config, serverConfig, guild)
			loritta save serverConfig
		} else {
			res.status(Status.NOT_IMPLEMENTED)
			res.send(
					WebsiteUtils.createErrorPayload(
							LoriWebCode.MISSING_PAYLOAD_HANDLER,
							"I don't know how to handle a \"${type}\" payload yet!"
					)
			)
			return
		}
	}
}