package francescozoccheddu.mmlauncher

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*
import androidx.preference.PreferenceManager

class GridFragment : VerticalGridSupportFragment(), OnItemViewClickedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onItemViewClickedListener = this
        adapter = ArrayObjectAdapter(TargetPresenter())
        updatePresenter()
        populate()
    }

    private fun populate() {
        val targetsAdapter = adapter as ArrayObjectAdapter
        targetsAdapter.clear()
        targetsAdapter.addAll(0, TargetManager.activeTargets)
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?, item: Any,
        rowViewHolder: RowPresenter.ViewHolder?, row: Row?
    ) {
        (item as Target).launch(requireContext())
    }

    private fun updatePresenter() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val columnCount = prefs.getInt(
            resources.getString(R.string.pref_column_count_key),
            resources.getInteger(R.integer.pref_column_count_default)
        )
        val focusHighlight = when (prefs.getString(
            resources.getString(R.string.pref_focus_zoom_key),
            resources.getInteger(R.integer.pref_focus_zoom_default).toString()
        )!!.toInt()) {
            0 -> FocusHighlight.ZOOM_FACTOR_NONE
            1 -> FocusHighlight.ZOOM_FACTOR_XSMALL
            2 -> FocusHighlight.ZOOM_FACTOR_SMALL
            4 -> FocusHighlight.ZOOM_FACTOR_LARGE
            else -> FocusHighlight.ZOOM_FACTOR_MEDIUM
        }
        val useDimmer = prefs.getBoolean(
            resources.getString(R.string.pref_use_dimmer_key),
            resources.getBoolean(R.bool.pref_use_dimmer_default)
        )
        gridPresenter = VerticalGridPresenter(focusHighlight, useDimmer).apply {
            numberOfColumns = columnCount
        }
        val targetPresenter = (adapter as ArrayObjectAdapter).getPresenter(null) as TargetPresenter
        targetPresenter.updateCardHeight(requireContext())
    }

}