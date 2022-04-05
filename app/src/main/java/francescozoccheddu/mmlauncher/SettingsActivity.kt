package francescozoccheddu.mmlauncher

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val MIN_BACK_DELAY_MS = 1500
    }

    private var creationTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        creationTime = System.currentTimeMillis()
        setContentView(R.layout.activity_settings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_fragment, MainSettingsFragment())
            .commit()
    }

    private fun exit() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - creationTime > MIN_BACK_DELAY_MS) {
            exit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                exit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}