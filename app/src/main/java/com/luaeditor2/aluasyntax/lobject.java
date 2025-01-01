package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.clib.*;
import static com.luaeditor2.aluasyntax.lctype.*;
import static com.luaeditor2.aluasyntax.lua.*;
import static com.luaeditor2.aluasyntax.luaconf.*;
import static com.luaeditor2.aluasyntax.lvm.*;
import static com.luaeditor2.aluasyntax.structs.*;

public class lobject {
    public lobject() {
    }

    //    #define RETS    "..."
//            #define PRE    "[string \""
//            #define POS    "\"]"
    public static final String RETS = "...";
    public static final String PRE = "[string \"";
    public static final String POS = "\"]";

//    void luaO_chunkid(char *out, const char *source, size_t bufflen) {
//        size_t l = strlen(source);
//        if (*source == '=') {  /* 'literal' source */
//            if (l <= bufflen)  /* small enough? */
//                memcpy(out, source + 1, l * sizeof(char));
//        else {  /* truncate it */
//                addstr(out, source + 1, bufflen - 1);
//            *out = '\0';
//            }
//        } else if (*source == '@') {  /* file name */
//            if (l <= bufflen)  /* small enough? */
//                memcpy(out, source + 1, l * sizeof(char));
//        else {  /* add '...' before rest of name */
//                addstr(out, RETS, LL(RETS));
//                bufflen -= LL(RETS);
//                memcpy(out, source + 1 + l - bufflen, bufflen * sizeof(char));
//            }
//        } else {  /* string; format as [string "source"] */
//        const char *nl = strchr(source, '\n');  /* find first new line (if any) */
//            addstr(out, PRE, LL(PRE));  /* add prefix */
//            bufflen -= LL(PRE RETS POS) + 1;  /* save space for prefix+suffix+'\0' */
//            if (l < bufflen && nl == NULL) {  /* small one-line source? */
//                addstr(out, source, l);  /* keep it */
//            } else {
//                if (nl != NULL) l = nl - source;  /* stop at first newline */
//                if (l > bufflen) l = bufflen;
//                addstr(out, source, l);
//                addstr(out, RETS, LL(RETS));
//            }
//            memcpy(out, POS, (LL(POS) + 1) * sizeof(char));
//        }
//    }


    //    #define addstr(a, b, l)    ( memcpy(a,b,(l) * sizeof(char)), a += (l) )
    public static void luaO_chunkid(byte[] out, String source, int bufflen) {
        int l = source.getBytes().length;
        if (source.charAt(0) == '=') {
            if (l <= bufflen) {
                memcpy(out, source.getBytes(), l);
            } else {
                memcpy(out, source.substring(1).getBytes(), bufflen - 1);
                out[bufflen - 1] = 0;
            }
        } else if (source.charAt(0) == '@') {
            if (l <= bufflen) {
                memcpy(out, source.substring(1).getBytes(), l);
            } else {
                memcpy(out, RETS);
                bufflen -= RETS.getBytes().length;
                memcpy(out, 5, source.substring(1 + l - bufflen).getBytes(), bufflen);
            }
        } else {
            int nl = strchr(source.getBytes(), (byte) '\n');
            memcpy(out, PRE);
            int offset = PRE.getBytes().length;
            bufflen -= PRE.getBytes().length + RETS.getBytes().length + POS.getBytes().length + 1;
            if (l < bufflen && nl == -1) {
                memcpy(out, offset, source.getBytes(), l);
            } else {
                if (nl != -1) l = nl;
                if (l > bufflen) l = bufflen;
                memcpy(out, offset, source.substring(0, l).getBytes(), l);
                offset += l;
                memcpy(out, offset, RETS);
            }
        }
    }


    //#define LUA_TLCL	(LUA_TFUNCTION | (0 << 4))  /* Lua closure */
//            #define LUA_TLCF	(LUA_TFUNCTION | (1 << 4))  /* light C function */
//            #define LUA_TCCL	(LUA_TFUNCTION | (2 << 4))  /* C closure */
    public static final int LUA_TLCL = (LUA_TFUNCTION | (0 << 4));
    public static final int LUA_TLCF = (LUA_TFUNCTION | (1 << 4));
    public static final int LUA_TCCL = (LUA_TFUNCTION | (2 << 4));
    //            /* Variant tags for strings */
//            #define LUA_TSHRSTR	(LUA_TSTRING | (0 << 4))  /* short strings */
//            #define LUA_TLNGSTR	(LUA_TSTRING | (1 << 4))  /* long strings */
    public static final int LUA_TSHRSTR = (LUA_TSTRING | (0 << 4));
    public static final int LUA_TLNGSTR = (LUA_TSTRING | (1 << 4));
    //            /* Variant tags for numbers */
//            #define LUA_TNUMFLT	(LUA_TNUMBER | (0 << 4))  /* float numbers */
//            #define LUA_TNUMINT	(LUA_TNUMBER | (1 << 4))  /* integer numbers */
    public static final int LUA_TNUMFLT = (LUA_TNUMBER | (0 << 4));
    public static final int LUA_TNUMINT = (LUA_TNUMBER | (1 << 4));
    //#define LUA_TDEADKEY	(LUA_NUMTAGS+1)		/* removed keys in tables */
    public static final int LUA_TDEADKEY = (LUA_NUMTAGS + 1);

