package francescozoccheddu.mmlauncher

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Interpolator
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import kotlin.math.ceil
import kotlin.math.max


class MainActivity : FragmentActivity() {

    companion object {
        private const val MAX_TAP_DELAY: Long = 2000
        private const val TAPS_FOR_TOAST: Int = 5
        private const val TAPS_FOR_SETTINGS: Int = 10
        private const val MAX_WALLPAPER_SLIDE_FACTOR: Float = 0.2f
        private const val MIN_WALLPAPER_SLIDE_FACTOR: Float = 0.05f
        private const val MAX_WALLPAPER_SLIDE_FACTOR_ROW_COUNT: Int = 10
    }

    private abstract inner class TapManager {

        private val handler = Handler(Looper.getMainLooper())
        protected var taps = 0
            private set
        private val resetter = Runnable {
            taps = 0
            updateRemainingTapsForSettings()
        }

        protected val remaining get() = TAPS_FOR_SETTINGS - taps

        protected abstract fun updateRemainingTapsForSettings()

        private fun launchSettings() {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun tap() {
            taps++
            if (taps >= TAPS_FOR_SETTINGS) {
                launchSettings()
            }
            handler.removeCallbacks(resetter)
            handler.postDelayed(resetter, MAX_TAP_DELAY)
            updateRemainingTapsForSettings()
        }

    }

    private inner class HelpTapManager : TapManager() {

        private val instructionsTextView: TextView = findViewById(R.id.help_instructions_text)

        init {
            updateRemainingTapsForSettings()
        }

        override fun updateRemainingTapsForSettings() {
            instructionsTextView.text = when (remaining) {
                0 -> resources.getString(R.string.help_instructions_done)
                TAPS_FOR_SETTINGS -> resources.getString(
                    R.string.help_instructions,
                    remaining
                )
                else -> resources.getQuantityString(
                    R.plurals.help_instructions_remaining,
                    remaining,
                    remaining
                )
            }
        }

    }

    private inner class ToastTapManager : TapManager() {

        private var toast: Toast? = null

        override fun updateRemainingTapsForSettings() {
            toast?.cancel()
            if (taps >= TAPS_FOR_TOAST && remaining > 0) {
                val message = resources.getQuantityString(
                    R.plurals.toast_settings_remaining,
                    remaining,
                    remaining
                )
                toast = Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                toast!!.show()
            }
        }

    }

    private inner class GridManager {

        init {
            findViewById<View>(R.id.main_grid_fragment)
                .startAnimation(
                    AnimationUtils.loadAnimation(
                        this@MainActivity,
                        R.anim.wallpaper_start
                    )
                )
        }

        val rowCount = ceil(Prefs.activeTargets.size.toFloat() / Prefs.columnCount).toInt()

        fun setup() {
            val fragment =
                supportFragmentManager.findFragmentById(R.id.main_grid_fragment) as GridFragment
            val view: VerticalGridView = fragment.requireView()
                .findViewById(androidx.leanback.R.id.browse_grid)
            val firstChildPosition = IntArray(2)
            view.setOnScrollChangeListener { _, _, _, _, _ ->
                val firstChild = view.getChildAt(0)!!
                firstChild.getLocationOnScreen(firstChildPosition)
                val firstChildTop = firstChildPosition[1]
                headerManager.gridTop = firstChildTop
            }
            view.setOnChildSelectedListener { _, _, position, _ ->
                val row = position / Prefs.columnCount
                wallpaperManager.progress = row.toFloat() / max(rowCount - 1, 1)
            }
        }

    }

    private inner class HeaderManager {

        private val showAnimation =
            AnimationUtils.loadAnimation(this@MainActivity, R.anim.header_show)
        private val hideAnimation =
            AnimationUtils.loadAnimation(this@MainActivity, R.anim.header_hide)
        private var wasOverlapping: Boolean = false
        private var started: Boolean = false
        private val view: ViewGroup = findViewById(R.id.header)
        private val childPanelView: ViewGroup = view.findViewById(R.id.header_child_panel)
        private val position = IntArray(2)

        fun update() {
            val actualGridTop = gridTop ?: return
            view.getLocationOnScreen(position)
            val headerBottom = position[1] + view.measuredHeight
            childPanelView.translationY = (actualGridTop - headerBottom).toFloat()
            val overlaps = actualGridTop < headerBottom
            if (!overlaps && !started) {
                started = true
                view.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@MainActivity,
                        R.anim.header_start
                    )
                )
            } else if (overlaps != wasOverlapping) {
                wasOverlapping = overlaps
                view.clearAnimation()
                view.startAnimation(if (overlaps) hideAnimation else showAnimation)
            }
        }

        var gridTop: Int? = null
            set(value) {
                if (value == field) {
                    return
                }
                field = value
                update()
            }

    }

    private inner class WallpaperManager {

        private val imageView: ImageView = findViewById(R.id.main_wallpaper)

        init {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.wallpaper)
            imageView.setImageBitmap(bitmap)
            imageView.startAnimation(
                AnimationUtils.loadAnimation(
                    this@MainActivity,
                    R.anim.wallpaper_start
                )
            )
            update()
        }

        fun update() {
            val imageWidth = imageView.drawable.intrinsicWidth
            val imageHeight = imageView.drawable.intrinsicHeight
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val fitScaleX = screenWidth.toFloat() / imageWidth
            val fitScaleY = screenHeight.toFloat() / imageHeight
            val fitScale: Float = max(fitScaleX, fitScaleY)
            val rowCount = gridManager?.rowCount ?: 0
            val slideFactor = if (rowCount == 0) {
                0.0f
            } else {
                val progress = rowCount.toFloat() / MAX_WALLPAPER_SLIDE_FACTOR_ROW_COUNT
                MIN_WALLPAPER_SLIDE_FACTOR * (1 - progress) + MAX_WALLPAPER_SLIDE_FACTOR * progress
            }
            val scale = fitScale * (1 + slideFactor)
            val maxTranslation = screenHeight * slideFactor
            imageView.imageMatrix = Matrix().apply {
                setScale(
                    scale,
                    scale
                )
                postTranslate(0.0f, -animatedProgress * maxTranslation)
            }
        }

        private var animatedProgress: Float = 0f
        private var animator: ValueAnimator? = null

        var progress: Float = 0f
            set(value) {
                if (value == field) {
                    return
                }
                animator?.cancel()
                animator = ValueAnimator.ofFloat(animatedProgress, value).apply {
                    addUpdateListener { animation ->
                        animatedProgress = animation.animatedValue as Float
                        update()
                    }
                    duration = 100
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
                field = value
            }

    }

    private lateinit var tapManager: TapManager
    private lateinit var headerManager: HeaderManager
    private lateinit var wallpaperManager: WallpaperManager
    private var gridManager: GridManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.update(this)
        val hasTargets = Prefs.activeTargets.isNotEmpty()
        setContentView(R.layout.activity_main)
        if (hasTargets) {
            findViewById<ViewStub>(R.id.main_header_stub).inflate()
            gridManager = GridManager()
            headerManager = HeaderManager()
            tapManager = ToastTapManager()
        } else {
            findViewById<ViewStub>(R.id.main_help_stub).inflate()
            tapManager = HelpTapManager()
        }
        wallpaperManager = WallpaperManager()
    }

    override fun onResume() {
        super.onResume()
        gridManager?.setup()
        wallpaperManager.update()
    }

    override fun onBackPressed() {
        tapManager.tap()
    }

}