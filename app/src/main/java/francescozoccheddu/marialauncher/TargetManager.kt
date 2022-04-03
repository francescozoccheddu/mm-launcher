package francescozoccheddu.marialauncher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager

object TargetManager {

    var allTargets: Set<Target> = emptySet()
        private set

    var activeTargets: Set<Target> = emptySet()
        set(value) {
            field = value.filter {
                allTargets.contains(it)
            }.toSortedSet(compareBy { it.label })
        }

    private fun updateAllTargets(context: Context) {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val packages = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        val thisPackageName = context.applicationContext.packageName
        allTargets = packages
            .mapNotNull { Target(it.activityInfo, packageManager) }
            .filter { it.packageName != thisPackageName }
            .toSortedSet(compareBy { it.label })
    }

    fun update(context: Context) {
        updateAllTargets(context)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val allTargetNames = allTargets.map { it.name }.toSet()
        val activeTargetNames = prefs.getStringSet(
            context.resources.getString(R.string.pref_active_targets_key),
            allTargetNames
        )!!.toSet()
        activeTargets = allTargets.filter {
            activeTargetNames.contains(it.name)
        }.toSet()
    }

}