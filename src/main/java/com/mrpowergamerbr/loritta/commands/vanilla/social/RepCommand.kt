package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class RepCommand : AbstractCommand("rep", listOf("reputation", "reputação", "reputacao"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["REP_DESCRIPTON"];
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta", "@MrPowerGamerBR")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		// TODO: Fix
		/* var profile = context.lorittaUser.profile
		val user = context.getUserAt(0)

		if (user != null) {
			if (user == context.userHandle) {
				context.reply(
						LoriReply(
								message = locale["REP_SELF"],
								prefix = Constants.ERROR
						)
				)
				return
			}

			var diff = System.currentTimeMillis() - profile.lastReputationGiven

			if (3.6e+6 > diff) {
				var fancy = DateUtils.formatDateDiff(profile.lastReputationGiven + 3.6e+6.toLong(), locale)
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["REP_WAIT", fancy])
				return
			}

			var givenProfile = LorittaLauncher.loritta.getLorittaProfileForUser(user.id);

			// Agora nós iremos dar reputação para este usuário
			givenProfile.receivedReputations.add(context.userHandle.id)

			// E vamos salvar a última vez que o usuário deu reputação para o usuário
			profile.lastReputationGiven = System.currentTimeMillis()

			context.reply(
					LoriReply(
							message = context.locale["REP_SUCCESS", user.asMention],
							prefix = "☝"
					)
			)

			// E vamos salvar as configurações
			loritta save givenProfile
			loritta save profile
		} else {
			if (context.args.isEmpty()) {
				this.explain(context)
			} else {
				context.reply(
						LoriReply(
								message = locale["REP_InvalidUser"],
								prefix = Constants.ERROR
						)
				)
			}
		} */
	}
}