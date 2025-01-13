package moon3.widget;
import android.content.Context;
import android.util.AttributeSet;

public class Toolbar extends androidx.appcompat.widget.Toolbar {
    protected Context mContext;
    
    public Toolbar(Context c) {
        super(c);
        mContext = c;
        _moon3InitToolbar();
    }

    public Toolbar(Context c, AttributeSet attrs) {
        super(c, attrs);
        mContext = c;
        _moon3InitToolbar();
    }

    public Toolbar(Context c, AttributeSet attrs, int def) {
        super(c, attrs, def);
        mContext = c;
        _moon3InitToolbar();
    }
    
    protected void _moon3InitToolbar() {
        // http://aospxref.com/android-14.0.0_r2/xref/frameworks/base/core/res/res/values/styles.xml#1303
        setTitleTextAppearance(mContext, moon3.R.style.Moon3_TextAppearance_ToolbarTitle);
        setSubtitleTextAppearance(mContext, moon3.R.style.Moon3_TextAppearance_ToolbarSubTitle);
    }
    
}
