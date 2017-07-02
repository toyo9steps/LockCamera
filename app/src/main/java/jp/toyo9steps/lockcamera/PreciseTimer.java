package jp.toyo9steps.lockcamera;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import java.util.Calendar;

import jp.toyo9steps.lockcamera.AlarmReceiver;

/**
 * Androidの各バージョンに応じて正確なタイマー機能を提供するクラス
 */

public class PreciseTimer {

	private Context mContext;
	private AlarmManager mAlarm;

	public PreciseTimer(Context context) {
		mContext = context;
		mAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	private void set(long triggerAtMillis, PendingIntent operation) {
		if(VERSION.SDK_INT >= VERSION_CODES.M) {
			mAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
		}
		else if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			mAlarm.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
		}
		else{
			mAlarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
		}
	}

	private void cancel(PendingIntent operation) {
		mAlarm.cancel(operation);
	}

	public boolean set(int requestCode, int hour, int minute, int dowBits){
		return set(requestCode, hour, minute, false, dowBits);
	}

	/* フラグtomorrowをtrueに設定すると、必ず日付が明日に設定される */
	public boolean set(int requestCode, int hour, int minute, boolean tomorrow, int dowBits){
		Intent intent = new Intent(mContext, AlarmReceiver.class);
		intent.putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, requestCode);
		intent.putExtra(AlarmReceiver.EXTRA_TIME_HOUR, hour);
		intent.putExtra(AlarmReceiver.EXTRA_TIME_MINUTE, minute);
		intent.putExtra(AlarmReceiver.EXTRA_TIME_DOW_BITS, dowBits);
		PendingIntent pending = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar calendar = Calendar.getInstance();
		long todayMillis = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		/* 指定した時刻が今現在の時刻より前か後ろか判定し、前だったら明日の指定時刻に設定する */
		/* 過去を設定すると即時発火してしまうのを避けるため。 */
		boolean ret = false;
		if (tomorrow || calendar.getTimeInMillis() <= todayMillis) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			ret = true;
		}
		set(calendar.getTimeInMillis(), pending);

		/* 戻り値は明日にタイマーが設定されたらtrue, 今日にタイマーが設定されたらfalseを返す */
		return ret;
	}

	public void cancel(int requestCode) {
		Intent intent = new Intent(mContext, AlarmReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		cancel(pending);
	}
}
