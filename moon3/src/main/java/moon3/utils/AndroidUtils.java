package moon3.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

public class AndroidUtils {

	public static boolean checkSdkVersion(int min) {
		return Build.VERSION.SDK_INT >= min;
	}

	public static boolean checkSelfPermission(Context c, String permission) {
		if (checkSdkVersion(23))
			return c.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
		return true;
	}
    
    public static boolean checkStoragePermission(Context c) {
        if (checkSdkVersion(30))
            return Environment.isExternalStorageManager();
        return checkSelfPermission(c, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    
    public static void requestStoragePermission(Context c) {
        if (checkSdkVersion(30))
            c.startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse("package:" + c.getPackageName())));
    }
    
    public static boolean isNightMode(Context c) {
        return c.getResources().getBoolean(moon3.R.bool.isNightMode);
    }

}
