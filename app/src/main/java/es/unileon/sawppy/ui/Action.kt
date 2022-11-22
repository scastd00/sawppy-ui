package es.unileon.sawppy.ui

/**
 * Data class that represents the different actions that can be performed by the user.
 *
 * @param signalToSend The signal to send to the rover. Numbering is done as follows:
 * - 1xx: movement commands
 * - 2xx: control mode commands
 * - 3xx: camera commands
 * - 4xx: light commands
 * - 5xx: sound commands
 * - 6xx: sensor commands
 * - 7xx: other commands
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

	private fun getTypeNumber(): Int {
		return this.signalToSend / 100
	}

	private fun getNumber(): Int {
		return this.signalToSend % 100
	}

	fun isMovement(): Boolean {
		return this.getTypeNumber() == 1
	}

	fun isControlMode(): Boolean {
		return this.getTypeNumber() == 2
	}

	fun isCamera(): Boolean {
		return this.getTypeNumber() == 3
	}

	fun isLight(): Boolean {
		return this.getTypeNumber() == 4
	}

	fun isSound(): Boolean {
		return this.getTypeNumber() == 5
	}

	fun isSensor(): Boolean {
		return this.getTypeNumber() == 6
	}

	fun isOther(): Boolean {
		return this.getTypeNumber() == 7
	}

	/**
	 * @return String with the content to send to the rover.
	 */
	override fun toString(): String {
		val actionType = when (getTypeNumber()) {
			1 -> "movement"
			2 -> "control"
			3 -> "camera"
			4 -> "light"
			5 -> "sound"
			6 -> "sensor"
			7 -> "other"
			else -> throw IllegalArgumentException("Invalid action type")
		}

		return """{"$actionType": ${getNumber()}}"""
	}
}
