package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.google.common.collect.ImmutableMap
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.util.*

class JoojCommand : CommandBase("jooj") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("JOOJ_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return ImmutableMap.builder<String, String>()
				.put("imagem", "imagem")
				.build()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.IMAGES
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		val image = LorittaUtils.getImageFromContext(context, 0)

		if (!LorittaUtils.isValidImage(context, image)) { return }

		val leftSide = image.getSubimage(0, 0, image.width / 2, image.height)

		// Girar a imagem horizontalmente
		val tx = AffineTransform.getScaleInstance(-1.0, 1.0);
		tx.translate(-leftSide.getWidth(null).toDouble(), 0.0);
		val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		val leftSideFlipped = op.filter(leftSide, null);

		image.graphics.drawImage(leftSideFlipped, image.width / 2, 0, null)

		context.sendFile(image, "jooj.png", context.getAsMention(true))
	}
}