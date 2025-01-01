package moon3.utils;

import android.content.Context;
import java.io.File;

public class FileUtils {
	
	public static File openPrivateFile(Context c,String path) {
		return new File(c.getDataDir(),path);
	}
	
	public static File openPublicFile(Context c,String path) {
		return new File(c.getExternalFilesDir(null),path);
	}
	
}