package francescozoccheddu.mmlauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    companion object {
        private const val MAX_BACK_ELAPSED_TIME: Double = 2.0
        private const val BACK_COUNT_FOR_TOAST: Int = 5
        private const val BACK_COUNT_FOR_SETTINGS: Int = 10
    }

    private var lastBackTime: Long? = null
    private var backCount = 0
    private var backToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TargetManager.update(this)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        if (lastBackTime != null) {
            val elapsed = (System.nanoTime() - lastBackTime!!) / 1_000_000_000.0
            if (elapsed <= MAX_BACK_ELAPSED_TIME) {
                backCount++
            } else {
                backCount = 1
            }
        } else {
            backCount = 1
        }
        lastBackTime = System.nanoTime()
        if (backCount >= BACK_COUNT_FOR_SETTINGS) {
            backCount = 0
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
        if (backCount >= BACK_COUNT_FOR_TOAST) {
            val remaining = BACK_COUNT_FOR_SETTINGS - backCount
            val message = resources.getQuantityString(
                R.plurals.toast_settings_taps,
                remaining,
                remaining
            )
            backToast?.cancel()
            backToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
            backToast!!.show()
        }
    }

}