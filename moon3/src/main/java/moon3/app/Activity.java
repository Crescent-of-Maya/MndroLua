package moon3.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import moon3.utils.AndroidUtils;
import moon3.utils.FastToast;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.color.DynamicColors;
import moon3.utils.ReflectUtils;

/**
 * @Author 满月叶
 * @Date 2024/02/06 09:34
 */
public class Activity extends androidx.appcompat.app.AppCompatActivity { //android.app.Activity {
	protected Toolbar toolbar;
	protected CoordinatorLayout rootContentViewHandler;
    protected LinearLayout rootContentView;
	protected ArrayList<OnDestroyListener> onDestroyListeners = new ArrayList<OnDestroyListener>();

	public interface OnDestroyListener {
		public void onDestroy();
	}

	public void setOnDestroyListener(OnDestroyListener l) {
        onDestroyListeners.clear();
		onDestroyListeners.add(l);
	}
    
    public void addOnDestroyListener(OnDestroyListener l) {
		onDestroyListeners.add(l);
	}

	public void setStatusBarColor(int c) {
		getWindow().setStatusBarColor(c);
	}

	public void setNavigationBarColor(int c) {
		getWindow().setNavigationBarColor(c);
	}
    
    public void setBackgroundColor(int c) {
        getWindow().getDecorView().setBackgroundColor(c);
    }

	public void startForegroundServiceSafely(Intent i) {
		if (AndroidUtils.checkSdkVersion(23))
			requestPermissions(new String[] { Manifest.permission.FOREGROUND_SERVICE }, 0);

		if (AndroidUtils.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE))
			if (AndroidUtils.checkSdkVersion(26))
				startForegroundService(i);
			else
				startService(i);
		/*else
			FastToast.shortSnack(this, "当前没有权限启动前台服务!").show();*/
	}
    
    
    @Override
    protected void onDestroy() {
        onDestroyListeners.forEach((l) -> l.onDestroy());
        super.onDestroy();
    }
    
    public void useDynamicColors() {
        // Android 12+ 动态配色
        DynamicColors.applyToActivityIfAvailable(this);
    }
    
    public void useDynamicColors(int c) {
        DynamicColors.applyToActivityIfAvailable(this, new DynamicColorsOptions.Builder().setContentBasedSource(c).build());
    }
    
    public void useDynamicColors(DynamicColorsOptions dco) {
        DynamicColors.applyToActivityIfAvailable(this, dco);
    }
    
    /*
    public void applyColorToBackground(int colorAttr) {
        int c = MaterialColors.getColor(new MaterialButton(this), colorAttr);
        getWindow().getDecorView().setBackgroundColor(c);
        setNavigationBarColor(c);
        setStatusBarColor(c);
    }
    */

	@Override
	protected void onCreate(Bundle savedInstance) {
        setTheme(moon3.R.style.Moon3);
        
		super.onCreate(savedInstance);
        
        if (!AndroidUtils.isNightMode(this) && AndroidUtils.checkSdkVersion(23))
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

		// 自动添加 Toolbar
        rootContentView = new LinearLayout(this);
        
        LinearLayout root = rootContentView;

		// 为后续 setContentView 做铺垫
		rootContentViewHandler = new CoordinatorLayout(this);
        
        // I forget to set layout_height
        // Holy shit
        // : )
        root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));

		root.setOrientation(LinearLayout.VERTICAL);

		// Give up
        /*
		if (toolbar == null)
			toolbar = new Toolbar(this);
        
        toolbar.setPopupTheme(com.google.android.material.R.style.Widget_Material3_PopupMenu_Overflow);
        
		super.setActionBar(toolbar);
        */
        if (toolbar == null)
			toolbar = new Toolbar(this);
        
        super.setSupportActionBar(toolbar);
        
		root.addView(toolbar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
		root.addView(rootContentViewHandler, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));

		super.setContentView(root);
	}
    
    public boolean isNightMode() {
        return AndroidUtils.isNightMode(this);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        try {
            ReflectUtils.invokeMethod(menu, "setOptionalIconsVisible", new Class[] { boolean.class }, new Object[] { true });
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return super.onMenuOpened(featureId, menu);
    }
    
	@Override
	public void setSupportActionBar(Toolbar newToolbar) {
		// throw new UnsupportedOperationException("You needn't use a new Toolbar, please use getToolbar().");
        if (newToolbar == toolbar) toolbar.setVisibility(View.VISIBLE);
        else {
            toolbar.setVisibility(View.GONE);
            //super.setActionBar(newToolbar);
            super.setSupportActionBar(toolbar);
        }
	}
    
    public Toolbar getFirstToolbar() {
		return toolbar;
	}
    
    public CoordinatorLayout getRootContentViewHandler() {
        return rootContentViewHandler;
    }

	// 重写设置视图方法 用以保持 Toolbar

	@Override
	public void setContentView(int resId) {
		handleSetContentView(getLayoutInflater().inflate(resId, null, false));
	}

	@Override
	public void setContentView(View v) {
		handleSetContentView(v);
	}

	@Override
	public void setContentView(View v, ViewGroup.LayoutParams lp) {
		handleSetContentView(v, lp);
	}

	protected void handleSetContentView(View v) {
		rootContentViewHandler.removeAllViews();
		rootContentViewHandler.addView(v, new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
				CoordinatorLayout.LayoutParams.MATCH_PARENT));
	}

	protected void handleSetContentView(View v, ViewGroup.LayoutParams lp) {
		rootContentViewHandler.removeAllViews();
		rootContentViewHandler.addView(v, lp);
	}

}
