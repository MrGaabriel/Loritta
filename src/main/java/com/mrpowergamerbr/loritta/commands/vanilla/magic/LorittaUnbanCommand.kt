package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaUnbanCommand : AbstractCommand("lorittaunban", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Desbanir usuários de usar a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size >= 1) {
			var monster = context.args[0].toLowerCase() // ID
			val profile = LorittaLauncher.loritta.getLorittaProfile(monster)

			if (profile == null) {
				context.reply(
						LoriReply(
								"Usuário não possui perfil na Loritta!",
								Constants.ERROR
						)
				)
				return
			}

			transaction(Databases.loritta) {
				profile.isBanned = false
				profile.bannedReason = null
			}

			context.sendMessage(context.getAsMention(true) + "Usuário desbanido com sucesso!")
		} else {
			this.explain(context);
		}
	}
}