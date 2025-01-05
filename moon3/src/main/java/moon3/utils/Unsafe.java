package moon3.utils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Unsafe {
    private static Object unsafe;
    
    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = f.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static <T> T invoke(String name,Class[] argclazz, Object... args) {
        try {
            Method m = unsafe.getClass().getDeclaredMethod(name, argclazz);
            m.setAccessible(true);
            return (T) m.invoke(unsafe, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static <T> T allocateInstance(Class<T> clazz) {
        return (T) invoke("allocateInstance", new Class[] { Class.class }, clazz);
    }
    
    /*
    public static int arrayBaseOffset(Class<?> clazz) {
        return invoke("arrayBaseOffset", new Class[] { Class.class }, clazz);
    }
    
    public static int arrayIndexScale(Class<?> clazz) {
        return invoke("arrayIndexScale", new Class[] { Class.class }, clazz);
    }
    */
    
}
