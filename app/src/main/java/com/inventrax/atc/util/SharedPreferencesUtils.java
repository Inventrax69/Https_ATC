package com.inventrax.atc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Map;

/**
 * Helper to work with the shared preferences
 */
public class SharedPreferencesUtils {


    private static SharedPreferencesUtils defaultSharedPreferencesHelper = new SharedPreferencesUtils(null, null);

    private String name;
    private Context context;

    public SharedPreferencesUtils(String name, Context context) {
        this.name = name;
        this.context = context;
    }

    public static SharedPreferencesUtils get(String name, Context context) {
        return new SharedPreferencesUtils(name, context);
    }

    public static SharedPreferencesUtils get() {
        return defaultSharedPreferencesHelper;
    }

    public Editor getEditor() {
        return getSharedPreferences().edit();
    }

    public SharedPreferences getSharedPreferences() {
        if (name != null) {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }


    public void savePreference(String key, String value) {
        Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();

    }

    public void savePreferenceAsync(String key, String value) {
        Editor editor = getEditor();
        editor.putString(key, value);
        editor.apply();

    }

    public void savePreference(String key, Boolean value) {
        Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.commit();

    }

    public void savePreferenceAsync(String key, Boolean value) {
        Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();

    }

    public void savePreference(String key, Integer value) {
        Editor editor = getEditor();
        editor.putInt(key, value);
        editor.commit();

    }

    public void savePreferenceAsync(String key, Integer value) {
        Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();

    }

    public void savePreference(String key, Long value) {
        Editor editor = getEditor();
        editor.putLong(key, value);
        editor.commit();

    }

    public void savePreferenceAsync(String key, Long value) {
        Editor editor = getEditor();
        editor.putLong(key, value);
        editor.apply();

    }

    public void savePreference(String key, Float value) {
        Editor editor = getEditor();
        editor.putFloat(key, value);
        editor.commit();

    }

    public void savePreferenceAsync(String key, Float value) {
        Editor editor = getEditor();
        editor.putFloat(key, value);
        editor.apply();

    }

    /**
     * Retrieves all the existent shared preferences.
     *
     * @return The shared preferences.
     */
    public Map<String, ?> loadAllPreferences() {
        return getSharedPreferences().getAll();
    }


    /**
     * Retrieve a string value from the preferences.
     *
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return the preference value if it exists, or defaultValue.
     */
    public String loadPreference(String key, String defaultValue) {
        String value = getSharedPreferences().getString(key, defaultValue);

        return value;
    }

    /**
     * Retrieve a string value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @return the preference value if it exists, or null.
     */
    public String loadPreference(String key) {
        return loadPreference(key, null);
    }

    /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return the preference value if it exists, or defaultValue.
     */
    public Boolean loadPreferenceAsBoolean(String key, Boolean defaultValue) {
        Boolean value = defaultValue;
        if (hasPreference(key)) {
            value = getSharedPreferences().getBoolean(key, false);
        }

        return value;
    }

    /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @return the preference value if it exists, or null.
     */
    public Boolean loadPreferenceAsBoolean(String key) {
        return loadPreferenceAsBoolean(key, null);
    }

    /**
     * Retrieve a long value from the preferences.
     *
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return the preference value if it exists, or defaultValue.
     */
    public Long loadPreferenceAsLong(String key, Long defaultValue) {
        Long value = defaultValue;
        if (hasPreference(key)) {
            value = getSharedPreferences().getLong(key, 0L);
        }

        return value;

    }

    /**
     * Retrieve a long value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @return the preference value if it exists, or null.
     */
    public Long loadPreferenceAsLong(String key) {
        return loadPreferenceAsLong(key, null);
    }

    /**
     * Retrieve an Integer value from the preferences.
     *
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return the preference value if it exists, or defaultValue.
     */
    public Integer loadPreferenceAsInteger(String key, Integer defaultValue) {
        Integer value = defaultValue;
        if (hasPreference(key)) {
            value = getSharedPreferences().getInt(key, 0);
        }

        return value;
    }

    /**
     * Retrieve an Integer value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @return the preference value if it exists, or null.
     */
    public Integer loadPreferenceAsInteger(String key) {
        return loadPreferenceAsInteger(key, null);
    }

    /**
     * Retrieve a Float value from the preferences.
     *
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return the preference value if it exists, or defaultValue.
     */
    public Float loadPreferenceAsFloat(String key, Float defaultValue) {
        Float value = defaultValue;
        if (hasPreference(key)) {
            value = getSharedPreferences().getFloat(key, 0);
        }

        return value;
    }

    /**
     * Retrieve a Float value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @return the preference value if it exists, or null.
     */
    public Float loadPreferenceAsFloat(String key) {
        return loadPreferenceAsFloat(key, null);
    }

    public boolean hasPreference(String key) {
        return getSharedPreferences().contains(key);
    }

    public void removePreferences(String... keys) {
        Editor editor = getEditor();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }

    public void removeAllPreferences() {
        Editor editor = getEditor();
        editor.clear();
        editor.commit();
    }

}
