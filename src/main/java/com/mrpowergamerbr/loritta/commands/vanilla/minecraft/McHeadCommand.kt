package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.minecraft.MCUtils

class McHeadCommand : AbstractCommand("mchead", category = CommandCategory.MINECRAFT) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("MCHEAD_DESCRIPTION")
	}

	override fun getUsage(): String {
		return "nickname"
	}

	override fun getExamples(): List<String> {
		return listOf("Monerk")
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val nickname = context.args[0]

			val uuid = MCUtils.getUniqueId(nickname)

			if (uuid == null) {
				context.reply(
						LoriReply(
								locale["MCSKIN_UnknownPlayer", context.args.getOrNull(0)],
								Constants.ERROR
						)
				)
				return
			}

			val bufferedImage = LorittaUtils.downloadImage("https://crafatar.com/renders/head/$uuid?size=128&overlay")
			context.sendFile(bufferedImage!!, "avatar.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

}