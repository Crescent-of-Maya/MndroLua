package com.luaeditor2.aluasyntax;

public class lua {
    public lua() {
    }

    //#define LUA_MULTRET	(-1)
    public static final int LUA_MULTRET = -1;
    //    #define LUA_TNONE		(-1)
//    #define LUA_TNIL		0
//            #define LUA_TBOOLEAN		1
//            #define LUA_TLIGHTUSERDATA	2
//            #define LUA_TNUMBER		3
//            #define LUA_TSTRING		4
//            #define LUA_TTABLE		5
//            #define LUA_TFUNCTION		6
//            #define LUA_TUSERDATA		7
//            #define LUA_TTHREAD		8
    public static final int LUA_TNONE = -1;
    public static final int LUA_TNIL = 0;
    public static final int LUA_TBOOLEAN = 1;
    public static final int LUA_TLIGHTUSERDATA = 2;
    public static final int LUA_TNUMBER = 3;
    public static final int LUA_TSTRING = 4;
    public static final int LUA_TTABLE = 5;
    public static final int LUA_TFUNCTION = 6;
    public static final int LUA_TUSERDATA = 7;
    public static final int LUA_TTHREAD = 8;
    //    #define LUA_NUMTAGS		9
    public static final int LUA_NUMTAGS = 9;
    //#define LUA_OK		0
//            #define LUA_YIELD	1
//            #define LUA_ERRRUN	2
//            #define LUA_ERRSYNTAX	3
//            #define LUA_ERRMEM	4
//            #define LUA_ERRGCMM	5
//            #define LUA_ERRERR	6
    public static final int LUA_OK = 0;
    public static final int LUA_YIELD = 1;
    public static final int LUA_ERRRUN = 2;
    public static final int LUA_ERRSYNTAX = 3;
    public static final int LUA_ERRMEM = 4;
    public static final int LUA_ERRGCMM = 5;
    public static final int LUA_ERRERR = 6;
    //#define LUA_OPADD	0	/* ORDER TM, ORDER OP */
//            #define LUA_OPSUB	1
//            #define LUA_OPMUL	2
//            #define LUA_OPMOD	3
//            #define LUA_OPPOW	4
//            #define LUA_OPDIV	5
//            #define LUA_OPIDIV	6
//            #define LUA_OPBAND	7
//            #define LUA_OPBOR	8
//            #define LUA_OPBXOR	9
//            #define LUA_OPSHL	10
//            #define LUA_OPSHR	11
//            #define LUA_OPUNM	12
//            #define LUA_OPBNOT	13
//            #define LUA_OPIS    14
    public static final int LUA_OPADD = 0;
    public static final int LUA_OPSUB = 1;
    public static final int LUA_OPMUL = 2;
    public static final int LUA_OPMOD = 3;
    public static final int LUA_OPPOW = 4;
    public static final int LUA_OPDIV = 5;
    public static final int LUA_OPIDIV = 6;
    public static final int LUA_OPBAND = 7;
    public static final int LUA_OPBOR = 8;
    public static final int LUA_OPBXOR = 9;
    public static final int LUA_OPSHL = 10;
    public static final int LUA_OPSHR = 11;
    public static final int LUA_OPUNM = 12;
    public static final int LUA_OPBNOT = 13;
    public static final int LUA_OPIS = 14;
}
