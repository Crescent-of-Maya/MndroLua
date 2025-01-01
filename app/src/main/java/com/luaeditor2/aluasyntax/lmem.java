package com.luaeditor2.aluasyntax;
import com.luaeditor2.aluasyntax.structs.*;
public class lmem {
    protected lmem() {
    }

    protected static byte[] luaM_realloc(lua_State L, byte[] buffer, int buffsize, int size) {
        byte[] newBuffer = new byte[size];
        if (buffer == null) {
            return newBuffer;
        }
        System.arraycopy(buffer, 0, newBuffer, 0, Math.min(buffsize, size));
        return newBuffer;
    }

    protected static byte[] luaM_reallocvchar(lua_State L, byte[] buffer, int buffsize, int size) {
        return luaM_realloc(L, buffer, buffsize, size);
    }

    protected static TValue[] luaM_realloc(lua_State L, TValue[] buffer, int buffsize, int size) {
        TValue[] newBuffer = new TValue[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new TValue();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new TValue();
        }
        return newBuffer;
    }

    protected static Instruction[] luaM_realloc(lua_State L, Instruction[] buffer, int buffsize, int size) {
        Instruction[] newBuffer = new Instruction[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new Instruction();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new Instruction();
        }
        return newBuffer;
    }

    protected static Proto[] luaM_realloc(lua_State L, Proto[] buffer, int buffsize, int size) {
        Proto[] newBuffer = new Proto[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new Proto();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new Proto();
        }
        return newBuffer;
    }

    protected static int[] luaM_realloc(lua_State L, int[] buffer, int buffsize, int size) {
        int[] newBuffer = new int[size];
        if (buffer == null) {
            return newBuffer;
        }
        System.arraycopy(buffer, 0, newBuffer, 0, Math.min(buffsize, size));
        return newBuffer;
    }

    protected static LocVar[] luaM_realloc(lua_State L, LocVar[] buffer, int buffsize, int size) {
        LocVar[] newBuffer = new LocVar[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new LocVar();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new LocVar();
        }
        return newBuffer;
    }

    protected static Upvaldesc[] luaM_realloc(lua_State L, Upvaldesc[] buffer, int buffsize, int size) {
        Upvaldesc[] newBuffer = new Upvaldesc[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new Upvaldesc();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new Upvaldesc();
        }
        return newBuffer;
    }

    protected static Vardesc[] luaM_realloc(lua_State L, Vardesc[] buffer, int buffsize, int size) {
        Vardesc[] newBuffer = new Vardesc[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new Vardesc();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new Vardesc();
        }
        return newBuffer;
    }

    protected static Labeldesc[] luaM_realloc(lua_State L, Labeldesc[] buffer, int buffsize, int size) {
        Labeldesc[] newBuffer = new Labeldesc[size];
        if (buffer == null) {
            for (int i = 0; i < size; i++) {
                newBuffer[i] = new Labeldesc();
            }
            return newBuffer;
        }
        int min = Math.min(buffsize, size);
        System.arraycopy(buffer, 0, newBuffer, 0, min);
        for (int i = min; i < size; i++) {
            newBuffer[i] = new Labeldesc();
        }
        return newBuffer;
    }


}
