package moon3.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

public class FastToast {
	
	// Normal Toast
	
	public static Toast shortToast(Context c,CharSequence text) {
		return Toast.makeText(c,text,Toast.LENGTH_SHORT);
	}

	public static Toast longToast(Context c,CharSequence text) {
		return Toast.makeText(c,text,Toast.LENGTH_LONG);
	}
	
	public static Toast shortToast(View c,CharSequence text) {
		return Toast.makeText(c.getContext(),text,Toast.LENGTH_SHORT);
	}

	public static Toast longToast(View c,CharSequence text) {
		return Toast.makeText(c.getContext(),text,Toast.LENGTH_LONG);
	}
	
	// Toast for Service
	
	public static void showShortToast(Service s,CharSequence text) {
		new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(s.getApplicationContext(),text,Toast.LENGTH_LONG).show());
	}

	public static void showLongToast(Service s,CharSequence text) {
		new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(s.getApplicationContext(),text,Toast.LENGTH_LONG).show());
	}
	
	// Special Toast - Snackbar
	
	public static Snackbar longSnack(View v,CharSequence text) {
		return Snackbar.make(v,text,Snackbar.LENGTH_LONG);
	}
	
	public static Snackbar shortSnack(View v,CharSequence text) {
		return Snackbar.make(v,text,Snackbar.LENGTH_SHORT);
	}
	
	public static Snackbar longSnack(Activity v,CharSequence text) {
		return Snackbar.make(v.findViewById(android.R.id.content),text,Snackbar.LENGTH_LONG);
	}
	
	public static Snackbar shortSnack(Activity v,CharSequence text) {
		return Snackbar.make(v.findViewById(android.R.id.content),text,Snackbar.LENGTH_SHORT);
	}
	
}
