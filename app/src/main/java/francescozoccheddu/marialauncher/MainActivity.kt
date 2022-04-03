package francescozoccheddu.marialauncher

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.leanback.widget.*


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ItemBridgeAdapter()
        val list = LaunchableActivityRetriever.getLaunchableActivities(baseContext)
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = LaunchableActivityCardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        for (app in list) {
            listRowAdapter.add(app)
        }
        rowsAdapter.add(ListRow(listRowAdapter))
        adapter.setAdapter(rowsAdapter)
        adapter.setPresenter(SinglePresenterSelector(ListRowPresenter()))
        val grid = findViewById<VerticalGridView>(R.id.grid)
        grid.adapter = adapter
    }

}