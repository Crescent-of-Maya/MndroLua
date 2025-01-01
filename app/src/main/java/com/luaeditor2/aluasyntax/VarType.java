package com.luaeditor2.aluasyntax;
import static com.luaeditor2.aluasyntax.lua.*;
public class VarType {
    public static final String[] TYPE_NAMES = {
            "nil",
            "boolean",
            "lightuserdata",
            "number",
            "string",
            "table",
            "function",
            "userdata",
            "thread",
            "value",
    };
    public static final VarType TNUMBER = new VarType(LUA_TNUMBER);
    public static final VarType TFUNCTION = new VarType(LUA_TFUNCTION);
    public static final VarType TSTRING = new VarType(LUA_TSTRING);
    public static final VarType TTABLE = new VarType(LUA_TTABLE);
    public static final VarType TBOOLEAN = new VarType(LUA_TBOOLEAN);
    public static final VarType TNIL = new VarType(LUA_TNIL);
    public int type = LUA_TNONE;
    public String typename = "";

    public VarType(int t, String n) {
        type = t;
        typename = n;
    }

    public VarType(int t) {
        if (t < TYPE_NAMES.length&&t>0)
            typename = TYPE_NAMES[t];
        type = t;
    }

    public VarType(String n) {
        switch (n) {
            case "string":
                type = LUA_TSTRING;
                break;
            case "number":
                type = LUA_TNUMBER;
                break;
            case "table":
                type = LUA_TTABLE;
                break;
            case "function":
                type = LUA_TFUNCTION;
                break;
            case "boolean":
                type = LUA_TBOOLEAN;
                break;
        }
        typename = n;
    }
}
