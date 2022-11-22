package es.unileon.sawppy.ui

/**
 * Enum class that represents the different actions that can be performed by the user.
 */
enum class ActionType(val signalToSend: Byte) {
	BACKWARD(0), FORWARD(1), RIGHT(2), LEFT(3), STOP(4)
}
