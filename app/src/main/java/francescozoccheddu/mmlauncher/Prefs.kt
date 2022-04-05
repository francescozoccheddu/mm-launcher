package francescozoccheddu.mmlauncher

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import kotlin.reflect.KClass

object Prefs {

    enum class CardSize {
        VerySmall, Small, Medium, Large, VeryLarge
    }

    enum class FocusZoom {
        None, VerySmall, Small, Medium, Large
    }

    lateinit var activeTargets: Set<Target>
        private set

    var columnCount: Int = -1
        private set

    lateinit var cardSize: CardSize
        private set

    lateinit var focusZoom: FocusZoom
        private set

    var useDimmer: Boolean = false
        private set

    private fun <T : Enum<*>> getEnum(
        resources: Resources,
        preferences: SharedPreferences,
        keyResId: Int,
        valuesResId: Int,
        defaultValueResId: Int,
        enumClass: KClass<T>
    ): T {
        val values = resources.getStringArray(valuesResId)
        val value = getString(resources, preferences, keyResId, defaultValueResId)
        return enumClass.java.enumConstants!![values.indexOf(value)]
    }

    private fun getString(
        resources: Resources,
        preferences: SharedPreferences,
        keyResId: Int,
        defaultValueResId: Int
    ): String = preferences.getString(
        resources.getString(keyResId),
        resources.getString(defaultValueResId)
    )!!

    private fun getInt(
        resources: Resources,
        preferences: SharedPreferences,
        keyResId: Int,
        defaultValueResId: Int
    ): Int = preferences.getInt(
        resources.getString(keyResId),
        resources.getInteger(defaultValueResId)
    )

    private fun getBool(
        resources: Resources,
        preferences: SharedPreferences,
        keyResId: Int,
        defaultValueResId: Int
    ): Boolean = preferences.getBoolean(
        resources.getString(keyResId),
        resources.getBoolean(defaultValueResId)
    )

    private fun getStrings(
        resources: Resources,
        preferences: SharedPreferences,
        keyResId: Int,
        defaultValue: Set<String>
    ): Set<String> = preferences.getStringSet(
        resources.getString(keyResId),
        defaultValue
    )!!.toSet()

    fun update(context: Context) {
        val resources = context.resources
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val activeTargetNames = getStrings(
            resources,
            preferences,
            R.string.pref_active_targets_key,
            emptySet()
        )
        activeTargets = TargetManager.getAllTargets(context).filter {
            activeTargetNames.contains(it.name)
        }.toSortedSet(compareBy { it.label })
        columnCount = getInt(
            resources,
            preferences,
            R.string.pref_column_count_key,
            R.integer.pref_column_count_default
        )
        cardSize = getEnum(
            resources,
            preferences,
            R.string.pref_card_size_key,
            R.array.pref_card_size_entry_values,
            R.string.pref_card_size_default,
            CardSize::class
        )
        focusZoom = getEnum(
            resources,
            preferences,
            R.string.pref_focus_zoom_key,
            R.array.pref_focus_zoom_entry_values,
            R.string.pref_focus_zoom_default,
            FocusZoom::class
        )
        useDimmer = getBool(
            resources,
            preferences,
            R.string.pref_use_dimmer_key,
            R.bool.pref_use_dimmer_default
        )
    }

}