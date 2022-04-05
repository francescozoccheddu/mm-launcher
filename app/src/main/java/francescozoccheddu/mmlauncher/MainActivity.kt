package francescozoccheddu.mmlauncher

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView

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
        Prefs.update(this)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        setupHeaderAnimation()
    }

    private lateinit var showAnimation: Animation
    private lateinit var hideAnimation: Animation
    private var wasOverlapping = false

    private fun setupHeaderAnimation() {
        val gridFragment =
            supportFragmentManager.findFragmentById(R.id.main_grid_fragment) as GridFragment
        val headerView: ViewGroup = findViewById(R.id.main_header)
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