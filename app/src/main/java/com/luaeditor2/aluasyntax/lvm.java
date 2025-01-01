package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.lobject.*;
import static com.luaeditor2.aluasyntax.lstring.*;
import static com.luaeditor2.aluasyntax.lua.*;
import static com.luaeditor2.aluasyntax.luaconf.*;
import static com.luaeditor2.aluasyntax.structs.*;

public class lvm {
    protected lvm() {
    }

    //    #define cvt2num(o)	ttisstring(o)
    protected static boolean cvt2num(TValue o) {
        return ttisstring(o);
    }

    //    int luaV_tointeger(const TValue *obj, lua_Integer *p, int mode) {
//        TValue v;
//        again:
//        if (ttisfloat(obj)) {
//            lua_Number n = fltvalue(obj);
//            lua_Number f = l_floor(n);
//            if (n != f) {  /* not an integral value? */
//                if (mode == 0) return 0;  /* fails if mode demands integral value */
//                else if (mode > 1)  /* needs ceil? */
//                    f += 1;  /* convert floor to ceil (remember: n != f) */
//            }
//            return lua_numbertointeger(f, p);
//        } else if (ttisinteger(obj)) {
//        *p = ivalue(obj);
//            return 1;
//        } else if (cvt2num(obj) &&
//                luaO_str2num(svalue(obj), &v) == vslen(obj) + 1) {
//            obj = &v;
//        goto again;  /* convert result from 'luaO_str2num' to an integer */
//        }
//        return 0;  /* conversion failed */
//    }
    protected static int luaV_tointeger(TValue obj, long[] p, int mode) {
        return luaV_tointeger(obj, p, mode, null);
    }

    protected static int luaV_tointeger(TValue obj, long[] p, int mode, TValue v) {
        if (ttisfloat(obj)) {
            double n = fltvalue(obj);
            double f = Math.floor(n);
            if (n != f) {
                if (mode == 0) return 0;
                else if (mode > 1) f += 1;
            }
            return lua_numbertointeger(f, p);
        } else if (ttisinteger(obj)) {
            p[0] = ivalue(obj);
            return 1;
        } else if (cvt2num(obj) &&
                luaO_str2num(svalue(obj), v) == vslen(obj) + 1) {
            obj = v;
            return luaV_tointeger(obj, p, mode, v);
        }
        return 0;
    }

    //    #define luaV_rawequalobj(t1,t2)		luaV_equalobj(NULL,t1,t2)
    protected static boolean luaV_rawequalobj(TValue t1, TValue t2) {
        return luaV_equalobj(null, t1, t2);
    }

    //    int luaV_equalobj(lua_State *L, const TValue *t1, const TValue *t2) {
//    const TValue *tm;
//        if (ttype(t1) != ttype(t2)) {  /* not the same variant? */
//            if (ttnov(t1) != ttnov(t2) || ttnov(t1) != LUA_TNUMBER)
//                return 0;  /* only numbers can be equal with different variants */
//            else {  /* two numbers with different variants */
//                lua_Integer i1, i2;  /* compare them as integers */
//                return (tointeger(t1, &i1) && tointeger(t2, &i2) && i1 == i2);
//            }
//        }
//        /* values have same type and same variant */
//        switch (ttype(t1)) {
//            case LUA_TNIL:
//                return 1;
//            case LUA_TNUMINT:
//                return (ivalue(t1) == ivalue(t2));
//            case LUA_TNUMFLT:
//                return luai_numeq(fltvalue(t1), fltvalue(t2));
//            case LUA_TBOOLEAN:
//                return bvalue(t1) == bvalue(t2);  /* true must be 1 !! */
//            case LUA_TLIGHTUSERDATA:
//                return pvalue(t1) == pvalue(t2);
//            case LUA_TLCF:
//                return fvalue(t1) == fvalue(t2);
//            case LUA_TSHRSTR:
//                return eqshrstr(tsvalue(t1), tsvalue(t2));
//            case LUA_TLNGSTR:
//                return luaS_eqlngstr(tsvalue(t1), tsvalue(t2));
//            case LUA_TUSERDATA: {
//                if (uvalue(t1) == uvalue(t2)) return 1;
//                else if (L == NULL) return 0;
//                tm = fasttm(L, uvalue(t1)->metatable, TM_EQ);
//                if (tm == NULL)
//                    tm = fasttm(L, uvalue(t2)->metatable, TM_EQ);
//                break;  /* will try TM */
//            }
//            case LUA_TTABLE: {
//                if (hvalue(t1) == hvalue(t2)) return 1;
//                else if (L == NULL) return 0;
//                tm = fasttm(L, hvalue(t1)->metatable, TM_EQ);
//                if (tm == NULL)
//                    tm = fasttm(L, hvalue(t2)->metatable, TM_EQ);
//                break;  /* will try TM */
//            }
//            default:
//                return gcvalue(t1) == gcvalue(t2);
//        }
//        if (tm == NULL)  /* no TM? */
//            return 0;  /* objects are different */
//        luaT_callTM(L, tm, t1, t2, L->top, 1);  /* call TM */
//        return !l_isfalse(L->top);
//    }
//#define luai_numeq(a,b)         ((a)==(b)||(fabs(a-b)<a/10e14))
    protected static boolean luai_numeq(double a, double b) {
        return ((a) == (b) || (Math.abs(a - b) < a / 10e14));
    }