    //#define rttype(o)	((o)->tt_)
    public static int rttype(TValue o) {
        return o.tt_;
    }

    //    #define BIT_ISCOLLECTABLE	(1 << 6)
    public static final int BIT_ISCOLLECTABLE = (1 << 6);

    //    #define ctb(t)			((t) | BIT_ISCOLLECTABLE)
    public static int ctb(int t) {
        return (t | BIT_ISCOLLECTABLE);
    }

    //    #define ttype(o)	(rttype(o) & 0x3F)
    public static int ttype(TValue o) {
        return (rttype(o) & 0x3F);
    }

    //#define ttnov(o)	(novariant(rttype(o)))
    public static int ttnov(TValue o) {
        return (novariant(rttype(o)));
    }

    //    #define novariant(x)	((x) & 0x0F)
    public static int novariant(int x) {
        return (x & 0x0F);
    }

    //#define checktag(o,t)		(rttype(o) == (t))
    public static boolean checktag(TValue o, int t) {
        return rttype(o) == t;
    }

    //            #define checktype(o,t)		(ttnov(o) == (t))
    public static boolean checktype(TValue o, int t) {
        return ttnov(o) == t;
    }

    //            #define ttisnumber(o)		checktype((o), LUA_TNUMBER)
    public static boolean ttisnumber(TValue o) {
        return checktype(o, LUA_TNUMBER);
    }

    //            #define ttisfloat(o)		checktag((o), LUA_TNUMFLT)
    public static boolean ttisfloat(TValue o) {
        return checktag(o, LUA_TNUMFLT);
    }

    //            #define ttisinteger(o)		checktag((o), LUA_TNUMINT)
    public static boolean ttisinteger(TValue o) {
        return checktag(o, LUA_TNUMINT);
    }

    //            #define ttisnil(o)		checktag((o), LUA_TNIL)
    public static boolean ttisnil(TValue o) {
        return checktag(o, LUA_TNIL);
    }

    //            #define ttisboolean(o)		checktag((o), LUA_TBOOLEAN)
    public static boolean ttisboolean(TValue o) {
        return checktag(o, LUA_TBOOLEAN);
    }

    //            #define ttislightuserdata(o)	checktag((o), LUA_TLIGHTUSERDATA)
    public static boolean ttislightuserdata(TValue o) {
        return checktag(o, LUA_TLIGHTUSERDATA);
    }

    //            #define ttisstring(o)		checktype((o), LUA_TSTRING)
    public static boolean ttisstring(TValue o) {
        return checktype(o, LUA_TSTRING);
    }

    //            #define ttisshrstring(o)	checktag((o), ctb(LUA_TSHRSTR))
    public static boolean ttisshrstring(TValue o) {
        return checktag(o, ctb(LUA_TSHRSTR));
    }

    //            #define ttislngstring(o)	checktag((o), ctb(LUA_TLNGSTR))
    public static boolean ttislngstring(TValue o) {
        return checktag(o, ctb(LUA_TLNGSTR));
    }

    //            #define ttistable(o)		checktag((o), ctb(LUA_TTABLE))
    public static boolean ttistable(TValue o) {
        return checktag(o, ctb(LUA_TTABLE));
    }

    //            #define ttisfunction(o)		checktype(o, LUA_TFUNCTION)
    public static boolean ttisfunction(TValue o) {
        return checktype(o, LUA_TFUNCTION);
    }

    //            #define ttisclosure(o)		((rttype(o) & 0x1F) == LUA_TFUNCTION)
    public static boolean ttisclosure(TValue o) {
        return ((rttype(o) & 0x1F) == LUA_TFUNCTION);
    }

    //            #define ttisCclosure(o)		checktag((o), ctb(LUA_TCCL))
    public static boolean ttisCclosure(TValue o) {
        return checktag(o, ctb(LUA_TCCL));
    }

    //            #define ttisLclosure(o)		checktag((o), ctb(LUA_TLCL))
    public static boolean ttisLclosure(TValue o) {
        return checktag(o, ctb(LUA_TLCL));
    }

    //            #define ttislcf(o)		checktag((o), LUA_TLCF)
    public static boolean ttislcf(TValue o) {
        return checktag(o, LUA_TLCF);
    }

    //            #define ttisfulluserdata(o)	checktag((o), ctb(LUA_TUSERDATA))
    public static boolean ttisfulluserdata(TValue o) {
        return checktag(o, ctb(LUA_TUSERDATA));
    }

