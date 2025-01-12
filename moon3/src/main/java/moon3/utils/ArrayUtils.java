package moon3.utils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayUtils {
    
    public static <T> List<T> toList(T[] array) {
        return Arrays.asList(array);
    }
    
    public static <T> Set<T> toSet(T[] array) {
        HashSet<T> set = new HashSet<T>();
        for (T o : array) {
            set.add(o);
        }
        return set;
    }
    
}
