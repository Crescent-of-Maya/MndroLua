package moon3.widget;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;

// 别名 & Wapper
// 还有很多方法没有覆盖到(恼)
public class MaterialEditText extends TextInputLayout {
	private TextInputEditText et;
    
	public MaterialEditText(Context c) {
		super(c);
        moon3InitEditText();
	}

	public MaterialEditText(Context c, AttributeSet attrs) {
		super(c, attrs);
        moon3InitEditText();
	}

	public MaterialEditText(Context c, AttributeSet attrs, int def) {
		super(c, attrs, def);
        moon3InitEditText();
	}
    
    protected void moon3InitEditText() {
        et = new TextInputEditText(getContext());
        addView(et,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
    }
    
    public TextInputEditText getInnerEditText() {
        return et;
    }
    
    // Wapper is very zhemoren
    // But I need to shipei them
    // : (
    
    public Editable getText() {
        return et.getText();
    }
    
    public void setText(CharSequence text) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> et.setText(text), 10);
    }
    
    /*
    // 1.8.0, Now I needn't write it
    // because you can get StackOverflowError by it :)
    public CharSequence getHint() {
        return et.getHint();
    }
    */

    /*
    public void setHint(CharSequence text) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> et.setHint(text), 100);
    }
    */
    
    
    public void setTextColor(int c) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> et.setTextColor(c), 10);
    }
    
    public ColorStateList getTextColor() {
        return et.getTextColors();
    }
    
    public void setHintTextColor(int c) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> et.setHintTextColor(c), 10);
    }

    public ColorStateList getHintTextColor() {
        return et.getHintTextColors();
    }
	
    public void setSingleLine(boolean b) {
        et.setSingleLine(b);
    }
    
    public void addTextChangedListener(TextWatcher wc) {
        et.addTextChangedListener(wc);
    }
    
}