    //            #define ttisthread(o)		checktag((o), ctb(LUA_TTHREAD))
    public static boolean ttisthread(TValue o) {
        return checktag(o, ctb(LUA_TTHREAD));
    }

    //            #define ttisdeadkey(o)		checktag((o), LUA_TDEADKEY)
    public static boolean ttisdeadkey(TValue o) {
        return checktag(o, LUA_TDEADKEY);
    }

    //#define val_(o)		((o)->value_)
    public static Value val_(TValue o) {
        return o.value_;
    }

    //            /* Macros to access values */
//            #define ivalue(o)	check_exp(ttisinteger(o), val_(o).i)
    public static long ivalue(TValue o) {
        return val_(o).i;
    }

    //            #define fltvalue(o)	check_exp(ttisfloat(o), val_(o).n)
    public static double fltvalue(TValue o) {
        return val_(o).n;
    }

    //            #define nvalue(o)	check_exp(ttisnumber(o), \
//            (ttisinteger(o) ? cast_num(ivalue(o)) : fltvalue(o)))
    public static double nvalue(TValue o) {
        return (ttisinteger(o) ? ivalue(o) : fltvalue(o));
    }

    //            #define gcvalue(o)	check_exp(iscollectable(o), val_(o).gc)
    public static Object gcvalue(TValue o) {
        return val_(o).gc;
    }

    //            #define pvalue(o)	check_exp(ttislightuserdata(o), val_(o).p)
    public static Object pvalue(TValue o) {
        return val_(o).p;
    }

    //            #define tsvalue(o)	check_exp(ttisstring(o), gco2ts(val_(o).gc))
    public static String tsvalue(TValue o) {
        return (String) gcvalue(o);
    }

    //            #define uvalue(o)	check_exp(ttisfulluserdata(o), gco2u(val_(o).gc))
    public static Object uvalue(TValue o) {
        return gcvalue(o);
    }

    //            #define clvalue(o)	check_exp(ttisclosure(o), gco2cl(val_(o).gc))
    public static Object clvalue(TValue o) {
        return gcvalue(o);
    }

    //            #define clLvalue(o)	check_exp(ttisLclosure(o), gco2lcl(val_(o).gc))
    public static Object clLvalue(TValue o) {
        return gcvalue(o);
    }

    //            #define clCvalue(o)	check_exp(ttisCclosure(o), gco2ccl(val_(o).gc))
    public static Object clCvalue(TValue o) {
        return gcvalue(o);
    }

    //            #define fvalue(o)	check_exp(ttislcf(o), val_(o).f)
    public static lua_CFunction fvalue(TValue o) {
        return val_(o).f;
    }

    //            #define hvalue(o)	check_exp(ttistable(o), gco2t(val_(o).gc))
    public static Table hvalue(TValue o) {
        return (Table) gcvalue(o);
    }

    //            #define bvalue(o)	check_exp(ttisboolean(o), val_(o).b)
    public static boolean bvalue(TValue o) {
        return val_(o).b != 0;
    }

    //            #define thvalue(o)	check_exp(ttisthread(o), gco2th(val_(o).gc))
    public static lua_State thvalue(TValue o) {
        return (lua_State) gcvalue(o);
    }

    //            /* a dead value may get the 'gc' field, but cannot access its contents */
//            #define deadvalue(o)	check_exp(ttisdeadkey(o), cast(void *, val_(o).gc))
    public static Object deadvalue(TValue o) {
        return val_(o).gc;
    }

    //            #define l_isfalse(o)	(ttisnil(o) || (ttisboolean(o) && bvalue(o) == 0))
    public static boolean l_isfalse(TValue o) {
        return (ttisnil(o) || (ttisboolean(o) && !bvalue(o)));
    }

    //            #define iscollectable(o)	(rttype(o) & BIT_ISCOLLECTABLE)
    public static boolean iscollectable(TValue o) {
        return (rttype(o) & BIT_ISCOLLECTABLE) != 0;
    }

    //    #define getstr(ts)  \
//    check_exp(sizeof((ts)->extra), cast(char *, (ts)) + sizeof(UTString))
    public static byte[] getstr(String ts) {
        return ts.getBytes();
    }

    //    #define svalue(o)       getstr(tsvalue(o))
    public static byte[] svalue(TValue o) {
        return getstr(tsvalue(o));
    }

    //    /* get string length from 'TString *s' */
//#define tsslen(s)	((s)->tt == LUA_TSHRSTR ? (s)->shrlen : (s)->u.lnglen)
    public static int tsslen(String s) {
        return s.getBytes().length;
    }

    //            /* get string length from 'TValue *o' */
//            #define vslen(o)	tsslen(tsvalue(o))
    public static int vslen(TValue o) {
        return tsslen(tsvalue(o));
    }


