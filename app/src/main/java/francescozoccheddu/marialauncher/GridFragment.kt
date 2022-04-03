package francescozoccheddu.marialauncher

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*

class GridFragment : VerticalGridSupportFragment(), OnItemViewClickedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rowsAdapter = ArrayObjectAdapter(TargetPresenter())
        adapter = rowsAdapter
        onItemViewClickedListener = this
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)
        val activities = TargetRetriever.getLaunchableActivities(requireContext())
        for (app in activities) {
            rowsAdapter.add(app)
        }
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?, item: Any,
        rowViewHolder: RowPresenter.ViewHolder?, row: Row?
    ) {
    }

    companion object {
        private const val NUM_COLUMNS = 5
    }

}