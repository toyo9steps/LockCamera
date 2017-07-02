package jp.toyo9steps.lockcamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 設定値を永続化したり、読みだしたりするクラス
 * SharedPreferencesのラッパー。
 */

public class SettingLoader {

	private static final String SHARED_PREFS_NAME = "settings";
	private static final String KEY_SETTING_MODE = "KEY_SETTING_MODE";
	private static final String KEY_START_TIME_HOUR = "KEY_START_TIME_HOUR";
	private static final String KEY_START_TIME_MINUTE = "KEY_START_TIME_MINUTE";
	private static final String KEY_END_TIME_HOUR = "KEY_END_TIME_HOUR";
	private static final String KEY_END_TIME_MINUTE = "KEY_END_TIME_MINUTE";
	private static final String KEY_TIMER_DOW = "KEY_TIMER_DOW";
	public static final int SETTING_MODE_MANUAL = 0;
	public static final int SETTING_MODE_TIMER = 1;
	public static final int TIMER_DOW_SUNDAY = 0x0001;
	public static final int TIMER_DOW_MONDAY = 0x0002;
	public static final int TIMER_DOW_TUESDAY = 0x0004;
	public static final int TIMER_DOW_WEDNESDAY = 0x0008;
	public static final int TIMER_DOW_THURSDAY = 0x0010;
	public static final int TIMER_DOW_FRYDAY = 0x0020;
	public static final int TIMER_DOW_SATURDAY = 0x0040;
	public static final int TIMER_DOW_EVERYDAY = 0x007F;

    private SharedPreferences mPrefs;
	public int settingMode;
    public int startTimeHour;
    public int startTimeMinute;
    public int endTimeHour;
    public int endTimeMinute;
	public int timerDow;

    public SettingLoader(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		settingMode = mPrefs.getInt(KEY_SETTING_MODE, SETTING_MODE_MANUAL);
		startTimeHour = mPrefs.getInt(KEY_START_TIME_HOUR, -1);
        startTimeMinute = mPrefs.getInt(KEY_START_TIME_MINUTE, -1);
        endTimeHour = mPrefs.getInt(KEY_END_TIME_HOUR, -1);
        endTimeMinute = mPrefs.getInt(KEY_END_TIME_MINUTE, -1);
		timerDow = mPrefs.getInt(KEY_TIMER_DOW, TIMER_DOW_EVERYDAY);
	}

	public void saveSettingMode(int mode) {
		settingMode = mode;
		mPrefs.edit().putInt(KEY_SETTING_MODE, mode).apply();
	}

    public void saveStartTime(int hour, int minute) {
        startTimeHour = hour;
        startTimeMinute = minute;
        Editor editor = mPrefs.edit();
        editor.putInt(KEY_START_TIME_HOUR, hour);
        editor.putInt(KEY_START_TIME_MINUTE, minute);
        editor.apply();
    }

    public void saveEndTime(int hour, int minute) {
        endTimeHour = hour;
        endTimeMinute = minute;
        Editor editor = mPrefs.edit();
        editor.putInt(KEY_END_TIME_HOUR, hour);
        editor.putInt(KEY_END_TIME_MINUTE, minute);
        editor.apply();
    }

    public boolean timeIsValid() {
        return startTimeHour >= 0 && startTimeMinute >= 0 && endTimeHour >= 0 && endTimeMinute >= 0;
    }

	public void saveTimerDow(int dowBits){
		timerDow = dowBits;
		Editor editor = mPrefs.edit();
		editor.putInt(KEY_TIMER_DOW, dowBits);
		editor.apply();
	}

	/* Calendarの曜日をdowBitに変換する */
	public static int calendarDowToDowBit(int calendarDow){
		/* Calendarの曜日は1-indexなので1を引くことに注意 */
		return 0x0001 << (calendarDow - 1);
	}
}
