package com.luaeditor2.aluasyntax;

import com.luaeditor2.aluasyntax.structs.*;

public class LuaError extends Exception {
    public LuaError(String message, int ecode) {
        super(message);
    }

    public LuaError(lua_State L, String message) {
        super(message);
    }
}