    //    /* Macros to set values */
//#define settt_(o,t)	((o)->tt_=(t))
    public static void settt_(TValue o, int t) {
        o.tt_ = t;
    }

    //            #define setfltvalue(obj,x) \
//    { TValue *io=(obj); val_(io).n=(x); settt_(io, LUA_TNUMFLT); }
    public static void setfltvalue(TValue obj, double x) {
        TValue io = obj;
        val_(io).n = x;
        settt_(io, LUA_TNUMFLT);
    }

    //#define chgfltvalue(obj,x) \
//    { TValue *io=(obj); lua_assert(ttisfloat(io)); val_(io).n=(x); }
    public static void chgfltvalue(TValue obj, double x) {
        TValue io = obj;
        val_(io).n = x;
    }

    //#define setivalue(obj,x) \
//    { TValue *io=(obj); val_(io).i=(x); settt_(io, LUA_TNUMINT); }
    public static void setivalue(TValue obj, long x) {
        TValue io = obj;
        val_(io).i = x;
        settt_(io, LUA_TNUMINT);
    }

    //#define chgivalue(obj,x) \
//    { TValue *io=(obj); lua_assert(ttisinteger(io)); val_(io).i=(x); }
    public static void chgivalue(TValue obj, long x) {
        TValue io = obj;
        val_(io).i = x;
    }

    //#define setnilvalue(obj) settt_(obj, LUA_TNIL)
    public static void setnilvalue(TValue obj) {
        settt_(obj, LUA_TNIL);
    }

    //            #define setfvalue(obj,x) \
//    { TValue *io=(obj); val_(io).f=(x); settt_(io, LUA_TLCF); }
    public static void setfvalue(TValue obj, lua_CFunction x) {
        TValue io = obj;
        val_(io).f = x;
        settt_(io, LUA_TLCF);
    }

    //#define setpvalue(obj,x) \
//    { TValue *io=(obj); val_(io).p=(x); settt_(io, LUA_TLIGHTUSERDATA); }
    public static void setpvalue(TValue obj, Object x) {
        TValue io = obj;
        val_(io).p = x;
        settt_(io, LUA_TLIGHTUSERDATA);
    }

    //#define setbvalue(obj,x) \
//    { TValue *io=(obj); val_(io).b=(x); settt_(io, LUA_TBOOLEAN); }
    public static void setbvalue(TValue obj, boolean x) {
        TValue io = obj;
        val_(io).b = x ? 1 : 0;
        settt_(io, LUA_TBOOLEAN);
    }

    public static void setbvalue(TValue obj, int x) {
        TValue io = obj;
        val_(io).b = x;
        settt_(io, LUA_TBOOLEAN);
    }

    //#define setthvalue(L,obj,x) \
//    { TValue *io = (obj); lua_State *x_ = (x); \
//        val_(io).gc = obj2gco(x_); settt_(io, ctb(LUA_TTHREAD)); \
//        checkliveness(L,io); }
    public static void setthvalue(lua_State L, TValue obj, lua_State x) {
        TValue io = obj;
        val_(io).gc = x;
        settt_(io, ctb(LUA_TTHREAD));
    }

    //#define setclLvalue(L,obj,x) \
//    { TValue *io = (obj); LClosure *x_ = (x); \
//        val_(io).gc = obj2gco(x_); settt_(io, ctb(LUA_TLCL)); \
//        checkliveness(L,io); }
    public static void setclLvalue(lua_State L, TValue obj, LClosure x) {
        TValue io = obj;
        val_(io).gc = x;
        settt_(io, ctb(LUA_TLCL));
    }

    //#define setclCvalue(L,obj,x) \
//    { TValue *io = (obj); CClosure *x_ = (x); \
//        val_(io).gc = obj2gco(x_); settt_(io, ctb(LUA_TCCL)); \
//        checkliveness(L,io); }
    public static void setclCvalue(lua_State L, TValue obj, CClosure x) {
        TValue io = obj;
        val_(io).gc = x;
        settt_(io, ctb(LUA_TCCL));
    }

    //#define sethvalue(L,obj,x) \
//    { TValue *io = (obj); Table *x_ = (x); \
//        val_(io).gc = obj2gco(x_); settt_(io, ctb(LUA_TTABLE)); \
//        checkliveness(L,io); }
    public static void sethvalue(lua_State L, TValue obj, Table x) {
        TValue io = obj;
        val_(io).gc = x;
        settt_(io, ctb(LUA_TTABLE));
    }

    //#define setdeadvalue(obj)	settt_(obj, LUA_TDEADKEY)
    public static void setdeadvalue(TValue obj) {
        settt_(obj, LUA_TDEADKEY);
    }

    //            #define setobj(L,obj1,obj2) \
//    { TValue *io1=(obj1); *io1 = *(obj2); \
//        (void)L; checkliveness(L,io1); }
    public static void setobj(lua_State L, TValue obj1, TValue obj2) {
        TValue io1 = obj1;
        io1.set(obj2);
    }

