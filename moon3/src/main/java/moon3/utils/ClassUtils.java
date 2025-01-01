package moon3.utils;

public class ClassUtils {
	
	public static <T> T as(Object o) {
		return (T) o;
	}
	
	public static String getCallingClassName() {
		try {
			return new Exception().getStackTrace()[2].getClassName();
		} catch (Exception e) {
			return null;
		}
	}
	
}