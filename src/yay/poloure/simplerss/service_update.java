package yay.poloure.simplerss;

import android.app.IntentService;
import android.content.Intent;

public class service_update extends IntentService
{
	int group;
	
	public service_update()
	{
		super("service_update");
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(pm.PARTIAL_WAKE_LOCK, "SIMPLERSS");
		wakelock.acquire();
		
		Intent intent = getIntent();
		group = Integer.parseInt(intent.getStringExtra("GROUP_NUMBER"));
		
		wakelock.release();
	}
}
