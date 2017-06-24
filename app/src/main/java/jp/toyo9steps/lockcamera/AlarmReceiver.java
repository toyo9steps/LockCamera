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
    private static final int NOTIFICATION_ID = 1;/* 通知の識別子 */

    @Override
    public void onReceive(Context context, Intent intent) {
        DevicePolicyManager policyManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminName = new ComponentName(context, MyDeviceAdminReceiver.class);
        if(!policyManger.isAdminActive(adminName)){
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
            policyManger.setCameraDisabled(adminName, true);
            showNotification(context);
			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true);
        }
        else if(request == REQUEST_DISABLE_END_TIME){
            policyManger.setCameraDisabled(adminName, false);
            hideNotification(context);
			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true);
        }
    }

    private void showNotification(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setTicker("カメラ無効中です");
		builder.setContentText("カメラ無効中です");
		builder.setContentIntent(pending);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(R.mipmap.ic_launcher);
		Notification notification = builder.getNotification();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        manager.notify(NOTIFICATION_ID, notification);
    }

    private void hideNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }
}
