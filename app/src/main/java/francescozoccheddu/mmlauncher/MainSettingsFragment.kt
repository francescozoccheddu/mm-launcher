package francescozoccheddu.mmlauncher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_SETTINGS
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.lang.RuntimeException

class MainSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val activeTargetsPref: MultiSelectListPreference =
            findPreference(resources.getString(R.string.pref_active_targets_key))!!
        val allTargets = TargetManager.getAllTargets(requireContext())
        activeTargetsPref.entries = allTargets.map { it.label }.toTypedArray()
        activeTargetsPref.entryValues = allTargets.map { it.name }.toTypedArray()

        val systemSettingsPref: Preference =
            findPreference(resources.getString(R.string.pref_system_settings_key))!!
        systemSettingsPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val context = requireContext()
            val packageManager = context.packageManager
            try {
                val intent = packageManager.getLaunchIntentForPackage("com.android.tv.settings")
                context.startActivity(intent)
            } catch (e: RuntimeException) {
                startActivity(Intent(ACTION_SETTINGS))
            }
            true
        }

    }

}