    //    /* from stack to (same) stack */
//#define setobjs2s	setobj
    public static void setobjs2s(lua_State L, TValue o1, TValue o2) {
        setobj(L, o1, o2);
    }

    //    /* to stack (not from same stack) */
//#define setobj2s	setobj
    public static void setobj2s(lua_State L, TValue o1, TValue o2) {
        setobj(L, o1, o2);
    }

    //#define sethvalue2s	sethvalue
    public static void sethvalue2s(lua_State L, TValue o1, Table o2) {
        sethvalue(L, o1, o2);
    }

    //#define setptvalue2s	setptvalue
    public static void setptvalue2s(lua_State L, TValue o1, Object o2) {
        setpvalue(o1, o2);
    }

    //    /* from table to same table */
//#define setobjt2t	setobj
    public static void setobjt2t(lua_State L, TValue o1, TValue o2) {
        setobj(L, o1, o2);
    }

    //    /* to new object */
//#define setobj2n	setobj
    public static void setobj2n(lua_State L, TValue o1, TValue o2) {
        setobj(L, o1, o2);
    }

    //
//    /* to table (define it as an expression to be used in macros) */
//#define setobj2t(L,o1,o2)  ((void)L, *(o1)=*(o2), checkliveness(L,(o1)))
    public static void setobj2t(lua_State L, TValue o1, TValue o2) {
        o1.set(o2);
    }

    //    size_t luaO_str2num(const char *s, TValue *o) {
//        lua_Integer i;
//        lua_Number n;
//        const char *e;
//        if ((e = l_str2int(s, &i)) != NULL) {  /* try as an integer */
//            setivalue(o, i);
//        } else if ((e = l_str2d(s, &n)) != NULL) {  /* else try as a float */
//            setfltvalue(o, n);
//        } else
//        return 0;  /* conversion failed */
//        return (e - s) + 1;  /* success; return string size */
//    }
    public static int luaO_str2num(byte[] s, TValue o) {
        long[] i = new long[]{0};
        double[] n = new double[]{0};
        int e;
        if ((e = l_str2int(s, i)) != -1) {
            setivalue(o, i[0]);
        } else if ((e = l_str2d(s, n)) != -1) {
            setfltvalue(o, n[0]);
        } else
            return 0;
        return e + 1;
    }

    //    #define MAXBY10        cast(lua_Unsigned, LUA_MAXINTEGER / 10)
//#define MAXLASTD    cast_int(LUA_MAXINTEGER % 10)
    public static final long MAXBY10 = (long) (Long.MAX_VALUE / 10);
    public static final int MAXLASTD = (int) (Long.MAX_VALUE % 10);

    //    static const char *l_str2int(const char *s, lua_Integer *result) {
//        lua_Unsigned a = 0;
//        int empty = 1;
//        int neg;
//        while (lisspace(cast_uchar(*s))) s++;  /* skip initial spaces */
//        neg = isneg(&s);
//        if (s[0] == '0' &&
//                (s[1] == 'x' || s[1] == 'X')) {  /* hex? */
//            s += 2;  /* skip '0x' */
//            for (; lisxdigit(cast_uchar(*s)); s++) {
//                a = a * 16 + luaO_hexavalue(*s);
//                empty = 0;
//            }
//        } else {  /* decimal */
//            //mod by nwdxlgzs
//            for (; lisdigit(cast_uchar(*s)) || *s == '_'; s++) {
//                if (*s == '_') continue;
//                int d = *s - '0';
//                if (a >= MAXBY10 && (a > MAXBY10 || d > MAXLASTD + neg))  /* overflow? */
//                    return NULL;  /* do not accept it (as integer) */
//                a = a * 10 + d;
//                empty = 0;
//            }
//        }
//        while (lisspace(cast_uchar(*s))) s++;  /* skip trailing spaces */
//        if (empty || *s != '\0') return NULL;  /* something wrong in the numeral */
//    else {
//        *result = l_castU2S((neg) ? 0u - a : a);
//            return s;
//        }
//    }
    public static int l_str2int(byte[] s, long[] result) {
        long a = 0;
        int empty = 1;
        int neg;
        int i = 0;
        while (lisspace(s[i])) i++;  /* skip initial spaces */
        neg = isneg(s);
        if (s[i] == '0' &&
                (s[i + 1] == 'x' || s[i + 1] == 'X')) {  /* hex? */
            i += 2;  /* skip '0x' */
            for (; lisxdigit(s[i]); i++) {
                a = a * 16 + luaO_hexavalue(s[i]);
                empty = 0;
            }
        } else {  /* decimal */
            for (; lisdigit(s[i]) || s[i] == '_'; i++) {
                if (s[i] == '_') continue;
                int d = s[i] - '0';
                if (a >= MAXBY10 && (a > MAXBY10 || d > MAXLASTD + neg))  /* overflow? */
                    return -1;  /* do not accept it (as integer) */
                a = a * 10 + d;
                empty = 0;
            }
        }
        while (lisspace(s[i])) i++;  /* skip trailing spaces */
        if (empty != 0 || s[i] != '\0') return -1;  /* something wrong in the numeral */
        else {
            result[0] = (neg != 0) ? 0 - a : a;
            return i;
        }
    }

