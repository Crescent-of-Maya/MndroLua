package moon3.app;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.widget.ImageView;
import android.widget.TextView;

// 感谢:
// https://github.com/RikkaApps/Shizuku/blob/master/manager/src/main/java/moe/shizuku/manager/home/HomeActivity.kt
// https://github.com/RikkaApps/Shizuku/blob/master/manager/src/main/res/layout/about_dialog.xml
public class AboutDialog extends MaterialDialog {
    
    public AboutDialog(Context context) {
        this(context, 0);
    }

    public AboutDialog(Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
        
        setView(getLayoutInflater().inflate(moon3.R.layout.about_dialog_layout, null));
    }
    
    public AboutDialog setAppName(CharSequence appName) {
        TextView v = getView().findViewById(moon3.R.id.app_name);
        v.setText(appName);
        return this;
    }
    
    public AboutDialog setAppIcon(Drawable icon) {
        ImageView v = getView().findViewById(android.R.id.icon);
        v.setImageDrawable(icon);
        return this;
    }
    
    // Api:23
    public AboutDialog setAppIcon(Icon icon) {
        ImageView v = getView().findViewById(android.R.id.icon);
        v.setImageIcon(icon);
        return this;
    }
    
    public AboutDialog setAppIcon(Bitmap icon) {
        ImageView v = getView().findViewById(android.R.id.icon);
        v.setImageBitmap(icon);
        return this;
    }
    
    public AboutDialog setVersionName(CharSequence versionName) {
        TextView v = getView().findViewById(moon3.R.id.version_name);
        v.setText(versionName);
        return this;
    }
    
    public AboutDialog setMoreInfo(CharSequence moreInfo) {
        TextView v = getMoreInfoView();
        v.setText(moreInfo);
        return this;
    }
    
    public TextView getMoreInfoView() {
        return getView().findViewById(moon3.R.id.more_info);
    }
    
    public AboutDialog updateInfo(Context c) {
        return updateInfo(c, false);
    }
    
    public AboutDialog updateInfo(Context c, boolean includeVersionCode) {
        try {
            PackageManager pm = c.getPackageManager();
            PackageInfo info = pm.getPackageInfo(c.getPackageName(), 0);
 
            setAppIcon(info.applicationInfo.loadIcon(pm));
            setAppName(info.applicationInfo.loadLabel(pm));
            setVersionName(info.versionName + (includeVersionCode ? " (" + info.versionCode + ")" : ""));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    
}
