package com.luaeditor2.aluasyntax;


import com.luaeditor2.aluasyntax.structs.*;
import static com.luaeditor2.aluasyntax.structs.TValue.*;
import static com.luaeditor2.aluasyntax.lobject.*;
import static com.luaeditor2.aluasyntax.lua.*;
import static com.luaeditor2.aluasyntax.lvm.*;

public class ltable {
    protected ltable() {
    }

    //    const TValue *luaH_getshortstr (Table *t, TString *key) {
//        Node *n = hashstr(t, key);
//        lua_assert(key->tt == LUA_TSHRSTR);
//        for (;;) {  /* check whether 'key' is somewhere in the chain */
//            const TValue *k = gkey(n);
//            if (ttisshrstring(k) && eqshrstr(tsvalue(k), key))
//                return gval(n);  /* that's it */
//            else {
//                int nx = gnext(n);
//                if (nx == 0)
//                    return luaO_nilobject;  /* not found */
//                n += nx;
//            }
//        }
//    }
    protected static TValue luaH_getshortstr(Table t, String key) {
        TValue o = (TValue) t.map.get(key);
        if (o != null) return o;
        o = (TValue) t.map.get(new TValue(key));
        if (o != null) return o;
        return luaO_nilobject;
    }

    //    const TValue *luaH_getint (Table *t, lua_Integer key) {
//        /* (1 <= key && key <= t->sizearray) */
//        if (l_castS2U(key) - 1 < t->sizearray)
//            return &t->array[key - 1];
//        else {
//            Node *n = hashint(t, key);
//            for (;;) {  /* check whether 'key' is somewhere in the chain */
//                if (ttisinteger(gkey(n)) && ivalue(gkey(n)) == key)
//                    return gval(n);  /* that's it */
//                else {
//                    int nx = gnext(n);
//                    if (nx == 0) break;
//                    n += nx;
//                }
//            }
//            return luaO_nilobject;
//        }
//    }
    protected static TValue luaH_getint(Table t, long key) {
        TValue o = (TValue) t.map.get(key);
        if (o != null) return o;
        o = (TValue) t.map.get(new TValue(key));
        if (o != null) return o;
        return luaO_nilobject;
    }

    //    static const TValue *getgeneric (Table *t, const TValue *key) {
//        Node *n = mainposition(t, key);
//        for (;;) {  /* check whether 'key' is somewhere in the chain */
//            if (luaV_rawequalobj(gkey(n), key))
//                return gval(n);  /* that's it */
//            else {
//                int nx = gnext(n);
//                if (nx == 0)
//                    return luaO_nilobject;  /* not found */
//                n += nx;
//            }
//        }
//    }
    protected static TValue getgeneric(Table t, TValue key) {
        TValue o = (TValue) t.map.get(key);
        if (o != null) return o;
        return luaO_nilobject;
    }

    //    const TValue *luaH_get (Table *t, const TValue *key) {
//        switch (ttype(key)) {
//            case LUA_TSHRSTR: return luaH_getshortstr(t, tsvalue(key));
//            case LUA_TNUMINT: return luaH_getint(t, ivalue(key));
//            case LUA_TNIL: return luaO_nilobject;
//            case LUA_TNUMFLT: {
//                lua_Integer k;
//                if (luaV_tointeger(key, &k, 0)) /* index is int? */
//                return luaH_getint(t, k);  /* use specialized version */
//                /* else... */
//            }  /* FALLTHROUGH */
//            default:
//                return getgeneric(t, key);
//        }
//    }
    protected static TValue luaH_get(Table t, TValue key) {
        switch (ttype(key)) {
            case LUA_TSHRSTR:
                return luaH_getshortstr(t, tsvalue(key));
            case LUA_TNUMINT:
                return luaH_getint(t, ivalue(key));
            case LUA_TNIL:
                return luaO_nilobject;
            case LUA_TNUMFLT: {
                long[] k = new long[]{0};
                if (luaV_tointeger(key, k, 0) != 0) {
                    return luaH_getint(t, k[0]);
                }
            }
            default:
                return getgeneric(t, key);
        }
    }

    //    TValue *luaH_set (lua_State *L, Table *t, const TValue *key) {
//       const TValue *p = luaH_get(t, key);
//        if (p != luaO_nilobject)
//            return cast(TValue *, p);
//        else return luaH_newkey(L, t, key);
//    }
    protected static TValue luaH_set(lua_State L, Table t, TValue key) {
        TValue p = luaH_get(t, key);
        if (p != luaO_nilobject) {
            return (TValue) p;
        } else {
            return luaH_newkey(L, t, key);
        }
    }

    //    TValue *luaH_newkey (lua_State *L, Table *t, const TValue *key) {
//        Node *mp;
//        TValue aux;
//        if (ttisnil(key)) luaG_runerror(L, "table index is nil");
//        else if (ttisfloat(key)) {
//            lua_Integer k;
//            if (luaV_tointeger(key, &k, 0)) {  /* index is int? */
//                setivalue(&aux, k);
//                key = &aux;  /* insert it as an integer */
//            }
//    else if (luai_numisnan(fltvalue(key)))
//                luaG_runerror(L, "table index is NaN");
//        }
//        mp = mainposition(t, key);
//        if (!ttisnil(gval(mp)) || isdummy(mp)) {  /* main position is taken? */
//            Node *othern;
//            Node *f = getfreepos(t);  /* get a free place */
//            if (f == NULL) {  /* cannot find a free place? */
//                rehash(L, t, key);  /* grow table */
//                /* whatever called 'newkey' takes care of TM cache */
//                return luaH_set(L, t, key);  /* insert key into grown table */
//            }
//            lua_assert(!isdummy(f));
//            othern = mainposition(t, gkey(mp));
//            if (othern != mp) {  /* is colliding node out of its main position? */
//                /* yes; move colliding node into free position */
//                while (othern + gnext(othern) != mp)  /* find previous */
//                    othern += gnext(othern);
//                gnext(othern) = cast_int(f - othern);  /* rechain to point to 'f' */
//      *f = *mp;  /* copy colliding node into free pos. (mp->next also goes) */
//                if (gnext(mp) != 0) {
//                    gnext(f) += cast_int(mp - f);  /* correct 'next' */
//                    gnext(mp) = 0;  /* now 'mp' is free */
//                }
//                setnilvalue(gval(mp));
//            }
//            else {  /* colliding node is in its own main position */
//                /* new node will go into free position */
//                if (gnext(mp) != 0)
//                    gnext(f) = cast_int((mp + gnext(mp)) - f);  /* chain new position */
//                else lua_assert(gnext(f) == 0);
//                gnext(mp) = cast_int(f - mp);
//                mp = f;
//            }
//        }
//        setnodekey(L, &mp->i_key, key);
//        luaC_barrierback(L, t, key);
//        lua_assert(ttisnil(gval(mp)));
//        return gval(mp);
//    }
    protected static TValue luaH_newkey(lua_State L, Table t, final TValue key) {
        TValue aux = new TValue();
        t.map.put(key, aux);
        return aux;
    }

}