    //    int luaO_hexavalue(int c) {
//        if (lisdigit(c)) return c - '0';
//        else return (ltolower(c) - 'a') + 10;
//    }
    public static int luaO_hexavalue(byte c) {
        if (lisdigit(c)) return c - '0';
        else return (ltolower(c) - 'a') + 10;
    }


    //    static int isneg(const char **s) {
//        if (**s == '-') {
//            (*s)++;
//            return 1;
//        } else if (**s == '+') (*s)++;
//        return 0;
//    }
    public static int isneg(byte[] s) {
        if (s[0] == '-') {
            return 1;
        } else if (s[0] == '+') return 1;
        return 0;
    }

    //    static const char *l_str2d(const char *s, lua_Number *result) {
//    const char *endptr;
//    const char *pmode = strpbrk(s, ".xXnN");
//        int mode = pmode ? ltolower(cast_uchar(*pmode)) : 0;
//        if (mode == 'n')  /* reject 'inf' and 'nan' */
//            return NULL;
//        endptr = l_str2dloc(s, result, mode);  /* try to convert */
//        if (endptr == NULL) {  /* failed? may be a different locale */
//            char buff[L_MAXLENNUM + 1];
//            char *pdot = strchr(s, '.');
//            if (strlen(s) > L_MAXLENNUM || pdot == NULL)
//                return NULL;  /* string too long or no dot; fail */
//            strcpy(buff, s);  /* copy string to buffer */
//            buff[pdot - s] = lua_getlocaledecpoint();  /* correct decimal point */
//            endptr = l_str2dloc(buff, result, mode);  /* try again */
//            if (endptr != NULL)
//                endptr = s + (endptr - buff);  /* make relative to 's' */
//        }
//        return endptr;
//    }
    public static int l_str2d(byte[] s, double[] result) {
        int endptr;
        int pmode = strchr(s, (byte) '.');
        int mode = pmode != -1 ? ltolower(s[pmode]) : 0;
        if (mode == 'n')  /* reject 'inf' and 'nan' */
            return -1;
        endptr = l_str2dloc(s, result, mode);  /* try to convert */
        if (endptr == -1) {  /* failed? may be a different locale */
            byte[] buff = new byte[L_MAXLENNUM + 1];
            int pdot = strchr(s, (byte) '.');
            if (s.length > L_MAXLENNUM || pdot == -1)
                return -1;  /* string too long or no dot; fail */
            memcpy(buff, s, pdot);
            buff[pdot - 1] = lua_getlocaledecpoint();  /* correct decimal point */
            endptr = l_str2dloc(buff, result, mode);  /* try again */
        }
        return endptr;
    }

    //    #define L_MAXLENNUM    200
    public static final int L_MAXLENNUM = 200;

    //    static const char *l_str2dloc(const char *s, lua_Number *result, int mode) {
//        char *endptr;
//    *result = (mode == 'x') ? lua_strx2number(s, &endptr)  /* try to convert */
//                            : lua_str2number(s, &endptr);
//        if (endptr == s) return NULL;  /* nothing recognized? */
//        while (lisspace(cast_uchar(*endptr))) endptr++;  /* skip trailing spaces */
//        return (*endptr == '\0') ? endptr : NULL;  /* OK if no trailing characters */
//    }
    public static int l_str2dloc(byte[] s, double[] result, int mode) {
        int[] endptr = new int[]{0};
        result[0] = (mode == 'x') ? lua_strx2number(s, endptr)  /* try to convert */
                : lua_str2number(s, endptr);
        if (endptr[0] == 0) return -1;  /* nothing recognized? */
        while (lisspace(s[endptr[0]])) endptr[0]++;  /* skip trailing spaces */
        return (s[endptr[0]] == '\0') ? endptr[0] : -1;  /* OK if no trailing characters */
    }

    //    #define lua_strx2number(s, p)        lua_str2number(s,p)
    public static double lua_strx2number(byte[] s, int[] p) {
        return lua_str2number(s, p);
    }

    //    #define lua_str2number(s, p)    strtod((s), (p))
    public static double lua_str2number(byte[] s, int[] p) {
        return strtod(s, p);
    }

    //    #define UTF8BUFFSZ	8
    public static final int UTF8BUFFSZ = 8;

