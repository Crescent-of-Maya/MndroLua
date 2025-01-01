package moon3.widget;
import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * @Author 满月叶
 * @Date 2024/02/06 11:18
 */

public class Switch extends /*SwitchMaterial*/ MaterialSwitch {

    public Switch(Context c) {
        super(c);
    }

    public Switch(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public Switch(Context c, AttributeSet attrs, int def) {
        super(c, attrs, def);
    }

}
