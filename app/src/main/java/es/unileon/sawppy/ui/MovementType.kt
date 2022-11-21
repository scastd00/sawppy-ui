package es.unileon.sawppy.ui

enum class MovementType(val signalToSend: Byte) {
    FORWARD(1),
    BACKWARD(0),
    LEFT(3),
    RIGHT(2);
}