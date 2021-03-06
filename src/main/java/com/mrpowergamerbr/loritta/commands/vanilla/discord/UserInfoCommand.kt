package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.UsernameChange
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.UsernameChanges
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class UserInfoCommand : AbstractCommand("userinfo", listOf("memberinfo"), CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("USERINFO_DESCRIPTION")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var user = context.getUserAt(0)

		if (user == null) {
			if (context.args.getOrNull(0) != null) {
				context.reply(
						LoriReply(
								locale["USERINFO_UnknownUser", context.args[0].stripCodeMarks()],
								Constants.ERROR
						)
				)
				return
			}
			user = context.userHandle
		}

		val member = if (context.guild.isMember(user)) {
			context.guild.getMember(user)
		} else {
			null
		}

		val embed = EmbedBuilder()

		embed.apply {
			setThumbnail(user.effectiveAvatarUrl)
			var nickname = user.name

			if (member != null) {
				nickname = member.effectiveName
			}

			setTitle("<:discord:314003252830011395> $nickname", null)
			setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)

			val lorittaProfile = loritta.getOrCreateLorittaProfile(user.id)
			val settings = transaction(Databases.loritta) { lorittaProfile.settings }

			if (settings.hidePreviousUsernames) {
				val alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n*${locale["USERINFO_PrivacyOn"]}*"
				setDescription(alsoKnownAs)
			} else {
				val usernameChanges = transaction(Databases.loritta) { UsernameChange.find { UsernameChanges.userId eq user.idLong }.sortedBy { it.changedAt }.toMutableList() }
				/* if (usernameChanges.isEmpty()) {
					usernameChanges.add(MongoLorittaProfile.UsernameChange(user.creationTime.toEpochSecond() * 1000, user.name, user.discriminator))
				} */

				if (usernameChanges.isNotEmpty() && usernameChanges[0].discriminator == user.discriminator && usernameChanges[0].username == user.name) {
					usernameChanges.removeAt(0)
				}

				if (usernameChanges.isNotEmpty()) {
					val alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n" + usernameChanges.joinToString(separator = "\n", transform = {
						"${it.username}#${it.discriminator} (" + Instant.ofEpochMilli(it.changedAt).atZone(ZoneId.systemDefault()).toOffsetDateTime().humanize(locale) + ")"
					})
					// Verificar tamanho do "alsoKnownAs" e, se necessário, cortar
					val alsoKnownAsLines = alsoKnownAs.split("\n").reversed()

					val aux = mutableListOf<String>()

					var length = 0
					for (line in alsoKnownAsLines) {
						if (length + line.length >= 2000) {
							break
						}
						aux.add(line)
						length += line.length
					}
					setDescription(aux.reversed().joinToString(separator = "\n"))
				}
			}

			addField("\uD83D\uDCBB " + context.locale.get("USERINFO_TAG_DO_DISCORD"), "${user.name}#${user.discriminator}", true)
			addField("\uD83D\uDCBB " + context.locale.get("USERINFO_ID_DO_DISCORD"), user.id, true)
			addField("\uD83D\uDCC5 " + context.locale.get("USERINFO_ACCOUNT_CREATED"), user.creationTime.humanize(locale), true)
			if (member != null)
				addField("\uD83C\uDF1F " + context.locale.get("USERINFO_ACCOUNT_JOINED"), member.joinDate.humanize(locale), true)

			val sharedServers = lorittaShards.getMutualGuilds(user)

			var servers = if (settings.hideSharedServers) {
				"*${locale["USERINFO_PrivacyOn"]}*"
			} else {
				sharedServers.joinToString(separator = ", ", transform = { "${it.name}"})
			}

			if (servers.length >= 1024) {
				servers = servers.substring(0..1020) + "...";
			}

			embed.addField("\uD83C\uDF0E " + context.locale["USERINFO_SHARED_SERVERS"] + " (${sharedServers.size})", servers, true)
			if (member != null) {
				addField("\uD83D\uDCE1 " + context.locale["USERINFO_STATUS"], member.onlineStatus.name, true)

				val roles = member.roles.joinToString(separator = ", ", transform = { "${it.name}" });

				addField("\uD83D\uDCBC " + context.locale["USERINFO_ROLES"], if (roles.isNotEmpty()) roles.substringIfNeeded(0 until 1024) else context.locale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)
			}

			val offset = Instant.ofEpochMilli(lorittaProfile.lastMessageSentAt).atZone(ZoneId.systemDefault()).toOffsetDateTime();

			if (lorittaProfile.lastMessageSentAt != 0L) {
				addField("\uD83D\uDC40 " + context.locale["USERINFO_LAST_SEEN"], offset.humanize(locale), true)
			}

			embed.setFooter(locale["USERINFO_PrivacyInfo"], null)
		}

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}