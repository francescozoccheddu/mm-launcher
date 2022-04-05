package francescozoccheddu.mmlauncher

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*

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
        targetsAdapter.addAll(0, Prefs.activeTargets)
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?, item: Any,
        rowViewHolder: RowPresenter.ViewHolder?, row: Row?
    ) {
        (item as Target).launch(requireContext())
    }

    private fun updatePresenter() {
        val focusHighlight = when (Prefs.focusZoom) {
            Prefs.FocusZoom.None -> FocusHighlight.ZOOM_FACTOR_NONE
            Prefs.FocusZoom.VerySmall -> FocusHighlight.ZOOM_FACTOR_XSMALL
            Prefs.FocusZoom.Small -> FocusHighlight.ZOOM_FACTOR_SMALL
            Prefs.FocusZoom.Medium -> FocusHighlight.ZOOM_FACTOR_MEDIUM
            Prefs.FocusZoom.Large -> FocusHighlight.ZOOM_FACTOR_LARGE
        }
        gridPresenter = VerticalGridPresenter(focusHighlight, Prefs.useDimmer).apply {
            numberOfColumns = Prefs.columnCount
        }
        val targetPresenter = (adapter as ArrayObjectAdapter).getPresenter(null) as TargetPresenter
        targetPresenter.updateCardHeight(requireContext().resources)
    }

}