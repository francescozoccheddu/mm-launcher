package francescozoccheddu.marialauncher

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TargetManager.update(baseContext)
        setContentView(R.layout.activity_main)
    }

}