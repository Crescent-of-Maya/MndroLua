package moon3.utils;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {
    
    public static <T> T getField(Object instance, String n) {
        try {
            Field f = instance.getClass().getDeclaredField(n);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
    public static <T> T getStaticField(Class<?> clazz, String n) {
        try {
            Field f = clazz.getDeclaredField(n);
            f.setAccessible(true);
            return (T) f.get(null);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
    public static Class[] toClasses(Object[] args) {
        Class[] cls = new Class[args.length];
        
        int i = 0;
        for (Object o : args) {
            cls[i] = o.getClass();
            i++;
        }
        return cls;
    }
    
    public static <T> T invoke(Object instance, String n, Class<?>[] cls, Object... args) {
        try {
            Method m = instance.getClass().getDeclaredMethod(n, cls);
            m.setAccessible(true);
            return (T) m.invoke(instance, args);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
    public static <T> T invokeMethod(Object instance, String n, Class<?>[] cls, Object... args) {
        return invoke(instance,  n, cls, args);
    }
    
    public static <T> T invokeStatic(Class<?> clazz, String n, Class<?>[] cls, Object... args) {
        try {
            Method m = clazz.getDeclaredMethod(n, cls);
            m.setAccessible(true);
            return (T) m.invoke(null, args);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
    public static <T> T invokeStaticMethod(Class<?> clazz, String n, Class<?>[] cls, Object... args) {
        return invokeStatic(clazz,  n, cls, args);
    }
    
    public static <T> T newInstance(Class<T> clazz, Class<?>[] cls, Object... args) {
        try {
            Constructor m = clazz.getDeclaredConstructor(cls);
            m.setAccessible(true);
            return (T) m.newInstance(args);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
    public static <T> T allocateInstance(Class<T> clazz) {
        return (T) Unsafe.allocateInstance(clazz);
    }
    
}
