package moon3.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.view.ViewCompat;
import com.google.android.material.R;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.InsetDialogOnTouchListener;
import com.google.android.material.dialog.MaterialDialogs;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * Material Design 3 Dialog
 * 
 * @Author 满月叶
 * @Used AndroLua+(LuaDialog.java) & MDC(MaterialAlertDialogBuilder.java)
 * @Date 2024.12.14
 */

// 这是一个究极缝合怪(x
// 虽然不知道符不符合规范
// 但用来顶替LuaDialog那是吊打的()()()
public class MaterialDialog extends AlertDialog implements DialogInterface.OnClickListener {
    
    // MaterialAlertDialogBuilder.java
    private static final int DEF_STYLE_ATTR = R.attr.alertDialogStyle;
    private static final int DEF_STYLE_RES = R.style.MaterialAlertDialog_MaterialComponents;
    private static final int MATERIAL_ALERT_DIALOG_THEME_OVERLAY = R.attr.materialAlertDialogTheme;
    private Drawable background;
    private Rect backgroundInsets;
    
    // LuaDialog.java
    private Context mContext;
    private ListView mListView;
    private String mMessage;
    private String mTitle;
    private View mView;
    
    // 修改
    private ArrayList<DialogInterface.OnClickListener> mOnClickListeners = new ArrayList<DialogInterface.OnClickListener>();

    // MaterialAlertDialogBuilder.java
    private static int getMaterialAlertDialogThemeOverlay(Context context) {
        TypedValue materialAlertDialogThemeOverlay = MaterialAttributes.resolve(context, MATERIAL_ALERT_DIALOG_THEME_OVERLAY);
        if (materialAlertDialogThemeOverlay == null) {
            return 0;
        }
        return materialAlertDialogThemeOverlay.data;
    }

    // MaterialAlertDialogBuilder.java
    private static Context createMaterialAlertDialogThemedContext(Context context) {
        int themeOverlayId = getMaterialAlertDialogThemeOverlay(context);
        Context themedContext = MaterialThemeOverlay.wrap(context, null, DEF_STYLE_ATTR, DEF_STYLE_RES);
        if (themeOverlayId == 0) {
            return themedContext;
        }
        return new ContextThemeWrapper(themedContext, themeOverlayId);
    }

    // MaterialAlertDialogBuilder.java
    private static int getOverridingThemeResId(Context context, int overrideThemeResId) {
        if (overrideThemeResId == 0) {
            return getMaterialAlertDialogThemeOverlay(context);
        }
        return overrideThemeResId;
    }

    public MaterialDialog(Context context) {
        this(context, 0);
    }

    // MaterialAlertDialogBuilder.java
    // LuaDialog.java
    public MaterialDialog(Context context, int overrideThemeResId) {
        super(createMaterialAlertDialogThemedContext(context), getOverridingThemeResId(context, overrideThemeResId));
        
        mContext = getContext();
        mListView = new ListView(mContext);
        
        Theme theme = mContext.getTheme();
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(mContext,null, DEF_STYLE_ATTR, DEF_STYLE_RES);
        this.backgroundInsets = MaterialDialogs.getDialogBackgroundInsets(mContext, DEF_STYLE_ATTR, DEF_STYLE_RES);
        int surfaceColor = MaterialColors.getColor(mContext, R.attr.colorSurface, getClass().getCanonicalName());
        TypedArray a = mContext.obtainStyledAttributes(null, R.styleable.MaterialAlertDialog, DEF_STYLE_ATTR, DEF_STYLE_RES);
        int backgroundColor = a.getColor(R.styleable.MaterialAlertDialog_backgroundTint, surfaceColor);
        a.recycle();
        materialShapeDrawable = new MaterialShapeDrawable(mContext, null, DEF_STYLE_ATTR, DEF_STYLE_RES);
        materialShapeDrawable.initializeElevationOverlay(mContext);
        materialShapeDrawable.setFillColor(ColorStateList.valueOf(backgroundColor));
        if (VERSION.SDK_INT >= 28) {
            TypedValue typedValue = new TypedValue();
            theme.resolveAttribute(16844145, typedValue, true);
            float dimension = typedValue.getDimension(getContext().getResources().getDisplayMetrics());
            if (typedValue.type == 5 && dimension >= 0.0f) {
                materialShapeDrawable.setCornerSize(dimension);
            }
        }
        this.background = materialShapeDrawable;
    }
    
    // MaterialAlertDialogBuilder.java
    // 修改
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        // TODO: Implement this method
        
