package com.luaeditor2.aluasyntax;

public class luaconf {
    protected luaconf() {
    }

    //    #define LUA_IDSIZE    60
    protected static final int LUA_IDSIZE = 60;
    protected static final double LUA_MININTEGER = Double.MIN_VALUE;

    //    #define lua_numbertointeger(n, p) \
//            ((n) >= (LUA_NUMBER)(LUA_MININTEGER) && \
//            (n) < -(LUA_NUMBER)(LUA_MININTEGER) && \
//            (*(p) = (LUA_INTEGER)(n), 1))
    protected static int lua_numbertointeger(double n, long[] p) {
        p[0] = (long) n;
        return 1;
    }
//    #define lua_getlocaledecpoint()        '.'
    protected static byte lua_getlocaledecpoint() {
        return '.';
    }
}

