package com.luaeditor2.aluasyntax;

public class llimits {
    protected llimits() {
    }

    //    #define LUAI_MAXSHORTLEN	40
    protected static final int LUAI_MAXSHORTLEN = 40;
    //    #define LUA_MINBUFFER	32
    protected static final int LUA_MINBUFFER = 32;
    //    #define LUAI_MAXCCALLS		200
    protected static final int LUAI_MAXCCALLS = 200;
//    #define MAX_INT		INT_MAX  /* maximum value of an int */
    protected static final int MAX_INT = 0x7fffffff;
//    #define luai_numisnan(a)        (!((a)==(a)))
    protected static boolean luai_numisnan(double a) {
        return a != a;
    }
}
