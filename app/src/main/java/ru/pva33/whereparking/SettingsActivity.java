package ru.pva33.whereparking;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = "PVA_DEBUG";
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                    index >= 0
                        ? listPreference.getEntries()[index]
                        : null
                );

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue)
                    );

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
            || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
            || !isXLargeTablet(context);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
            PreferenceManager
                .getDefaultSharedPreferences(
                    preference.getContext()
                )
                .getString(
                    preference.getKey(),
                    ""
                )
        );
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
//        return super.isValidFragment(fragmentName);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }
        final String GENERAL_TAG = "prefs_general";
        final String NOTIFICATION_TAG = "prefs_notification";
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        getFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new SimplePreferenceFragment())
            .commit();
/*        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment newFragment = fm.findFragmentByTag(GENERAL_TAG);
        if (newFragment == null) {
            newFragment = new GeneralPreferenceFragment();
            Log.d(TAG, "new fragment general");
        }
        // find the old to know should wee replase or add to this container
        Fragment oldFragment = fm.findFragmentById(android.R.id.content);
        if (oldFragment == null) {
            Log.d(TAG, "add general to content");
            ft.add(android.R.id.content, newFragment, GENERAL_TAG);
        } else{
            Log.d(TAG, "replace general in content");
            ft.replace(android.R.id.content, newFragment, GENERAL_TAG);
        }
        // Optionally set nice transition
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        // Optionally add to backtrack
//        ft.addToBackStack(null);
//        ft.commit();

        newFragment = fm.findFragmentByTag(NOTIFICATION_TAG);
        if (newFragment == null) {
            Log.d(TAG, "new fragment notification");
            newFragment = new NotificationPreferenceFragment();
        }
        oldFragment = fm.findFragmentById(android.R.id.content);
        if (oldFragment == null) {
            Log.d(TAG, "add notification to content");
            Log.d(TAG, "android.R.id.content="+android.R.id.content);
            ft.add(android.R.id.content, newFragment, NOTIFICATION_TAG);
        } else {
            Log.d(TAG, "replace notification in content");
            ft.replace(android.R.id.content, newFragment, NOTIFICATION_TAG);
        }

        ft.commit();
*/
/*
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("example_text"));
        bindPreferenceSummaryToValue(findPreference("example_list"));
//        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        bindPreferenceSummaryToValue(findPreference("sync_frequency"));

        bindPreferenceSummaryToValue((findPreference("location_method")));
*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        } else {
//            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    public abstract static class PVAPreferenceFragment extends PreferenceFragment {
        /**
         * Link values in preferences controls and there summary text.
         */
        public abstract void bindPreferences();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PVAPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("example_text"));
//            bindPreferenceSummaryToValue(findPreference("example_list"));
//            bindPreferenceSummaryToValue(findPreference("location_method"));
            bindPreferences();
        }

        @Override
        public void bindPreferences() {
//            bindPreferenceSummaryToValue(findPreference("example_text"));
//            bindPreferenceSummaryToValue(findPreference("example_list"));
            bindPreferenceSummaryToValue(findPreference("location_method"));
            bindPreferenceSummaryToValue(findPreference("fire_distance"));
        }

    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PVAPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferences();
        }

        @Override
        public void bindPreferences() {
//            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            bindPreferenceSummaryToValue(findPreference("notification_1_time"));
            bindPreferenceSummaryToValue(findPreference("notification_2_time"));
            bindPreferenceSummaryToValue(findPreference("notification_3_time"));
            bindPreferenceSummaryToValue(findPreference("norification_text"));
        }
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PVAPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
            bindPreferences();
        }

        @Override
        public void bindPreferences() {
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SimplePreferenceFragment extends PVAPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceCategory fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_header_general);

            if (getPreferenceScreen() != null) {
                getPreferenceScreen().addPreference(fakeHeader);
            }
            addPreferencesFromResource(R.xml.pref_general);
//            PVAPreferenceFragment general = new GeneralPreferenceFragment();
//            general.bindPreferences();
            // Add 'notifications' preferences, and a corresponding header.
            fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_header_notifications);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_notification);
            /*fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_header_data_sync);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_data_sync);*/
            bindPreferences();
        }

        /**
         * Bind value of control and summary text, so when value is changed, the text of summary
         * changed too. Summary text is text below title of setting.
         */
        @Override
        public void bindPreferences() {
            // to start working default values from xml. may be bug?
//            PreferenceManager.setDefaultValues(this.getActivity(),R.xml.pref_general, false);
            bindPreferenceSummaryToValue(findPreference("fire_distance"));
            bindPreferenceSummaryToValue(findPreference("location_method"));
            // from notification
            bindPreferenceSummaryToValue(findPreference("notification_1_time"));
            bindPreferenceSummaryToValue(findPreference("notification_2_time"));
            bindPreferenceSummaryToValue(findPreference("notification_3_time"));
            bindPreferenceSummaryToValue(findPreference("norification_text"));
//            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            // from data_sync
//            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }
}
