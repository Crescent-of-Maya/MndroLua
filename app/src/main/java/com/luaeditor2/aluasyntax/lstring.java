package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.llex.*;
import static com.luaeditor2.aluasyntax.llimits.*;

import java.util.HashMap;

public class lstring {
    protected lstring() {
    }

    //        for (i = 0; i < NUM_RESERVED; i++) {
//        TString *ts = luaS_new(L, luaX_tokens[i]);
//        luaC_fix(L, obj2gco(ts));  /* reserved words are never collected */
//        ts->extra = cast_byte(i + 1);  /* reserved word */
//    }
    protected static final HashMap<String, Integer> RESERVED_extra = new HashMap<>();

    static {
        for (int i = 0; i < NUM_RESERVED; i++) {
            String ts = luaX_tokens[i];
            int extra = (i + 1);
            RESERVED_extra.put(ts, extra);
        }
    }

    //    #define isreserved(s)	((s)->tt == LUA_TSHRSTR && (s)->extra > 0)
    protected static int isreserved(String s) {
        byte[] b = s.getBytes();
        if (b.length > LUAI_MAXSHORTLEN) return -1;
        Integer extra = RESERVED_extra.get(s);
        if (extra == null) return -1;
        return extra;
    }

    //    #define eqshrstr(a,b)	check_exp((a)->tt == LUA_TSHRSTR, (a) == (b))
    protected static boolean eqshrstr(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    //    int luaS_eqlngstr(TString *a, TString *b) {
//        size_t len = a->u.lnglen;
//        lua_assert(a->tt == LUA_TLNGSTR && b->tt == LUA_TLNGSTR);
//        return (a == b) ||  /* same instance or... */
//                ((len == b->u.lnglen) &&  /* equal length and ... */
//                        (memcmp(getstr(a), getstr(b), len) == 0));  /* equal contents */
//    }
    protected static boolean luaS_eqlngstr(String a, String b) {
        return eqshrstr(a, b);
    }
}
