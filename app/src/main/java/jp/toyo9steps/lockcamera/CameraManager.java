package jp.toyo9steps.lockcamera;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * カメラの無効化と合わせて諸々の処理をするクラス
 */

public class CameraManager{

	private static final int NOTIFICATION_ID = 1;/* 通知の識別子 */

	private Context mContext;
	private DevicePolicyManager mPolicyManager;
	private ComponentName mAdminReceiver;

	public CameraManager(Context context){
		mContext = context;
		mPolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mAdminReceiver = new ComponentName(context, MyDeviceAdminReceiver.class);
	}

	public boolean isAdminActive(){
		return mPolicyManager.isAdminActive(mAdminReceiver);
	}

	public ComponentName getAdminComponent(){
		return mAdminReceiver;
	}

	public boolean getDisabled(){
		return mPolicyManager.getCameraDisabled(mAdminReceiver);
	}

	public void setDisabled(boolean disabled, boolean showNotif){
		/* 権限をチェックしてからカメラを無効化する */
		if(!isAdminActive()){
			return;
		}
		mPolicyManager.setCameraDisabled(mAdminReceiver, disabled);
		if(disabled && showNotif){
			showNotification(mContext);
		}
		else{
			hideNotification(mContext);
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
		builder.setSmallIcon(R.mipmap.notification);
		Notification notification = builder.getNotification();
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, notification);
	}

	private void hideNotification(Context context) {
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancelAll();
	}

	public void updateNotificationSetting(boolean showNotif){
		if(!isAdminActive()){
			return;
		}
		boolean disabled = mPolicyManager.getCameraDisabled(mAdminReceiver);
		if(disabled && showNotif){
			showNotification(mContext);
		}
		else{
			hideNotification(mContext);
		}
	}
}