        Window window = getWindow();
        View decorView = window.getDecorView();
        Drawable insetDrawable = this.background;
        if (insetDrawable instanceof MaterialShapeDrawable) {
            ((MaterialShapeDrawable) insetDrawable).setElevation(ViewCompat.getElevation(decorView));
        }
        window.setBackgroundDrawable(MaterialDialogs.insetDrawable(this.background, this.backgroundInsets));
        decorView.setOnTouchListener(new InsetDialogOnTouchListener(this, this.backgroundInsets));
    }

    // LuaDialog.java
    
    public void setButton(CharSequence text) {
        setOkButton(text);
    }

    public void setButton1(CharSequence text) {
        setButton(DialogInterface.BUTTON_POSITIVE, text, this);
    }
    public void setButton2(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEGATIVE, text, this);
    }
    public void setButton3(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEUTRAL, text, this);
    }
    public void setPosButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_POSITIVE, text, this);
    }
    public void setNegButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEGATIVE, text, this);
    }
    public void setNeuButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEUTRAL, text, this);
    }

    public void setOkButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_POSITIVE, text, this);
    }

    public void setCancelButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEGATIVE, text, this);
    }

    public void setOnClickListener(DialogInterface.OnClickListener listener) {
        mOnClickListeners.clear();
        mOnClickListeners.add(listener);
    }
    
    public void addOnClickListener(DialogInterface.OnClickListener listener) {
        mOnClickListeners.add(listener);
    }
    
    public void clearOnClickListeners() {
        mOnClickListeners.clear();
    }
    
    public void removeOnClickListeners() {
        mOnClickListeners.clear();
    }

    public void setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        setButton(DialogInterface.BUTTON_POSITIVE, text, listener);
    }

    public void setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        setButton(DialogInterface.BUTTON_NEGATIVE, text, listener);
    }

    public void setNeutralButton(CharSequence text, DialogInterface.OnClickListener listener) {
        setButton(DialogInterface.BUTTON_NEUTRAL, text, listener);
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public void setTitle(CharSequence title) {
        // TODO: Implement this method
        mTitle = title.toString();
        super.setTitle(title);
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public void setMessage(CharSequence message) {
        // TODO: Implement this method
        mMessage = message.toString();
        super.setMessage(message);
    }

    @Override
    public void setIcon(Drawable icon) {
        // TODO: Implement this method
        super.setIcon(icon);
    }

    public View getView() {
        return mView;
    }

    // 为了方便 直接做了统一化
    
    @Override
    public void setView(View view) {
        // TODO: Implement this method
        mView = view;
        if (isShowing())
            super.setContentView(view);
        else
            super.setView(view);
    }
    
    @Override
    public void setContentView(View view) {
        // TODO: Implement this method
        mView = view;
        if (isShowing())
            super.setContentView(view);
        else
            super.setView(view);
    }

    public void setItems(String[] items) {
        ArrayList<String> alist = new ArrayList<String>(Arrays.asList(items));
        ArrayAdapter adp = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, alist);
        setAdapter(adp);
        mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }

    public void setAdapter(ListAdapter adp) {
        if (!mListView.equals(mView))
            setView(mListView);
        mListView.setAdapter(adp);
    }

    public void setSingleChoiceItems(CharSequence[] items){
        setSingleChoiceItems(items, 0);
    }

    public void setSingleChoiceItems(CharSequence[] items, int checkedItem){
        ArrayList<CharSequence> alist = new ArrayList<CharSequence>(Arrays.asList(items));
        ArrayAdapter adp = new ArrayAdapter<CharSequence>(mContext, android.R.layout.simple_list_item_single_choice, alist);
        setAdapter(adp);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setItemChecked(checkedItem,true);
    }

    public void setMultiChoiceItems(CharSequence[] items){
        setMultiChoiceItems(items, new int[0]);
    }

    public void setMultiChoiceItems(CharSequence[] items, int[] checkedItems){
        ArrayList<CharSequence> alist = new ArrayList<CharSequence>(Arrays.asList(items));
        ArrayAdapter adp = new ArrayAdapter<CharSequence>(mContext, android.R.layout.simple_list_item_multiple_choice, alist);
        setAdapter(adp);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        for (int i:checkedItems)
            mListView.setItemChecked(checkedItems[i],true);
    }

    public ListView getListView() {
        return mListView;
    }

    public void setOnItemClickListener(final AdapterView.OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public void setOnItemLongClickListener(final AdapterView.OnItemLongClickListener listener) {
        mListView.setOnItemLongClickListener(listener);
    }

    public void setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
        mListView.setOnItemSelectedListener(listener);
    }

    @Override
    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        // TODO: Implement this method
        super.setOnCancelListener(listener);
    }

    @Override
    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        // TODO: Implement this method
        super.setOnDismissListener(listener);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }


    @Override
    public boolean isShowing() {
        return super.isShowing();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mOnClickListeners.forEach((mOnClickListener) -> {
            mOnClickListener.onClick(this, which);
        });
    }

}
