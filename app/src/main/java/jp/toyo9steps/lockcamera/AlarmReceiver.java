package jp.toyo9steps.lockcamera;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * AlarmManagerのタイマー発火を受け取るクラス
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_REQUEST_CODE = "AlarmReceiver.extra.REQUEST_CODE";
    public static final String EXTRA_TIME_HOUR = "AlarmReceiver.extra.TIME_HOUR";
    public static final String EXTRA_TIME_MINUTE = "AlarmReceiver.extra.TIME_MINUTE";
    public static final int REQUEST_DISABLE_START_TIME = 0;
    public static final int REQUEST_DISABLE_END_TIME = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
		CameraManager cameraManager = new CameraManager(context);
		if(!cameraManager.isAdminActive()){
            return;
        }

		PreciseTimer timer = new PreciseTimer(context);
        int request = intent.getIntExtra(EXTRA_REQUEST_CODE, REQUEST_DISABLE_START_TIME);
		int hour = intent.getIntExtra(EXTRA_TIME_HOUR, -1);
		int minute = intent.getIntExtra(EXTRA_TIME_MINUTE, -1);
		if(hour < 0 || minute < 0){
			return;
		}

		if(request == REQUEST_DISABLE_START_TIME){
			cameraManager.setDisabled(true);
			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true);
        }
        else if(request == REQUEST_DISABLE_END_TIME){
			cameraManager.setDisabled(false);
			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true);
        }
    }
}
