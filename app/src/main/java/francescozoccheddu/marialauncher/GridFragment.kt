package francescozoccheddu.marialauncher

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*

class GridFragment : VerticalGridSupportFragment(), OnItemViewClickedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onItemViewClickedListener = this
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)
        val targetsAdapter = ArrayObjectAdapter(TargetPresenter())
        adapter = targetsAdapter
        targetsAdapter.addAll(0, TargetManager.getTargets(requireContext()))
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?, item: Any,
        rowViewHolder: RowPresenter.ViewHolder?, row: Row?
    ) {
        (item as Target).launch(requireContext())
    }

    companion object {
        private const val NUM_COLUMNS = 4
    }

}