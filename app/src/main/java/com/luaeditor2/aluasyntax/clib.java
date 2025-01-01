package com.luaeditor2.aluasyntax;

import com.luaeditor2.aluasyntax.structs.*;

public class clib {
    protected clib() {
    }

    //memcpy
    protected static void memcpy(byte[] a, byte[] b, int l) {
        System.arraycopy(b, 0, a, 0, l);
    }

    protected static void memcpy(byte[] a, int posa, byte[] b, int l) {
        System.arraycopy(b, 0, a, posa, l);
    }

    protected static void memcpy(byte[] a, int posa, byte[] b, int posb, int l) {
        System.arraycopy(b, posb, a, posa, l);
    }

    protected static void memcpy(byte[] a, String b) {
        byte[] bBytes = b.getBytes();
        System.arraycopy(bBytes, 0, a, 0, bBytes.length);
    }

    protected static void memcpy(byte[] a, int posa, String b) {
        byte[] bBytes = b.getBytes();
        System.arraycopy(bBytes, 0, a, posa, bBytes.length);
    }
    protected static void memcpy(Instruction[] a,int posa, Instruction[] b, int l) {
        System.arraycopy(b, 0, a, posa, l);
    }

    //strlen
    protected static int strlen(byte[] source) {
        int i = 0;
        for (byte b : source) {
            if (b == 0) {
                break;
            }
            i++;
        }
        return i;
    }

    protected static String cstring(byte[] source) {
        int len = strlen(source);
        byte[] dest = new byte[len];
        memcpy(dest, source, len);
        return new String(dest);
    }

    //strchr
    protected static int strchr(byte[] source, byte c) {
        for (int i = 0; i < source.length; i++) {
            if (source[i] == c) {
                return i;
            }
        }
        return -1;
    }

    //strtod
    protected static double strtod(byte[] source, int[] end) {
        //解析source到double并且将end[0]设置为解析结束的位置
        end[0] = strlen(source);
        String sourceStr = cstring(source).trim();
        return Double.parseDouble(sourceStr);
    }

}
