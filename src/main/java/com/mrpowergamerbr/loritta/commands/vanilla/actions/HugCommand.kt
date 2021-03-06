package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class HugCommand : ActionCommand("hug", listOf("abraço", "abraçar", "abraco", "abracar")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.actions.hug.description }
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return locale.format(first.asMention, second.asMention) { commands.actions.hug.response }
	}

	override fun getFolderName(): String {
		return "hug"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDC99"
	}
}