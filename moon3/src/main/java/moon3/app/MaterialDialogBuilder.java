package moon3.app;
import android.content.Context;
import android.widget.ListView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import moon3.utils.Reflect;

public class MaterialDialogBuilder extends MaterialAlertDialogBuilder {
    
    public MaterialDialogBuilder(Context c) {
        super(c);
    }
    
    public MaterialDialogBuilder(Context c, int style) {
        super(c, style);
    }
    
}
