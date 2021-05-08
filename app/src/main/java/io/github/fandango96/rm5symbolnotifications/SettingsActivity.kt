package io.github.fandango96.rm5symbolnotifications

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = object : PreferenceDataStore() {
                override fun getBoolean(key: String, defValue: Boolean): Boolean {
                    if (key == "led_effect") {
                        return Settings.Global.getInt(activity?.contentResolver, "nubia_symbol_mode") == 1
                    }
                    return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(key, defValue)
                }

                override fun putBoolean(key: String, value: Boolean) {
                    if (key == "led_effect") {
                        Settings.Global.putInt(activity?.contentResolver, "nubia_symbol_mode", if (value) 1 else 0)
                        return
                    }
                    PreferenceManager.getDefaultSharedPreferences(activity).edit().run {
                        putBoolean(key, value)
                        apply()
                    }
                }
            }
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}