    protected static boolean luaV_equalobj(lua_State L, TValue t1, TValue t2) {
//        TValue tm;
        if (ttype(t1) != ttype(t2)) {  /* not the same variant? */
            if (ttnov(t1) != ttnov(t2) || ttnov(t1) != LUA_TNUMBER)
                return false;  /* only numbers can be equal with different variants */
            else {  /* two numbers with different variants */
                long[] i1 = new long[]{0};
                long[] i2 = new long[]{0};
                return (tointeger(t1, i1) && tointeger(t2, i2) && i1 == i2);
            }
        }
        /* values have same type and same variant */
        switch (ttype(t1)) {
            case LUA_TNIL:
                return true;
            case LUA_TNUMINT:
                return (ivalue(t1) == ivalue(t2));
            case LUA_TNUMFLT:
                return luai_numeq(fltvalue(t1), fltvalue(t2));
            case LUA_TBOOLEAN:
                return bvalue(t1) == bvalue(t2);  /* true must be 1 !! */
            case LUA_TLIGHTUSERDATA:
                return pvalue(t1) == pvalue(t2);
            case LUA_TLCF:
                return fvalue(t1) == fvalue(t2);
            case LUA_TSHRSTR:
                return eqshrstr(tsvalue(t1), tsvalue(t2));
            case LUA_TLNGSTR:
                return luaS_eqlngstr(tsvalue(t1), tsvalue(t2));
            case LUA_TUSERDATA: {
                if (uvalue(t1) == uvalue(t2)) return true;
                else if (L == null) return false;
//                tm = fasttm(L, uvalue(t1).metatable, TM_EQ);
//                if (tm == null)
//                    tm = fasttm(L, uvalue(t2).metatable, TM_EQ);
                break;
            }
            case LUA_TTABLE: {
                if (hvalue(t1) == hvalue(t2)) return true;
                else if (L == null) return false;
//                tm = fasttm(L, hvalue(t1).metatable, TM_EQ);
//                if (tm == null)
//                    tm = fasttm(L, hvalue(t2)->metatable, TM_EQ);
                break;
            }
            default:
                return gcvalue(t1) == gcvalue(t2);
        }
//        if (tm == null)  /* no TM? */
        return false;
//        luaT_callTM(L, tm, t1, t2, L -> top, 1);  /* call TM */
//        return !l_isfalse(L -> top);
    }

    //    #define tointeger(o,i) \
//            (ttisinteger(o) ? (*(i) = ivalue(o), 1) : luaV_tointeger(o,i,LUA_FLOORN2I))
    protected static boolean tointeger(TValue o, long[] i) {
        if (ttisinteger(o)) {
            i[0] = ivalue(o);
            return true;
        } else {
            return luaV_tointeger(o, i, LUA_FLOORN2I) != 0;
        }
    }

