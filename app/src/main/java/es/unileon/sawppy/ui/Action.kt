package es.unileon.sawppy.ui

/**
 * Data class that represents the different actions that can be performed by the user.
 *
 * @param signalToSend The signal to send to the rover. Numbering is done as follows:
 * - 10x: movement commands
 * - 20x: control mode commands
 * - 30x: camera commands
 * - 40x: light commands
 * - 50x: sound commands
 * - 60x: sensor commands
 * - 70x: other commands
 */
data class Action(val signalToSend: Int) {
	companion object {
		val FORWARD = Action(101)
		val BACKWARD = Action(102)
		val LEFT = Action(103)
		val RIGHT = Action(104)
		val STOP = Action(105)
		val AUTO = Action(206)
		val MANUAL = Action(207)
	}

	/**
	 * @return String with the content to send to the rover.
	 */
	override fun toString(): String {
		val signalString = this.signalToSend.toString()
		val actionType = when (signalString[0]) {
			'1' -> "movement"
			'2' -> "control"
			'3' -> "camera"
			'4' -> "light"
			'5' -> "sound"
			'6' -> "sensor"
			'7' -> "other"
			else -> throw IllegalArgumentException("Invalid action type")
		}

		val signalNumberToSend = signalString.substring(1).toInt()

		return """
			{
				"$actionType": "$signalNumberToSend"
			}
		""".trimIndent()
	}
}
