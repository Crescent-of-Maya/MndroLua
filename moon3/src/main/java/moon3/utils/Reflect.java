package moon3.utils;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflect {
    
    private Class c;
    private Object o;
    
    private Reflect(Object o, Class c){
        this.o = o;
        this.c = c;
    }
    
    public static Reflect from(Class c) {
        return new Reflect(null, c);
    }
    
    public static Reflect from(Object o) {
        return new Reflect(o, o.getClass());
    }
    
    public <T> T field(String n) {
        return (T) field(n, o);
    }
    
    public <T> T field(String n, Object instance) {
        try {
            Field f = c.getDeclaredField(n);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
    public <T> T call(String n, Object... args) {
        return (T) call(n, o, args);
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
    
    public <T> T call(String n, Class[] cls, Object... args) {
        return (T) call(n, o, cls, args);
    }
    
    public <T> T call(String n, Object instance, Object... args) {
        return call(n, instance, toClasses(args), args);
    }
    
    public <T> T call(String n, Object instance, Class[] cls, Object... args) {
        try {
            Method m = c.getDeclaredMethod(n, cls);
            m.setAccessible(true);
            return (T) m.invoke(instance, args);
        } catch(Exception e) {
            Log.e("Reflect","", e);
            return null;
        }
    }
    
}