    //    #define LUA_FLOORN2I		0
    protected static final int LUA_FLOORN2I = 0;

    //    lua_Integer luaV_mod(lua_State *L, lua_Integer m, lua_Integer n) {
//        if (l_castS2U(n) + 1u <= 1u) {  /* special cases: -1 or 0 */
//            if (n == 0)
//                luaG_runerror(L, "attempt to perform 'n%%0'");
//            return 0;   /* m % -1 == 0; avoid overflow with 0x80000...%-1 */
//        } else {
//            lua_Integer r = m % n;
//            if (r != 0 && (m ^ n) < 0)  /* 'm/n' would be non-integer negative? */
//                r += n;  /* correct result for different rounding */
//            return r;
//        }
//    }
    protected static long luaV_mod(lua_State L, long m, long n) throws LuaError {
        if (n + 1 <= 1) {
            if (n == 0)
                throw new LuaError(L, "mod求解时企图执行0%0");
            return 0;
        } else {
            long r = m % n;
            if (r != 0 && (m ^ n) < 0)
                r += n;
            return r;
        }
    }

    //    lua_Integer luaV_div(lua_State *L, lua_Integer m, lua_Integer n) {
//        if (l_castS2U(n) + 1u <= 1u) {  /* special cases: -1 or 0 */
//            if (n == 0)
//                luaG_runerror(L, "attempt to divide by zero");
//            return intop(-, 0, m);   /* n==-1; avoid overflow with 0x80000...//-1 */
//        } else {
//            lua_Integer q = m / n;  /* perform C division */
//            if ((m ^ n) < 0 && m % n != 0)  /* 'm/n' would be negative non-integer? */
//                q -= 1;  /* correct result for different rounding */
//            return q;
//        }
//    }
    protected static long luaV_div(lua_State L, long m, long n) throws LuaError {
        if (n + 1 <= 1) {
            if (n == 0)
                throw new LuaError(L, "尝试除0");
            return 0 - m;
        } else {
            long q = m / n;
            if ((m ^ n) < 0 && m % n != 0)
                q -= 1;
            return q;
        }
    }

    //#define NBITS    cast_int(sizeof(lua_Integer) * CHAR_BIT)
    protected static final int NBITS = 8 * 8;

    //    lua_Integer luaV_shiftl(lua_Integer x, lua_Integer y) {
//        if (y < 0) {  /* shift right? */
//            if (y <= -NBITS) return 0;
//            else return intop(>>, x, -y);
//        } else {  /* shift left */
//            if (y >= NBITS) return 0;
//            else return intop(<<, x, y);
//        }
//    }
    protected static long luaV_shiftl(long x, long y) {
        if (y < 0) {
            if (y <= -NBITS) return 0;
            else return x >> -y;
        } else {
            if (y >= NBITS) return 0;
            else return x << y;
        }
    }

    //    #define tonumber(o,n) \
//            (ttisfloat(o) ? (*(n) = fltvalue(o), 1) : luaV_tonumber_(o,n))
    protected static boolean tonumber(TValue o, double[] n) {
        if (ttisfloat(o)) {
            n[0] = fltvalue(o);
            return true;
        } else {
            return luaV_tonumber_(o, n);
        }
    }

    //    int luaV_tonumber_(const TValue *obj, lua_Number *n) {
//        TValue v;
//        if (ttisinteger(obj)) {
//        *n = cast_num(ivalue(obj));
//            return 1;
//        } else if (cvt2num(obj) &&  /* string convertible to number? */
//                luaO_str2num(svalue(obj), &v) == vslen(obj) + 1) {
//        *n = nvalue(&v);  /* convert result of 'luaO_str2num' to a float */
//            return 1;
//        } else
//        return 0;  /* conversion failed */
//    }
    protected static boolean luaV_tonumber_(TValue obj, double[] n) {
        TValue v = new TValue();
        if (ttisinteger(obj)) {
            n[0] = ivalue(obj);
            return true;
        } else if (cvt2num(obj) &&
                luaO_str2num(svalue(obj), v) == vslen(obj) + 1) {
            n[0] = nvalue(v);
            return true;
        } else
            return false;
    }
}
