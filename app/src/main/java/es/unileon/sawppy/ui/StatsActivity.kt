package es.unileon.sawppy.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
	private val movementHandler: MovementHandler = MovementHandler.INSTANCE

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_stats)
	}

	fun goToMainActivity(view: View) {
		val intent = Intent(this, MainActivity::class.java)
		startActivity(intent)
		finish()
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
	}

	@Deprecated("Deprecated in Java")
	override fun onBackPressed() {
		super.onBackPressed()
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
	}
}