    //    int luaO_utf8esc(char *buff, unsigned long x) {
//        int n = 1;  /* number of bytes put in buffer (backwards) */
//        lua_assert(x <= 0x7FFFFFFFu);
//        if (x < 0x80)  /* ascii? */
//            buff[UTF8BUFFSZ - 1] = cast_char(x);
//        else {  /* need continuation bytes */
//            unsigned int mfb = 0x3f;  /* maximum that fits in first byte */
//            do {  /* add continuation bytes */
//                buff[UTF8BUFFSZ - (n++)] = cast_char(0x80 | (x & 0x3f));
//                x >>= 6;  /* remove added bits */
//                mfb >>= 1;  /* now there is one less bit available in first byte */
//            } while (x > mfb);  /* still needs continuation byte? */
//            buff[UTF8BUFFSZ - n] = cast_char((~mfb << 1) | x);  /* add first byte */
//        }
//        return n;
//    }
    public static int luaO_utf8esc(byte[] buff, long x) {
        int n = 1;
        if (x < 0x80)
            buff[UTF8BUFFSZ - 1] = (byte) x;
        else {
            int mfb = 0x3f;
            do {
                buff[UTF8BUFFSZ - (n++)] = (byte) (0x80 | (x & 0x3f));
                x >>= 6;
                mfb >>= 1;
            } while (x > mfb);
            buff[UTF8BUFFSZ - n] = (byte) ((~mfb << 1) | x);
        }
        return n;
    }

    //    const char *luaO_pushfstring(lua_State *L, const char *fmt, ...) {
//    const char *msg;
//        va_list argp;
//        va_start(argp, fmt);
//        msg = luaO_pushvfstring(L, fmt, argp);
//        va_end(argp);
//        return msg;
//    }
    public static String luaO_pushfstring(lua_State L, String fmt, Object... args) {
        return String.format(fmt, args);
    }

    //    int luaO_int2fb(unsigned int x) {
//        int e = 0;  /* exponent */
//        if (x < 8) return x;
//        while (x >= (8 << 4)) {  /* coarse steps */
//            x = (x + 0xf) >> 4;  /* x = ceil(x / 16) */
//            e += 4;
//        }
//        while (x >= (8 << 1)) {  /* fine steps */
//            x = (x + 1) >> 1;  /* x = ceil(x / 2) */
//            e++;
//        }
//        return ((e + 1) << 3) | (cast_int(x) - 8);
//    }
    public static int luaO_int2fb(long x) {
        int e = 0;
        if (x < 8) return (int) x;
        while (x >= (8 << 4)) {
            x = (x + 0xf) >> 4;
            e += 4;
        }
        while (x >= (8 << 1)) {
            x = (x + 1) >> 1;
            e++;
        }
        return ((e + 1) << 3) | ((int) x - 8);
    }

