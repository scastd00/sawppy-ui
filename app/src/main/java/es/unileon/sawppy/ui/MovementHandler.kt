package es.unileon.sawppy.ui

class MovementHandler {
    fun move(type: MovementType) {
        println("Moving ${type.signalToSend}")
    }

    fun stop() {
        println("Stopping")
    }
}