package es.unileon.sawppy.ui

enum class ActionType(val signalToSend: Byte) {
	BACKWARD(0), FORWARD(1), RIGHT(2), LEFT(3), STOP(4)
}