    //    void luaO_arith(lua_State *L, int op, const TValue *p1, const TValue *p2,
//                    TValue *res) {
//        switch (op) {
//            case LUA_OPBAND:
//            case LUA_OPBOR:
//            case LUA_OPBXOR:
//            case LUA_OPSHL:
//            case LUA_OPSHR:
//            case LUA_OPBNOT: {  /* operate only on integers */
//                lua_Integer i1;
//                lua_Integer i2;
//                if (tointeger(p1, &i1) && tointeger(p2, &i2)) {
//                    setivalue(res, intarith(L, op, i1, i2));
//                    return;
//                } else break;  /* go to the end */
//            }
//            case LUA_OPDIV:
//            case LUA_OPPOW: {  /* operate only on floats */
//                lua_Number n1;
//                lua_Number n2;
//                if (tonumber(p1, &n1) && tonumber(p2, &n2)) {
//                    setfltvalue(res, numarith(L, op, n1, n2));
//                    return;
//                } else break;  /* go to the end */
//            }
//            default: {  /* other operations */
//                lua_Number n1;
//                lua_Number n2;
//                if (ttisinteger(p1) && ttisinteger(p2)) {
//                    setivalue(res, intarith(L, op, ivalue(p1), ivalue(p2)));
//                    return;
//                } else if (tonumber(p1, &n1) && tonumber(p2, &n2)) {
//                    setfltvalue(res, numarith(L, op, n1, n2));
//                    return;
//                } else break;  /* go to the end */
//            }
//        }
//        /* could not perform raw operation; try metamethod */
//        lua_assert(L != NULL);  /* should not fail when folding (compile time) */
//        luaT_trybinTM(L, p1, p2, res, cast(TMS, (op - LUA_OPADD) + TM_ADD));
//    }
    public static void luaO_arith(lua_State L, int op, TValue p1, TValue p2, TValue res) throws LuaError {
        switch (op) {
            case LUA_OPBAND:
            case LUA_OPBOR:
            case LUA_OPBXOR:
            case LUA_OPSHL:
            case LUA_OPSHR:
            case LUA_OPBNOT: {
                long[] i1 = new long[]{0};
                long[] i2 = new long[]{0};
                if (tointeger(p1, i1) && tointeger(p2, i2)) {
                    setivalue(res, intarith(L, op, i1[0], i2[0]));
                    return;
                } else break;
            }
            case LUA_OPDIV:
            case LUA_OPPOW: {
                double[] n1 = new double[]{0};
                double[] n2 = new double[]{0};
                if (tonumber(p1, n1) && tonumber(p2, n2)) {
                    setfltvalue(res, numarith(L, op, n1[0], n2[0]));
                    return;
                } else break;
            }
            default: {
                double[] n1 = new double[]{0};
                double[] n2 = new double[]{0};
                if (ttisinteger(p1) && ttisinteger(p2)) {
                    setivalue(res, intarith(L, op, ivalue(p1), ivalue(p2)));
                    return;
                } else if (tonumber(p1, n1) && tonumber(p2, n2)) {
                    setfltvalue(res, numarith(L, op, n1[0], n2[0]));
                    return;
                } else break;
            }
        }
//        luaT_trybinTM(L, p1, p2, res, (op - LUA_OPADD) + TMS.TM_ADD);
    }
//    static lua_Integer intarith(lua_State *L, int op, lua_Integer v1,
//                                lua_Integer v2) {
//        switch (op) {
//            case LUA_OPADD:
//                return intop(+, v1, v2);
//            case LUA_OPSUB:
//                return intop(-, v1, v2);
//            case LUA_OPMUL:
//                return intop(*, v1, v2);
//            case LUA_OPMOD:
//                return luaV_mod(L, v1, v2);
//            case LUA_OPIDIV:
//                return luaV_div(L, v1, v2);
//            case LUA_OPBAND:
//                return intop(&, v1, v2);
//            case LUA_OPBOR:
//                return intop(|, v1, v2);
//            case LUA_OPBXOR:
//                return intop(^, v1, v2);
//            case LUA_OPSHL:
//                return luaV_shiftl(v1, v2);
//            case LUA_OPSHR:
//                return luaV_shiftl(v1, -v2);
//            case LUA_OPUNM:
//                return intop(-, 0, v1);
//            case LUA_OPBNOT:
//                return intop(^, ~l_castS2U(0), v1);
//            default:
//                lua_assert(0);
//                return 0;
//        }
//    }
    public static long intarith(lua_State L, int op, long v1, long v2) throws LuaError {
        switch (op) {
            case LUA_OPADD:
                return v1 + v2;
            case LUA_OPSUB:
                return v1 - v2;
            case LUA_OPMUL:
                return v1 * v2;
            case LUA_OPMOD:
                return luaV_mod(L, v1, v2);
            case LUA_OPIDIV:
                return luaV_div(L, v1, v2);
            case LUA_OPBAND:
                return v1 & v2;
            case LUA_OPBOR:
                return v1 | v2;
            case LUA_OPBXOR:
                return v1 ^ v2;
            case LUA_OPSHL:
                return luaV_shiftl(v1, v2);
            case LUA_OPSHR:
                return luaV_shiftl(v1, -v2);
            case LUA_OPUNM:
                return -v1;
            case LUA_OPBNOT:
                return ~v1;
            default:
                return 0;
        }
    }
//    static lua_Number numarith(lua_State *L, int op, lua_Number v1,
//                               lua_Number v2) {
//        switch (op) {
//            case LUA_OPADD:
//                return luai_numadd(L, v1, v2);
//            case LUA_OPSUB:
//                return luai_numsub(L, v1, v2);
//            case LUA_OPMUL:
//                return luai_nummul(L, v1, v2);
//            case LUA_OPDIV:
//                return luai_numdiv(L, v1, v2);
//            case LUA_OPPOW:
//                return luai_numpow(L, v1, v2);
//            case LUA_OPIDIV:
//                return luai_numidiv(L, v1, v2);
//            case LUA_OPUNM:
//                return luai_numunm(L, v1);
//            case LUA_OPMOD: {
//                lua_Number m;
//                luai_nummod(L, v1, v2, m);
//                return m;
//            }
//            default:
//                lua_assert(0);
//                return 0;
//        }
//    }
    public static double numarith(lua_State L, int op, double v1, double v2) {
        switch (op) {
            case LUA_OPADD:
                return v1 + v2;
            case LUA_OPSUB:
                return v1 - v2;
            case LUA_OPMUL:
                return v1 * v2;
            case LUA_OPDIV:
                return v1 / v2;
            case LUA_OPPOW:
                return Math.pow(v1, v2);
            case LUA_OPIDIV:
                return Math.floor(v1 / v2);
            case LUA_OPUNM:
                return -v1;
            case LUA_OPMOD:
                return v1 % v2;
            default:
                return 0;
        }
    }
//    #define nvalue(o)	check_exp(ttisnumber(o), \
//            (ttisinteger(o) ? cast_num(ivalue(o)) : fltvalue(o)))

}
