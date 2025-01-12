package moon3.app;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;
import android.widget.TextView;

// https://juejin.cn/post/6979461869581008903
public class MaterialProgressDialog extends MaterialDialog {
    
    public MaterialProgressDialog(Context context) {
        this(context, 0);
    }

    public MaterialProgressDialog(Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
        
        setView(getLayoutInflater().inflate(moon3.R.layout.progress_dialog_layout, null));
    }
    
    @Override
    public void setMessage(CharSequence message) {
        TextView mText = (TextView) getView().findViewById(moon3.R.id.message);
        mText.setText(message);
    }
    
    public void setProgressDrawable(Drawable d) {
        ProgressBar mProgress = (ProgressBar) getView().findViewById(moon3.R.id.progress);
        mProgress.setProgressDrawable(d);
    }

}
