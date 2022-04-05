package francescozoccheddu.mmlauncher

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import kotlin.math.max

class MainActivity : FragmentActivity() {

    companion object {
        private const val MAX_TAP_DELAY: Long = 2000
        private const val TAPS_FOR_TOAST: Int = 5
        private const val TAPS_FOR_SETTINGS: Int = 10
    }

    private val handler = Handler(Looper.getMainLooper())
    private val tapsResetter = Runnable {
        taps = 0
        updateRemainingTapsForSettings()
    }
    private var taps = 0
    private var tapsToast: Toast? = null
    private var hasTargets: Boolean = false
    private lateinit var helpInstructionsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.update(this)
        hasTargets = Prefs.activeTargets.isNotEmpty()
        setContentView(R.layout.activity_main)
        if (!hasTargets) {
            findViewById<ViewStub>(R.id.main_help_stub).inflate()
            helpInstructionsTextView = findViewById(R.id.help_instructions_text)
        } else {
            findViewById<ViewStub>(R.id.main_header_stub).inflate()
        }
        updateRemainingTapsForSettings()
    }

    override fun onResume() {
        super.onResume()
        if (hasTargets) {
            setupHeaderAnimation()
        }
    }

    private lateinit var showAnimation: Animation
    private lateinit var hideAnimation: Animation
    private var wasOverlapping = false

    private fun setupHeaderAnimation() {
        val gridFragment =
            supportFragmentManager.findFragmentById(R.id.main_grid_fragment) as GridFragment
        val headerView: ViewGroup = findViewById(R.id.header)
        val verticalGridView: VerticalGridView = gridFragment.requireView()
            .findViewById(androidx.leanback.R.id.browse_grid)
        val firstChildPosition = IntArray(2)
        val headerPosition = IntArray(2)
        showAnimation = AnimationUtils.loadAnimation(this, R.anim.header_show)
        hideAnimation = AnimationUtils.loadAnimation(this, R.anim.header_hide)
        verticalGridView.setOnScrollChangeListener { _, _, _, _, _ ->
            val firstChild = verticalGridView.getChildAt(0) ?: return@setOnScrollChangeListener
            firstChild.getLocationOnScreen(firstChildPosition)
            headerView.getLocationOnScreen(headerPosition)
            val firstChildTop = firstChildPosition[1]
            val headerBottom = headerPosition[1] + headerView.measuredHeight
            val overlaps = firstChildTop < headerBottom
            if (overlaps != wasOverlapping) {
                wasOverlapping = overlaps
                headerView.clearAnimation()
                headerView.startAnimation(if (overlaps) hideAnimation else showAnimation)
            }
        }
    }

    private fun launchSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateRemainingTapsForSettings() {
        val remaining = max(TAPS_FOR_SETTINGS - taps, 0)
        if (hasTargets) {
            tapsToast?.cancel()
            if (taps >= TAPS_FOR_TOAST && remaining > 0) {
                val message = resources.getQuantityString(
                    R.plurals.toast_settings_remaining,
                    remaining,
                    remaining
                )
                tapsToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                tapsToast!!.show()
            }
        } else {
            val message =
                when (remaining) {
                    0 -> resources.getString(R.string.help_instructions_done)
                    TAPS_FOR_SETTINGS -> resources.getString(R.string.help_instructions, remaining)
                    else -> resources.getQuantityString(
                        R.plurals.help_instructions_remaining,
                        remaining,
                        remaining
                    )
                }
            helpInstructionsTextView.text = message
        }
    }

    override fun onBackPressed() {
        taps++
        if (taps >= TAPS_FOR_SETTINGS) {
            launchSettings()
        }
        handler.removeCallbacks(tapsResetter)
        handler.postDelayed(tapsResetter, MAX_TAP_DELAY)
        updateRemainingTapsForSettings()
    }

}