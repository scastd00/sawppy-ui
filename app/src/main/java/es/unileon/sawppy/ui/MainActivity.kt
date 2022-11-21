package es.unileon.sawppy.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    val movementHandler = MovementHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun moveForward(view: View) {
        this.movementHandler.move(MovementType.FORWARD)
    }

    fun moveBackward(view: View) {
        this.movementHandler.move(MovementType.BACKWARD)
    }

    fun moveLeft(view: View) {
        this.movementHandler.move(MovementType.LEFT)
    }

    fun moveRight(view: View) {
        this.movementHandler.move(MovementType.RIGHT)
    }

    fun stop(view: View) {
        this.movementHandler.stop();
    }
}