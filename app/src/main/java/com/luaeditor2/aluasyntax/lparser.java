package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.clib.*;
import static com.luaeditor2.aluasyntax.lcode.*;
import static com.luaeditor2.aluasyntax.lfunc.*;
import static com.luaeditor2.aluasyntax.llex.*;
import static com.luaeditor2.aluasyntax.llimits.*;
import static com.luaeditor2.aluasyntax.lmem.*;
import static com.luaeditor2.aluasyntax.lobject.*;
import static com.luaeditor2.aluasyntax.lopcodes.*;
import static com.luaeditor2.aluasyntax.lstring.*;
import static com.luaeditor2.aluasyntax.lua.*;

import android.graphics.Rect;

import com.luaeditor2.aluasyntax.structs.*;
import com.luaeditor2.common.AluaParser;
import com.luaeditor2.common.Pair;

import java.util.ArrayList;
import java.util.Arrays;

public class lparser {
    protected lparser() {
    }

    // typedef enum {
    // VVOID, /* when 'expdesc' describes the last expression a list,
    // this kind means an empty list (so, no expression) */
    // VNIL, /* constant nil */
    // VTRUE, /* constant true */
    // VFALSE, /* constant false */
    // VK, /* constant in 'k'; info = index of constant in 'k' */
    // VKFLT, /* floating constant; nval = numerical float value */
    // VKINT, /* integer constant; nval = numerical integer value */
    // VNONRELOC, /* expression has its value in a fixed register;
    // info = result register */
    // VLOCAL, /* local variable; info = local register */
    // VUPVAL, /* upvalue variable; info = index of upvalue in 'upvalues' */
    // VINDEXED, /* indexed variable;
    // ind.vt = whether 't' is register or upvalue;
    // ind.t = table register or upvalue;
    // ind.idx = key's R/K index */
    // VJMP, /* expression is a test/comparison;
    // info = pc of corresponding jump instruction */
    // VRELOCABLE, /* expression can put result in any register;
    // info = instruction pc */
    // VCALL, /* expression is a function call; info = instruction pc */
    // VVARARG /* vararg expression; info = instruction pc */
    // } expkind;
    public static final int VVOID = 0;
    public static final int VNIL = VVOID + 1;
    public static final int VTRUE = VNIL + 1;
    public static final int VFALSE = VTRUE + 1;
    public static final int VK = VFALSE + 1;
    public static final int VKFLT = VK + 1;
    public static final int VKINT = VKFLT + 1;
    public static final int VNONRELOC = VKINT + 1;
    public static final int VLOCAL = VNONRELOC + 1;
    public static final int VUPVAL = VLOCAL + 1;
    public static final int VINDEXED = VUPVAL + 1;
    public static final int VJMP = VINDEXED + 1;
    public static final int VRELOCABLE = VJMP + 1;
    public static final int VCALL = VRELOCABLE + 1;
    public static final int VVARARG = VCALL + 1;
    /*
     * maximum number of local variables per function (must be smaller
     * than 250, due to the bytecode format)
     */
    // #define MAXVARS 200
    public static final int MAXVARS = 200;

    // #define hasmultret(k) ((k) == VCALL || (k) == VVARARG)
    protected static boolean hasmultret(int k) {
        return (k == VCALL || k == VVARARG);
    }

    /*
     * because all strings are unified by the scanner, the parser
     * can use pointer equality for string equality
     */
    // #define eqstr(a, b) ((a) == (b))
    protected static boolean eqstr(String a, String b) {
        return a.equals(b);
    }

    // static l_noret semerror(LexState *ls, const char *msg) {
    // ls->t.token = 0; /* remove "near <token>" from final message */
    // luaX_syntaxerror(ls, msg);
    // }
    protected static void semerror(LexState ls, String msg) throws LuaError {
        ls.t.token = 0;
        luaX_syntaxerror(ls, msg);
    }

    // static l_noret error_expected(LexState *ls, int token) {
    // luaX_syntaxerror(ls,
    // luaO_pushfstring(ls->L, "%s expected", luaX_token2str(ls, token)));
    // }
    protected static void error_expected(LexState ls, int token) throws LuaError {
        luaX_syntaxerror(ls, luaO_pushfstring(ls.L, "应有%s", luaX_token2str(ls, token)));
    }

    // static l_noret errorlimit(FuncState *fs, int limit, const char *what) {
    // lua_State *L = fs->ls->L;
    // const char *msg;
    // int line = fs->f->linedefined;
    // const char *where = (line == 0)
    // ? "main function"
    // : luaO_pushfstring(L, "function at line %d", line);
    // msg = luaO_pushfstring(L, "too many %s (limit is %d) in %s",
    // what, limit, where);
    // luaX_syntaxerror(fs->ls, msg);
    // }
    protected static void errorlimit(FuncState fs, int limit, String what) throws LuaError {
        lua_State L = fs.ls.L;
        String msg;
        int line = fs.f.linedefined;
        String where = (line == 0)
                ? "主函数"
                : luaO_pushfstring(L, "在%d行的函数", line);
        msg = luaO_pushfstring(L, "%s处有太多的%s了，上限为%d",
                where, what, limit);
        luaX_syntaxerror(fs.ls, msg);
    }

    // static void checklimit(FuncState *fs, int v, int l, const char *what) {
    // if (v > l) errorlimit(fs, l, what);
    // }

    protected static void checklimit(FuncState fs, int v, int l, String what) throws LuaError {
        if (v > l)
            errorlimit(fs, l, what);
    }

    // static int testnext(LexState *ls, int c) {
    // if (ls->t.token == c) {
    // luaX_next(ls);
    // return 1;
    // } else return 0;
    // }
    protected static boolean testnext(LexState ls, int c) throws LuaError {
        if (ls.t.token == c) {
            luaX_next(ls);
            return true;
        } else
            return false;
    }

    // static int itestnext(LexState *ls, int c) {
    // if (ls->t.token == c) {
    // return 1;
    // } else return 0;
    // }

    protected static boolean itestnext(LexState ls, int c) {
        return ls.t.token == c;
    }

    // static void check(LexState *ls, int c) {
    // if (ls->t.token != c)
    // error_expected(ls, c);
    // }
    protected static void check(LexState ls, int c) throws LuaError {
        if (ls.t.token != c)
            error_expected(ls, c);
    }

    // static void checknext(LexState *ls, int c) {
    // check(ls, c);
    // luaX_next(ls);
    // }
    protected static void checknext(LexState ls, int c) throws LuaError {
        check(ls, c);
        luaX_next(ls);
    }

    // static void ichecknext(LexState *ls, int c) {
    // if (ls->t.token == c)
    // luaX_next(ls);
    // }
    protected static void ichecknext(LexState ls, int c) throws LuaError {
        if (ls.t.token == c)
            luaX_next(ls);
    }

    // static void ichecknext2(LexState *ls, int c1, int c2) {
    // if (ls->t.token == c1 || ls->t.token == c2)
    // luaX_next(ls);
    // }
    // static void ichecknext2(LexState ls, int c1, int c2) throws LuaError {
    // if (ls.t.token == c1 || ls.t.token == c2)
    // luaX_next(ls);
    // }

    // #define check_condition(ls, c, msg) { if (!(c)) luaX_syntaxerror(ls, msg); }
    protected static void check_condition(LexState ls, boolean c, String msg) throws LuaError {
        if (!c) {
            luaX_syntaxerror(ls, msg);
        }
    }

    // protected static void check_condition(LexState ls, int c, String msg) throws
    // LuaError {
    // check_condition(ls, c != 0, msg);
    // }

    // static void check_match(LexState *ls, int what, int who, int where) {
    // if (!testnext(ls, what)) {
    // if (where == ls->linenumber)
    // error_expected(ls, what);
    // else {
    // luaX_syntaxerror(ls, luaO_pushfstring(ls->L,
    // "%s expected (to close %s at line %d)",
    // luaX_token2str(ls, what), luaX_token2str(ls, who),
    // where));
    // }
    // }
    // }
    protected static void check_match(LexState ls, int what, int who, int where) throws LuaError {
        if (!testnext(ls, what)) {
            if (where == ls.linenumber)
                error_expected(ls, what);
            else {
                luaX_syntaxerror(ls, luaO_pushfstring(ls.L,
                        "期待%s（以配对%d行的%s）",
                        luaX_token2str(ls, what), where, luaX_token2str(ls, who)));
            }
        }
    }

    // static TString *str_checkname(LexState *ls) {
    // TString *ts;
    // //check(ls, TK_NAME);
    // //mod by nirenr
    // //mod by nwdxlgzs
    // if (ls->t.token != TK_NAME && (ls->t.token > TK_WHILE || ls->t.token <
    // FIRST_RESERVED
    // || ls->t.token == TK_LAMBDA))
    // error_expected(ls, TK_NAME);
    // ts = ls->t.seminfo.ts;
    // luaX_next(ls);
    // return ts;
    // }
    protected static String str_checkname(LexState ls) throws LuaError {
        String ts;
        if (ls.t.token != TK_NAME && (ls.t.token > TK_WHILE || ls.t.token < FIRST_RESERVED
                || ls.t.token == TK_LAMBDA))
            error_expected(ls, TK_NAME);
        ts = ls.t.seminfo.ts;
        ls.checknameidx = ls.lastnameidx;
        luaX_next(ls);
        return ts;
    }

    // static void init_exp(expdesc *e, expkind k, int i) {
    // e->f = e->t = NO_JUMP;
    // e->k = k;
    // e->u.info = i;
    // }
    protected static void init_exp(expdesc e, int k, int i) {
        e.f = e.t = NO_JUMP;
        e.k = k;
        e.u.info = i;
    }

    // static void codestring(LexState *ls, expdesc *e, TString *s) {
    // init_exp(e, VK, luaK_stringK(ls->fs, s));
    // }
    protected static void codestring(LexState ls, expdesc e, String s) {
        init_exp(e, VK, luaK_stringK(ls.fs, s));
    }

    // static void checkname(LexState *ls, expdesc *e) {
    // codestring(ls, e, str_checkname(ls));
    // }
    protected static void checkname(LexState ls, expdesc e) throws LuaError {
        codestring(ls, e, str_checkname(ls));
    }

    // static int registerlocalvar(LexState *ls, TString *varname) {
    // FuncState *fs = ls->fs;
    // Proto *f = fs->f;
    // int oldsize = f->sizelocvars;
    // luaM_growvector(ls->L, f->locvars, fs->nlocvars, f->sizelocvars,
    // LocVar, SHRT_MAX, "local variables");
    // while (oldsize < f->sizelocvars)
    // f->locvars[oldsize++].varname = NULL;
    // f->locvars[fs->nlocvars].varname = varname;
    // luaC_objbarrier(ls->L, f, varname);
    // return fs->nlocvars++;
    // }
    private static void addLexer(String n, int vartype, int lastidx) {
        ArrayList<Pair> a = AluaParser.LexState_valueMap.get(n);
        if (a == null) {
            a = new ArrayList<>();
            AluaParser.LexState_valueMap.put(n, a);
        }
        a.add(new Pair(lastidx, vartype));
    }

    protected static int registerlocalvar(LexState ls, String varname) {
        FuncState fs = ls.fs;
        Proto f = fs.f;
        int oldsize = f.sizelocvars;
        // luaM_growvector(ls->L, f->locvars, fs->nlocvars, f->sizelocvars,
        // LocVar, SHRT_MAX, "local variables");
        f.locvars = luaM_realloc(ls.L, f.locvars, f.sizelocvars, f.sizelocvars + 1);
        f.sizelocvars++;
        while (oldsize < f.sizelocvars)
            f.locvars[oldsize++].varname = null;
        LocVar var = f.locvars[fs.nlocvars];
        var.varname = varname;
        if (ls.L.inLexer) {
//            var.startidx = ls.lastidx;
            var.startidx = ls.checknameidx;
//            System.out.println(varname + " startidx " + var.startidx);
            if (varname != null)
//            addLexer(varname, VLOCAL, ls.lastidx);
                addLexer(varname, VLOCAL, ls.checknameidx);
        }
        return fs.nlocvars++;
    }

    // static void new_localvar(LexState *ls, TString *name) {
    // FuncState *fs = ls->fs;
    // Dyndata *dyd = ls->dyd;
    // int reg = registerlocalvar(ls, name);
    // checklimit(fs, dyd->actvar.n + 1 - fs->firstlocal,
    // MAXVARS, "local variables");
    // luaM_growvector(ls->L, dyd->actvar.arr, dyd->actvar.n + 1,
    // dyd->actvar.size, Vardesc, MAX_INT, "local variables");
    // dyd->actvar.arr[dyd->actvar.n++].idx = cast(short, reg);
    // }
    protected static void new_localvar(LexState ls, String name) throws LuaError {
        FuncState fs = ls.fs;
        Dyndata dyd = ls.dyd;
        int reg = registerlocalvar(ls, name);
        checklimit(fs, dyd.actvar.n + 1 - fs.firstlocal,
                MAXVARS, "局部变量");
        // luaM_growvector(ls->L, dyd->actvar.arr, dyd->actvar.n + 1,
        // dyd->actvar.size, Vardesc, MAX_INT, "local variables");
        dyd.actvar.arr = luaM_realloc(ls.L, dyd.actvar.arr, dyd.actvar.size, dyd.actvar.size + 1);
        dyd.actvar.size++;
        dyd.actvar.arr[dyd.actvar.n++].idx = (short) (reg);
    }

    // static void new_localvarliteral_(LexState *ls, const char *name, size_t sz) {
    // new_localvar(ls, luaX_newstring(ls, name, sz));
    // }
    // protected static void new_localvarliteral_(LexState ls, byte[] name, int sz)
    // throws LuaError {
    // new_localvar(ls, new String(name, sz));
    // }

    // #define new_localvarliteral(ls, v) \
    // new_localvarliteral_(ls, "" v, (sizeof(v)/sizeof(char))-1)
    protected static void new_localvarliteral(LexState ls, String name) throws LuaError {
        new_localvar(ls, name);
    }

    // static LocVar *getlocvar(FuncState *fs, int i) {
    // int idx = fs->ls->dyd->actvar.arr[fs->firstlocal + i].idx;
    // lua_assert(idx < fs->nlocvars);
    // return &fs->f->locvars[idx];
    // }
    protected static LocVar getlocvar(FuncState fs, int i) {
        int idx = fs.ls.dyd.actvar.arr[fs.firstlocal + i].idx;
        return fs.f.locvars[idx];
    }

    // static void adjustlocalvars(LexState *ls, int nvars) {
    // FuncState *fs = ls->fs;
    // fs->nactvar = cast_byte(fs->nactvar + nvars);
    // for (; nvars; nvars--) {
    // getlocvar(fs, fs->nactvar - nvars)->startpc = fs->pc;
    // }
    // }
    protected static void adjustlocalvars(LexState ls, int nvars) {
        FuncState fs = ls.fs;
        fs.nactvar = (byte) (fs.nactvar + nvars);
        for (; nvars != 0; nvars--) {
            LocVar var = getlocvar(fs, fs.nactvar - nvars);
            var.startpc = fs.pc;
        }
    }

    // static void removevars(FuncState *fs, int tolevel) {
    // fs->ls->dyd->actvar.n -= (fs->nactvar - tolevel);
    // while (fs->nactvar > tolevel)
    // getlocvar(fs, --fs->nactvar)->endpc = fs->pc;
    // }
    protected static void removevars(FuncState fs, int tolevel) {
        removevars(fs, tolevel, true);
    }

    protected static void removevars(FuncState fs, int tolevel, boolean namemode) {
        fs.ls.dyd.actvar.n -= (fs.nactvar - tolevel);
        while (fs.nactvar > tolevel) {
            LocVar var = getlocvar(fs, --fs.nactvar);
            var.endpc = fs.pc;
            if (namemode)
                var.endidx = fs.ls.lastnameidx - 4;
            else
                var.endidx = fs.ls.lastSymEndidx;
        }
    }

    // static int searchupvalue(FuncState *fs, TString *name) {
    // int i;
    // Upvaldesc *up = fs->f->upvalues;
    // for (i = 0; i < fs->nups; i++) {
    // if (eqstr(up[i].name, name)) return i;
    // }
    // return -1; /* not found */
    // }
    protected static int searchupvalue(FuncState fs, String name) {
        int i;
        Upvaldesc[] up = fs.f.upvalues;
        for (i = 0; i < fs.nups; i++) {
            if (eqstr(up[i].name, name))
                return i;
        }
        return -1;
    }

    // static int newupvalue(FuncState *fs, TString *name, expdesc *v) {
    // Proto *f = fs->f;
    // int oldsize = f->sizeupvalues;
    // checklimit(fs, fs->nups + 1, MAXUPVAL, "upvalues");
    // luaM_growvector(fs->ls->L, f->upvalues, fs->nups, f->sizeupvalues,
    // Upvaldesc, MAXUPVAL, "upvalues");
    // while (oldsize < f->sizeupvalues)
    // f->upvalues[oldsize++].name = NULL;
    // f->upvalues[fs->nups].instack = (v->k == VLOCAL);
    // f->upvalues[fs->nups].idx = cast_byte(v->u.info);
    // f->upvalues[fs->nups].name = name;
    // luaC_objbarrier(fs->ls->L, f, name);
    // return fs->nups++;
    // }
    protected static int newupvalue(FuncState fs, String name, expdesc v) throws LuaError {
        Proto f = fs.f;
        int oldsize = f.sizeupvalues;
        checklimit(fs, fs.nups + 1, MAXUPVAL, "上值");
        // luaM_growvector(fs->ls->L, f->upvalues, fs->nups, f->sizeupvalues,
        // Upvaldesc, MAXUPVAL, "upvalues");
        f.upvalues = luaM_realloc(fs.ls.L, f.upvalues, f.sizeupvalues, f.sizeupvalues + 1);
        f.sizeupvalues++;
        while (oldsize < f.sizeupvalues)
            f.upvalues[oldsize++].name = null;
        f.upvalues[fs.nups].instack = (v.k == VLOCAL);
        f.upvalues[fs.nups].idx = (byte) v.u.info;
        f.upvalues[fs.nups].name = name;
        return fs.nups++;
    }

    // static int searchvar(FuncState *fs, TString *n) {
    // int i;
    // for (i = cast_int(fs->nactvar) - 1; i >= 0; i--) {
    // if (eqstr(n, getlocvar(fs, i)->varname))
    // return i;
    // }
    // return -1; /* not found */
    // }
    protected static int searchlocalvar(FuncState fs, String n) {
        int i;
        for (i = (fs.nlocvars) - 1; i >= 0; i--) {
            if (eqstr(n, getlocvar(fs, i).varname))
                return i;
        }
        return -1;
    }

    protected static int searchvar(FuncState fs, String n) {
        int i;
        for (i = (fs.nactvar) - 1; i >= 0; i--) {
            if (eqstr(n, getlocvar(fs, i).varname))
                return i;
        }
        return -1;
    }

    // static void markupval(FuncState *fs, int level) {
    // BlockCnt *bl = fs->bl;
    // while (bl->nactvar > level)
    // bl = bl->previous;
    // bl->upval = 1;
    // fs->needclose = 1;
    // }
    protected static void markupval(FuncState fs, int level) {
        BlockCnt bl = fs.bl;
        while (bl.nactvar > level)
            bl = bl.previous;
        bl.upval = 1;
        fs.needclose = 1;
    }

    // static void singlevaraux(FuncState *fs, TString *n, expdesc *var, int base) {
    // if (fs == NULL) /* no more levels? */
    // init_exp(var, VVOID, 0); /* default is global */
    // else {
    // int v = searchvar(fs, n); /* look up locals at current level */
    // if (v >= 0) { /* found? */
    // init_exp(var, VLOCAL, v); /* variable is local */
    // if (!base)
    // markupval(fs, v); /* local will be used as an upval */
    // } else { /* not found as local at current level; try upvalues */
    // int idx = searchupvalue(fs, n); /* try existing upvalues */
    // if (idx < 0) { /* not found? */
    // singlevaraux(fs->prev, n, var, 0); /* try upper levels */
    // if (var->k == VVOID) /* not found? */
    // return; /* it is a global */
    // /* else was LOCAL or UPVAL */
    // idx = newupvalue(fs, n, var); /* will be a new upvalue */
    // }
    // init_exp(var, VUPVAL, idx); /* new or old upvalue */
    // }
    // }
    // }
    protected static int singlevaraux(FuncState fs, String n, expdesc var, boolean base) throws LuaError {
        return singlevaraux(fs, n, var, base, null);
    }

    protected static int singlevaraux(FuncState fs, String n, expdesc var, boolean base, VarType type) throws LuaError {
        if (fs == null) {
            init_exp(var, VVOID, 0);
            return VVOID;
        } else {
            int v = searchvar(fs, n);
            if (v >= 0) {
                init_exp(var, VLOCAL, v);
                var.type = getlocvar(fs, v).type;
                if (var.type != null) {
                    type.type = var.type.type;
                    type.typename = var.type.typename;
                }
                if (!base)
                    markupval(fs, v);
                return VLOCAL;
            } else {
                int idx = searchupvalue(fs, n);
                if (idx < 0) {
                    singlevaraux(fs.prev, n, var, 0);
                    if (var.k == VVOID)
                        return VVOID;
                    idx = newupvalue(fs, n, var);
                }
                init_exp(var, VUPVAL, idx);
                return VUPVAL;
            }
        }
    }

    protected static int singlevaraux(FuncState fs, String n, expdesc var, int base) throws LuaError {
        VarType type = new VarType(-1);
        int ret = singlevaraux(fs, n, var, base != 0, type);
        if (type.type != -1)
            var.type = type;
        return ret;
    }

    // static void singlevar(LexState *ls, expdesc *var) {
    // TString *varname = str_checkname(ls);
    // FuncState *fs = ls->fs;
    // singlevaraux(fs, varname, var, 1);
    // if (var->k == VVOID) { /* global name? */
    // expdesc key;
    // singlevaraux(fs, ls->envn, var, 1); /* get environment variable */
    // lua_assert(var->k != VVOID); /* this one must exist */
    // codestring(ls, &key, varname); /* key is variable name */
    // luaK_indexed(fs, var, &key); /* env[varname] */
    // }
    // }
    protected static void singlevar(LexState ls, expdesc var) throws LuaError {
        String varname = str_checkname(ls);
        FuncState fs = ls.fs;
        int vartype = singlevaraux(fs, varname, var, 1);
        if (ls.L.inLexer) {
//            addLexer(varname, vartype, ls.lastidx);
            addLexer(varname, vartype, ls.checknameidx);
        }
        if (var.k == VVOID) {
            if (ls.L.inLexer)
                AluaParser.LexState_globals.add(varname);
            expdesc key = new expdesc();
            singlevaraux(fs, ls.envn, var, 1);
            codestring(ls, key, varname);
            luaK_indexed(fs, var, key);
        }
    }

    // static void adjust_assign(LexState *ls, int nvars, int nexps, expdesc *e) {
    // FuncState *fs = ls->fs;
    // int extra = nvars - nexps;
    // if (hasmultret(e->k)) {
    // extra++; /* includes call itself */
    // if (extra < 0) extra = 0;
    // luaK_setreturns(fs, e, extra); /* last exp. provides the difference */
    // if (extra > 1) luaK_reserveregs(fs, extra - 1);
    // } else {
    // if (e->k != VVOID) luaK_exp2nextreg(fs, e); /* close last expression */
    // if (extra > 0) {
    // int reg = fs->freereg;
    // luaK_reserveregs(fs, extra);
    // luaK_nil(fs, reg, extra);
    // }
    // }
    // if (nexps > nvars)
    // ls->fs->freereg -= nexps - nvars; /* remove extra values */
    // }
    protected static void adjust_assign(LexState ls, int nvars, int nexps, expdesc e) throws LuaError {
        FuncState fs = ls.fs;
        int extra = nvars - nexps;
        if (hasmultret(e.k)) {
            extra++;
            if (extra < 0)
                extra = 0;
            luaK_setreturns(fs, e, extra);
            if (extra > 1)
                luaK_reserveregs(fs, extra - 1);
        } else {
            if (e.k != VVOID)
                luaK_exp2nextreg(fs, e);
            if (extra > 0) {
                int reg = fs.freereg;
                luaK_reserveregs(fs, extra);
                luaK_nil(fs, reg, extra);
            }
        }
        if (nexps > nvars)
            ls.fs.freereg -= nexps - nvars;
    }

    // static void enterlevel(LexState *ls) {
    // lua_State *L = ls->L;
    // ++L->nCcalls;
    // checklimit(ls->fs, L->nCcalls, LUAI_MAXCCALLS, "C levels");
    // }
    protected static void enterlevel(LexState ls) throws LuaError {
        lua_State L = ls.L;
        L.nCcalls++;
        checklimit(ls.fs, L.nCcalls, LUAI_MAXCCALLS, "C调用层级");
    }

    // #define leavelevel(ls) ((ls)->L->nCcalls--)
    protected static void leavelevel(LexState ls) {
        ls.L.nCcalls--;
    }

    // static void closegoto(LexState *ls, int g, Labeldesc *label) {
    // int i;
    // FuncState *fs = ls->fs;
    // Labellist *gl = &ls->dyd->gt;
    // Labeldesc *gt = &gl->arr[g];
    // lua_assert(eqstr(gt->name, label->name));
    // if (gt->nactvar < label->nactvar) {
    // TString *vname = getlocvar(fs, gt->nactvar)->varname;
    // const char *msg = luaO_pushfstring(ls->L,
    // "<goto %s> at line %d jumps into the scope of local '%s'",
    // getstr(gt->name), gt->line, getstr(vname));
    // semerror(ls, msg);
    // }
    // luaK_patchlist(fs, gt->pc, label->pc);
    // /* remove goto from pending list */
    // for (i = g; i < gl->n - 1; i++)
    // gl->arr[i] = gl->arr[i + 1];
    // gl->n--;
    // }
    protected static void closegoto(LexState ls, int g, Labeldesc label) throws LuaError {
        int i;
        FuncState fs = ls.fs;
        Labellist gl = ls.dyd.gt;
        Labeldesc gt = gl.arr[g];
        if (gt.nactvar < label.nactvar) {
            String vname = getlocvar(fs, gt.nactvar).varname;
            String msg = luaO_pushfstring(ls.L,
                    "%d行的<goto %s>跳转到了局部变量%s的作用域中",
                    gt.line, (gt.name), (vname));
            semerror(ls, msg);
        }
        luaK_patchlist(fs, gt.pc, label.pc);
        for (i = g; i < gl.n - 1; i++)
            gl.arr[i] = gl.arr[i + 1];
        gl.n--;
    }

    // void luaK_patchlist(FuncState *fs, int list, int target) {
    // if (target == fs->pc) /* 'target' is current position? */
    // luaK_patchtohere(fs, list); /* add list to pending jumps */
    // else {
    // lua_assert(target < fs->pc);
    // patchlistaux(fs, list, target, NO_REG, target);
    // }
    // }
    protected static void luaK_patchlist(FuncState fs, int list, int target) throws LuaError {
        if (target == fs.pc)
            luaK_patchtohere(fs, list);
        else {
            patchlistaux(fs, list, target, NO_REG, target);
        }
    }

    // static int findlabel(LexState *ls, int g) {
    // int i;
    // BlockCnt *bl = ls->fs->bl;
    // Dyndata *dyd = ls->dyd;
    // Labeldesc *gt = &dyd->gt.arr[g];
    // /* check labels in current block for a match */
    // for (i = bl->firstlabel; i < dyd->label.n; i++) {
    // Labeldesc *lb = &dyd->label.arr[i];
    // if (eqstr(lb->name, gt->name)) { /* correct label? */
    // if (gt->nactvar > lb->nactvar &&
    // (bl->upval || dyd->label.n > bl->firstlabel))
    // luaK_patchclose(ls->fs, gt->pc, lb->nactvar);
    // closegoto(ls, g, lb); /* close it */
    // return 1;
    // }
    // }
    // return 0; /* label not found; cannot close goto */
    // }
    protected static int findlabel(LexState ls, int g) throws LuaError {
        int i;
        BlockCnt bl = ls.fs.bl;
        Dyndata dyd = ls.dyd;
        Labeldesc gt = dyd.gt.arr[g];
        for (i = bl.firstlabel; i < dyd.label.n; i++) {
            Labeldesc lb = dyd.label.arr[i];
            if (eqstr(lb.name, gt.name)) {
                if (gt.nactvar > lb.nactvar &&
                        (bl.upval != 0 || dyd.label.n > bl.firstlabel))
                    luaK_patchclose(ls.fs, gt.pc, lb.nactvar);
                closegoto(ls, g, lb);
                return 1;
            }
        }
        return 0;
    }

    // static int newlabelentry(LexState *ls, Labellist *l, TString *name,
    // int line, int pc) {
    // int n = l->n;
    // luaM_growvector(ls->L, l->arr, n, l->size,
    // Labeldesc, SHRT_MAX, "labels/gotos");
    // l->arr[n].name = name;
    // l->arr[n].line = line;
    // l->arr[n].nactvar = ls->fs->nactvar;
    // l->arr[n].pc = pc;
    // l->n = n + 1;
    // return n;
    // }
    protected static int newlabelentry(LexState ls, Labellist l, String name, int line, int pc) {
        int n = l.n;
        // luaM_growvector(ls->L, l->arr, n, l->size,
        // Labeldesc, SHRT_MAX, "labels/gotos");
        l.arr = luaM_realloc(ls.L, l.arr, l.size, l.size + 1);
        l.size++;
        l.arr[n].name = name;
        l.arr[n].line = line;
        l.arr[n].nactvar = ls.fs.nactvar;
        l.arr[n].pc = pc;
        l.n = n + 1;
        return n;
    }

    // static void findgotos(LexState *ls, Labeldesc *lb) {
    // Labellist *gl = &ls->dyd->gt;
    // int i = ls->fs->bl->firstgoto;
    // while (i < gl->n) {
    // if (eqstr(gl->arr[i].name, lb->name))
    // closegoto(ls, i, lb);
    // else
    // i++;
    // }
    // }
    protected static void findgotos(LexState ls, Labeldesc lb) throws LuaError {
        Labellist gl = ls.dyd.gt;
        int i = ls.fs.bl.firstgoto;
        while (i < gl.n) {
            if (eqstr(gl.arr[i].name, lb.name))
                closegoto(ls, i, lb);
            else
                i++;
        }
    }

    // static void movegotosout(FuncState *fs, BlockCnt *bl) {
    // int i = bl->firstgoto;
    // Labellist *gl = &fs->ls->dyd->gt;
    // /* correct pending gotos to current block and try to close it
    // with visible labels */
    // while (i < gl->n) {
    // Labeldesc *gt = &gl->arr[i];
    // if (gt->nactvar > bl->nactvar) {
    // if (bl->upval)
    // luaK_patchclose(fs, gt->pc, bl->nactvar);
    // gt->nactvar = bl->nactvar;
    // }
    // if (!findlabel(fs->ls, i))
    // i++; /* move to next one */
    // }
    // }
    protected static void movegotosout(FuncState fs, BlockCnt bl) throws LuaError {
        int i = bl.firstgoto;
        Labellist gl = fs.ls.dyd.gt;
        while (i < gl.n) {
            Labeldesc gt = gl.arr[i];
            if (gt.nactvar > bl.nactvar) {
                if (bl.upval != 0)
                    luaK_patchclose(fs, gt.pc, bl.nactvar);
                gt.nactvar = bl.nactvar;
            }
            if (findlabel(fs.ls, i) == 0)
                i++;
        }
    }

    // static void enterblock(FuncState *fs, BlockCnt *bl, lu_byte isloop) {
    // bl->isloop = isloop;
    // bl->nactvar = fs->nactvar;
    // bl->firstlabel = fs->ls->dyd->label.n;
    // bl->firstgoto = fs->ls->dyd->gt.n;
    // bl->upval = 0;
    // bl->previous = fs->bl;
    // fs->bl = bl;
    // lua_assert(fs->freereg == fs->nactvar);
    // }
    protected static void enterblock(FuncState fs, BlockCnt bl, int isloop) {
        bl.isloop = (byte) isloop;
        bl.nactvar = fs.nactvar;
        bl.firstlabel = fs.ls.dyd.label.n;
        bl.firstgoto = fs.ls.dyd.gt.n;
        bl.upval = 0;
        bl.previous = fs.bl;
        fs.bl = bl;
    }

    // static void breaklabel(LexState *ls) {
    // TString *n = luaS_new(ls->L, "break");
    // int l = newlabelentry(ls, &ls->dyd->label, n, 0, ls->fs->pc);
    // findgotos(ls, &ls->dyd->label.arr[l]);
    // }
    protected static void breaklabel(LexState ls) throws LuaError {
        String n = "break";
        int l = newlabelentry(ls, ls.dyd.label, n, 0, ls.fs.pc);
        findgotos(ls, ls.dyd.label.arr[l]);
    }

    // static void continuelabel(LexState *ls) {
    // TString *n = luaS_new(ls->L, "continue");
    // int l = newlabelentry(ls, &ls->dyd->label, n, 0, ls->fs->pc);
    // findgotos(ls, &ls->dyd->label.arr[l]);
    // }
    protected static void continuelabel(LexState ls) throws LuaError {
        String n = "continue";
        int l = newlabelentry(ls, ls.dyd.label, n, 0, ls.fs.pc);
        findgotos(ls, ls.dyd.label.arr[l]);
    }

    // static l_noret undefgoto(LexState *ls, Labeldesc *gt) {
    // const char *msg = isreserved(gt->name)
    // ? "<%s> at line %d not inside a loop"
    // : "no visible label '%s' for <goto> at line %d";
    // msg = luaO_pushfstring(ls->L, msg, getstr(gt->name), gt->line);
    // semerror(ls, msg);
    // }
    protected static void undefgoto(LexState ls, Labeldesc gt) throws LuaError {
        String msg = isreserved(gt.name) >= 0
                ? "第%d行的<%d>不在循环内" : "第%d行<goto>没有可抵达的标签位置'%s'";
        msg = luaO_pushfstring(ls.L, msg, gt.line, gt.name);
        semerror(ls, msg);
    }

    // static void leaveblock(FuncState *fs) {
    // BlockCnt *bl = fs->bl;
    // LexState *ls = fs->ls;
    // if (bl->previous && bl->upval) {
    // /* create a 'jump to here' to close upvalues */
    // int j = luaK_jump(fs);
    // luaK_patchclose(fs, j, bl->nactvar);
    // luaK_patchtohere(fs, j);
    // }
    // if (bl->isloop)
    // breaklabel(ls); /* close pending breaks */
    // fs->bl = bl->previous;
    // removevars(fs, bl->nactvar);
    // lua_assert(bl->nactvar == fs->nactvar);
    // fs->freereg = fs->nactvar; /* free registers */
    // ls->dyd->label.n = bl->firstlabel; /* remove local labels */
    // if (bl->previous) /* inner block? */
    // movegotosout(fs, bl); /* update pending gotos to outer block */
    // else if (bl->firstgoto < ls->dyd->gt.n) /* pending gotos in outer block? */
    // undefgoto(ls, &ls->dyd->gt.arr[bl->firstgoto]); /* error */
    // }
    protected static void leaveblock(FuncState fs) throws LuaError {
        leaveblock(fs, true);
    }

    protected static void leaveblock(FuncState fs, boolean namemode) throws LuaError {
        BlockCnt bl = fs.bl;
        LexState ls = fs.ls;
        if (bl.previous != null && bl.upval != 0) {
            int j = luaK_jump(fs);
            luaK_patchclose(fs, j, bl.nactvar);
            luaK_patchtohere(fs, j);
        }
        if (bl.isloop != 0)
            breaklabel(ls);
        fs.bl = bl.previous;
        removevars(fs, bl.nactvar, namemode);
        fs.freereg = fs.nactvar;
        ls.dyd.label.n = bl.firstlabel;
        if (bl.previous != null)
            movegotosout(fs, bl);
        else if (bl.firstgoto < ls.dyd.gt.n)
            undefgoto(ls, ls.dyd.gt.arr[bl.firstgoto]);
    }


    // static Proto *addprototype(LexState *ls) {
    // Proto *clp;
    // lua_State *L = ls->L;
    // FuncState *fs = ls->fs;
    // Proto *f = fs->f; /* prototype of current function */
    // if (fs->np >= f->sizep) {
    // int oldsize = f->sizep;
    // luaM_growvector(L, f->p, fs->np, f->sizep, Proto *, MAXARG_Bx, "functions");
    // while (oldsize < f->sizep)
    // f->p[oldsize++] = NULL;
    // }
    // f->p[fs->np++] = clp = luaF_newproto(L);
    // luaC_objbarrier(L, f, clp);
    // return clp;
    // }
    protected static Proto addprototype(LexState ls) {
        Proto clp;
        lua_State L = ls.L;
        FuncState fs = ls.fs;
        Proto f = fs.f;
        if (fs.np >= f.sizep) {
            int oldsize = f.sizep;
            f.p = luaM_realloc(L, f.p, f.sizep, f.sizep + 1);
            f.sizep++;
            while (oldsize < f.sizep)
                f.p[oldsize++] = null;
        }
        f.p[fs.np++] = clp = new Proto();
        return clp;
    }

    // static void codeclosure(LexState *ls, expdesc *v) {
    // FuncState *fs = ls->fs->prev;
    // init_exp(v, VRELOCABLE, luaK_codeABx(fs, OP_CLOSURE, 0, fs->np - 1));
    // luaK_exp2nextreg(fs, v); /* fix it at the last register */
    // }
    protected static void codeclosure(LexState ls, expdesc v) throws LuaError {
        FuncState fs = ls.fs.prev;
        init_exp(v, VRELOCABLE, luaK_codeABx(fs, OP_CLOSURE, 0, fs.np - 1));
        luaK_exp2nextreg(fs, v);
    }

    // static void open_func(LexState *ls, FuncState *fs, BlockCnt *bl) {
    // Proto *f;
    // fs->prev = ls->fs; /* linked list of funcstates */
    // fs->ls = ls;
    // ls->fs = fs;
    // fs->pc = 0;
    // fs->lasttarget = 0;
    // fs->jpc = NO_JUMP;
    // fs->freereg = 0;
    // fs->nk = 0;
    // fs->np = 0;
    // fs->nups = 0;
    // fs->nlocvars = 0;
    // fs->nactvar = 0;
    // fs->needclose = 0;
    // fs->firstlocal = ls->dyd->actvar.n;
    // fs->bl = NULL;
    // f = fs->f;
    // f->source = ls->source;
    // f->maxstacksize = 2; /* registers 0/1 are always valid */
    // enterblock(fs, bl, 0);
    // }
    protected static void open_func(LexState ls, FuncState fs, BlockCnt bl) {
        Proto f;
        fs.prev = ls.fs;
        fs.ls = ls;
        ls.fs = fs;
        fs.pc = 0;
        fs.lasttarget = 0;
        fs.jpc = NO_JUMP;
        fs.freereg = 0;
        fs.nk = 0;
        fs.np = 0;
        fs.nups = 0;
        fs.nlocvars = 0;
        fs.nactvar = 0;
        fs.needclose = 0;
        fs.firstlocal = ls.dyd.actvar.n;
        fs.bl = null;
        f = fs.f;
        f.source = ls.source;
        f.maxstacksize = 2;
        enterblock(fs, bl, 0);
    }

    // static void close_func(LexState *ls) {
    // lua_State *L = ls->L;
    // FuncState *fs = ls->fs;
    // Proto *f = fs->f;
    // luaK_ret(fs, 0, 0); /* final return */
    // leaveblock(fs);
    // luaM_reallocvector(L, f->code, f->sizecode, fs->pc, Instruction);
    // f->sizecode = fs->pc;
    // luaM_reallocvector(L, f->lineinfo, f->sizelineinfo, fs->pc, int);
    // f->sizelineinfo = fs->pc;
    // luaM_reallocvector(L, f->k, f->sizek, fs->nk, TValue);
    // f->sizek = fs->nk;
    // luaM_reallocvector(L, f->p, f->sizep, fs->np, Proto *);
    // f->sizep = fs->np;
    // luaM_reallocvector(L, f->locvars, f->sizelocvars, fs->nlocvars, LocVar);
    // f->sizelocvars = fs->nlocvars;
    // luaM_reallocvector(L, f->upvalues, f->sizeupvalues, fs->nups, Upvaldesc);
    // f->sizeupvalues = fs->nups;
    // lua_assert(fs->bl == NULL);
    // ls->fs = fs->prev;
    // luaC_checkGC(L);
    // }
    protected static void close_func(LexState ls) throws LuaError {
        close_func(ls, true);
    }

    protected static void close_func(LexState ls, boolean namemode) throws LuaError {
        lua_State L = ls.L;
        FuncState fs = ls.fs;
        Proto f = fs.f;
        luaK_ret(fs, 0, 0);
        leaveblock(fs, namemode);
        f.code = luaM_realloc(L, f.code, f.sizecode, fs.pc);
        f.sizecode = fs.pc;
        f.lineinfo = luaM_realloc(L, f.lineinfo, f.sizelineinfo, fs.pc);
        f.sizelineinfo = fs.pc;
        f.k = luaM_realloc(L, f.k, f.sizek, fs.nk);
        f.sizek = fs.nk;
        f.p = luaM_realloc(L, f.p, f.sizep, fs.np);
        f.sizep = fs.np;
        f.locvars = luaM_realloc(L, f.locvars, f.sizelocvars, fs.nlocvars);
        f.sizelocvars = fs.nlocvars;
        f.upvalues = luaM_realloc(L, f.upvalues, f.sizeupvalues, fs.nups);
        f.sizeupvalues = fs.nups;
        ls.fs = fs.prev;
    }

    // static int block_follow(LexState *ls, int withuntil) {
    // switch (ls->t.token) {
    // case TK_ELSE:
    // case TK_ELSEIF:
    // case TK_END:
    // case TK_EOS:
    // case TK_CASE:
    // case TK_DEFAULT:
    // case '}':
    // return 1;
    // case TK_UNTIL:
    // return withuntil;
    // default:
    // return 0;
    // }
    // }
    protected static boolean block_follow(LexState ls, int withuntil) {
        return block_follow(ls, withuntil != 0);
    }

    protected static boolean block_follow(LexState ls, boolean withuntil) {
        switch (ls.t.token) {
            case TK_ELSE:
            case TK_ELSEIF:
            case TK_END:
            case TK_EOS:
            case TK_CASE:
            case TK_DEFAULT:
            case '}':
                return true;
            case TK_UNTIL:
                return withuntil;
            default:
                return false;
        }
    }

    // static void statlist(LexState *ls) {
    // /* statlist -> { stat [';'] } */
    // while (!block_follow(ls, 1)) {
    // if (ls->t.token == TK_CATCH || ls->t.token == TK_FINALLY)//mod by nwdxlgzs
    // return;
    // if (ls->t.token == TK_RETURN) {
    // statement(ls);
    // return; /* 'return' must be last statement */
    // }
    // statement(ls);
    // }
    // }
    protected static void statlist(LexState ls) throws LuaError {
        while (!block_follow(ls, true)) {
            if (ls.L.abort.isSet()) {
                AluaParser.LexState_errormsg = null;
                return;
            }
            if (ls.t.token == TK_CATCH || ls.t.token == TK_FINALLY)
                return;
            if (ls.t.token == TK_RETURN) {
                statement(ls);
                return;
            }
            statement(ls);
        }
    }

    // static void fieldsel(LexState *ls, expdesc *v) {
    // /* fieldsel -> ['.' | ':'] NAME */
    // FuncState *fs = ls->fs;
    // expdesc key;
    // luaK_exp2anyregup(fs, v);
    // luaX_next(ls); /* skip the dot or colon */
    // checkname(ls, &key);
    // luaK_indexed(fs, v, &key);
    // }
    protected static void fieldsel(LexState ls, expdesc v) throws LuaError {
        FuncState fs = ls.fs;
        expdesc key = new expdesc();
        luaK_exp2anyregup(fs, v);
        luaX_next(ls);
        checkname(ls, key);
        luaK_indexed(fs, v, key);
    }

    // static void yindex(LexState *ls, expdesc *v) {
    // /* index -> '[' expr ']' */
    // luaX_next(ls); /* skip the '[' */
    // expr(ls, v);
    // luaK_exp2val(ls->fs, v);
    // checknext(ls, ']');
    // }
    protected static void yindex(LexState ls, expdesc v) throws LuaError {
        luaX_next(ls);
        expr(ls, v);
        luaK_exp2val(ls.fs, v);
        checknext(ls, ']');
    }

    // static TString *str_check(LexState *ls) {
    // TString *ts;
    // check(ls, TK_STRING);
    // ts = ls->t.seminfo.ts;
    // luaX_next(ls);
    // return ts;
    // }
    protected static String str_check(LexState ls) throws LuaError {
        String ts;
        check(ls, TK_STRING);
        ts = ls.t.seminfo.ts;
        luaX_next(ls);
        return ts;
    }

    // static void checkstring(LexState *ls, expdesc *e) {
    // TString *str = str_check(ls);
    // codestring(ls, e, str);
    // }
    protected static void checkstring(LexState ls, expdesc e) throws LuaError {
        String str = str_check(ls);
        codestring(ls, e, str);
    }

    protected static class ConsControl {
        protected ConsControl() {
        }

        // struct ConsControl {
        // expdesc v; /* last list item read */
        // expdesc *t; /* table descriptor */
        // int nh; /* total number of 'record' elements */
        // int na; /* total number of array elements */
        // int tostore; /* number of array elements pending to be stored */
        // };
        protected expdesc v = new expdesc();
        protected expdesc t = null;
        protected int nh = 0;
        protected int na = 0;
        protected int tostore = 0;
    }

    // static void recfield(LexState *ls, struct ConsControl *cc) {
    // /* recfield -> (NAME | '['exp1']') = exp1 */
    // FuncState *fs = ls->fs;
    // int reg = ls->fs->freereg;
    // expdesc key, val;
    // int rkkey;
    // int isstr = 0;
    // int isfunc = 0;
    // if (ls->t.token == TK_FUNCTION) {
    // isfunc = 1;
    // checklimit(fs, cc->nh, MAX_INT, "items in a constructor");
    // luaX_next(ls);
    // checkname(ls, &key);
    // } else if (ls->t.token == TK_STRING) {
    // isstr = 1;
    // checklimit(fs, cc->nh, MAX_INT, "items in a constructor");
    // checkstring(ls, &key);
    // } else if (ls->t.token == TK_INT) {
    // isstr = 1;
    // checklimit(fs, cc->nh, MAX_INT, "items in a constructor");
    // init_exp(&key, VKINT, 0);
    // key.u.nval = ls->t.seminfo.r;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // checklimit(fs, cc->nh, MAX_INT, "items in a constructor");
    // checkname(ls, &key);
    // } else /* ls->t.token == '[' */
    // yindex(ls, &key);
    // cc->nh++;
    // if (!isfunc && (!isstr || !testnext(ls, ':')))
    // checknext(ls, '=');
    // rkkey = luaK_exp2RK(fs, &key);
    // if (isfunc)
    // body(ls, &val, 0, ls->linenumber);
    // else
    // expr(ls, &val);
    // luaK_codeABC(fs, OP_SETTABLE, cc->t->u.info, rkkey, luaK_exp2RK(fs, &val));
    // fs->freereg = reg; /* free registers */
    // }
    protected static void recfield(LexState ls, ConsControl cc) throws LuaError {
        FuncState fs = ls.fs;
        int reg = ls.fs.freereg;
        expdesc key = new expdesc();
        expdesc val = new expdesc();
        int rkkey;
        int isstr = 0;
        int isfunc = 0;
        if (ls.t.token == TK_FUNCTION) {
            isfunc = 1;
            checklimit(fs, cc.nh, MAX_INT, "构造函数中的项");
            luaX_next(ls);
            checkname(ls, key);
        } else if (ls.t.token == TK_STRING) {
            isstr = 1;
            checklimit(fs, cc.nh, MAX_INT, "构造函数中的项");
            checkstring(ls, key);
        } else if (ls.t.token == TK_INT) {
            isstr = 1;
            checklimit(fs, cc.nh, MAX_INT, "构造函数中的项");
            init_exp(key, VKINT, 0);
            key.u.nval = ls.t.seminfo.r;
            luaX_next(ls);
        } else if (ls.t.token == TK_NAME) {
            checklimit(fs, cc.nh, MAX_INT, "构造函数中的项");
            checkname(ls, key);
        } else
            yindex(ls, key);
        cc.nh++;
        if (isfunc == 0 && (isstr == 0 || !testnext(ls, ':')))
            checknext(ls, '=');
        rkkey = luaK_exp2RK(fs, key);
        if (isfunc != 0)
            body(ls, val, 0, ls.linenumber);
        else
            expr(ls, val);
        luaK_codeABC(fs, OP_SETTABLE, cc.t.u.info, rkkey, luaK_exp2RK(fs, val));
        fs.freereg = reg;
    }

    // static void closelistfield(FuncState *fs, struct ConsControl *cc) {
    // if (cc->v.k == VVOID) return; /* there is no list item */
    // luaK_exp2nextreg(fs, &cc->v);
    // cc->v.k = VVOID;
    // if (cc->tostore == LFIELDS_PER_FLUSH) {
    // luaK_setlist(fs, cc->t->u.info, cc->na, cc->tostore); /* flush */
    // cc->tostore = 0; /* no more items pending */
    // }
    // }
    protected static void closelistfield(FuncState fs, ConsControl cc) throws LuaError {
        if (cc.v.k == VVOID)
            return;
        luaK_exp2nextreg(fs, cc.v);
        cc.v.k = VVOID;
        if (cc.tostore == LFIELDS_PER_FLUSH) {
            luaK_setlist(fs, cc.t.u.info, cc.na, cc.tostore);
            cc.tostore = 0;
        }
    }

    // static void lastlistfield(FuncState *fs, struct ConsControl *cc) {
    // if (cc->tostore == 0) return;
    // if (hasmultret(cc->v.k)) {
    // luaK_setmultret(fs, &cc->v);
    // luaK_setlist(fs, cc->t->u.info, cc->na, LUA_MULTRET);
    // cc->na--; /* do not count last expression (unknown number of elements) */
    // } else {
    // if (cc->v.k != VVOID)
    // luaK_exp2nextreg(fs, &cc->v);
    // luaK_setlist(fs, cc->t->u.info, cc->na, cc->tostore);
    // }
    // }
    protected static void lastlistfield(FuncState fs, ConsControl cc) throws LuaError {
        if (cc.tostore == 0)
            return;
        if (hasmultret(cc.v.k)) {
            luaK_setmultret(fs, cc.v);
            luaK_setlist(fs, cc.t.u.info, cc.na, LUA_MULTRET);
            cc.na--;
        } else {
            if (cc.v.k != VVOID)
                luaK_exp2nextreg(fs, cc.v);
            luaK_setlist(fs, cc.t.u.info, cc.na, cc.tostore);
        }
    }

    // static void listfield(LexState *ls, struct ConsControl *cc) {
    // /* listfield -> exp */
    // expr(ls, &cc->v);
    // checklimit(ls->fs, cc->na, MAX_INT, "items in a constructor");
    // cc->na++;
    // cc->tostore++;
    // }
    protected static void listfield(LexState ls, ConsControl cc) throws LuaError {
        expr(ls, cc.v);
        checklimit(ls.fs, cc.na, MAX_INT, "构造函数中的项");
        cc.na++;
        cc.tostore++;
    }

    // static void field(LexState *ls, struct ConsControl *cc) {
    // /* field -> listfield | recfield */
    // switch (ls->t.token) {
    // case TK_STRING: {
    // int token = luaX_lookahead(ls);
    // if (token != '=' && token != ':') /* expression? */
    // listfield(ls, cc);
    // else
    // recfield(ls, cc);
    // break;
    // }
    // case TK_FUNCTION: {
    // int token = luaX_lookahead(ls);
    // if (token != TK_NAME) /* expression? */
    // listfield(ls, cc);
    // else
    // recfield(ls, cc);
    // break;
    // }
    // case TK_INT: { /* may be 'listfield' or 'recfield' */
    // if (luaX_lookahead(ls) != '=') /* expression? */
    // listfield(ls, cc);
    // else
    // recfield(ls, cc);
    // break;
    // }
    // case TK_NAME: { /* may be 'listfield' or 'recfield' */
    // if (luaX_lookahead(ls) != '=') /* expression? */
    // listfield(ls, cc);
    // else
    // recfield(ls, cc);
    // break;
    // }
    // case '[': {
    // recfield(ls, cc);
    // break;
    // }
    // default: {
    // listfield(ls, cc);
    // break;
    // }
    // }
    // }
    protected static void field(LexState ls, ConsControl cc) throws LuaError {
        switch (ls.t.token) {
            case TK_STRING: {
                int token = luaX_lookahead(ls);
                if (token != '=' && token != ':')
                    listfield(ls, cc);
                else
                    recfield(ls, cc);
                break;
            }
            case TK_FUNCTION: {
                int token = luaX_lookahead(ls);
                if (token != TK_NAME)
                    listfield(ls, cc);
                else
                    recfield(ls, cc);
                break;
            }
            case TK_INT: {
                if (luaX_lookahead(ls) != '=')
                    listfield(ls, cc);
                else
                    recfield(ls, cc);
                break;
            }
            case TK_NAME: {
                if (luaX_lookahead(ls) != '=')
                    listfield(ls, cc);
                else
                    recfield(ls, cc);
                break;
            }
            case '[': {
                recfield(ls, cc);
                break;
            }
            default: {
                listfield(ls, cc);
                break;
            }
        }
    }

    // static void constructor(LexState *ls, expdesc *t) {
    // /* constructor -> '{' [ field { sep field } [sep] ] '}'
    // sep -> ',' | ';' */
    // FuncState *fs = ls->fs;
    // int line = ls->linenumber;
    // int pc = luaK_codeABC(fs, OP_NEWTABLE, 0, 0, 0);
    // struct ConsControl cc;
    // cc.na = cc.nh = cc.tostore = 0;
    // cc.t = t;
    // init_exp(t, VRELOCABLE, pc);
    // init_exp(&cc.v, VVOID, 0); /* no value (yet) */
    // luaK_exp2nextreg(ls->fs, t); /* fix it at stack top */
    // checknext(ls, '{');
    // do {
    // lua_assert(cc.v.k == VVOID || cc.tostore > 0);
    // if (ls->t.token == '}') break;
    // closelistfield(fs, &cc);
    // field(ls, &cc);
    // } while (testnext(ls, ',') || testnext(ls, ';'));
    // check_match(ls, '}', '{', line);
    // lastlistfield(fs, &cc);
    // SETARG_B(fs->f->code[pc], luaO_int2fb(cc.na)); /* set initial array size */
    // SETARG_C(fs->f->code[pc], luaO_int2fb(cc.nh)); /* set initial table size */
    // }
    protected static void constructor(LexState ls, expdesc t) throws LuaError {
        FuncState fs = ls.fs;
        int startidx = ls.currentidx;
        int line = ls.linenumber;
        int pc = luaK_codeABC(fs, OP_NEWTABLE, 0, 0, 0);
        ConsControl cc = new ConsControl();
        cc.na = cc.nh = cc.tostore = 0;
        cc.t = t;
        init_exp(t, VRELOCABLE, pc);
        init_exp(cc.v, VVOID, 0);
        luaK_exp2nextreg(ls.fs, t);
        checknext(ls, '{');
        do {
            if (ls.t.token == '}')
                break;
            closelistfield(fs, cc);
            field(ls, cc);
        } while (testnext(ls, ',') || testnext(ls, ';'));
        if (ls.L.inLexer) {
            AluaParser.LexState_lines.add(new Rect(startidx, line, ls.currentidx - 1, ls.linenumber));
        }
        check_match(ls, '}', '{', line);
        lastlistfield(fs, cc);
        SETARG_B(fs.f.code[pc], luaO_int2fb(cc.na));
        SETARG_C(fs.f.code[pc], luaO_int2fb(cc.nh));
    }

    // static void constructor2(LexState *ls, expdesc *t) {
    // /* constructor -> '{' [ field { sep field } [sep] ] '}'
    // sep -> ',' | ';' */
    // FuncState *fs = ls->fs;
    // int line = ls->linenumber;
    // int pc = luaK_codeABC(fs, OP_NEWARRAY, 0, 0, 0);
    // struct ConsControl cc;
    // cc.na = cc.nh = cc.tostore = 0;
    // cc.t = t;
    // init_exp(t, VRELOCABLE, pc);
    // init_exp(&cc.v, VVOID, 0); /* no value (yet) */
    // luaK_exp2nextreg(ls->fs, t); /* fix it at stack top */
    // checknext(ls, '[');
    // do {
    // lua_assert(cc.v.k == VVOID || cc.tostore > 0);
    // if (ls->t.token == ']') break;
    // closelistfield(fs, &cc);
    // listfield(ls, &cc);
    // } while (testnext(ls, ',') || testnext(ls, ';'));
    // check_match(ls, ']', '[', line);
    // lastlistfield(fs, &cc);
    // SETARG_B(fs->f->code[pc], luaO_int2fb(cc.na)); /* set initial array size */
    //// SETARG_C(fs->f->code[pc], luaO_int2fb(-1)); /* set initial table size */
    // }
    protected static void constructor2(LexState ls, expdesc t) throws LuaError {
        FuncState fs = ls.fs;
        int line = ls.linenumber;
        int pc = luaK_codeABC(fs, OP_NEWARRAY, 0, 0, 0);
        ConsControl cc = new ConsControl();
        cc.na = cc.nh = cc.tostore = 0;
        cc.t = t;
        init_exp(t, VRELOCABLE, pc);
        init_exp(cc.v, VVOID, 0);
        luaK_exp2nextreg(ls.fs, t);
        checknext(ls, '[');
        do {
            if (ls.t.token == ']')
                break;
            closelistfield(fs, cc);
            listfield(ls, cc);
        } while (testnext(ls, ',') || testnext(ls, ';'));
        check_match(ls, ']', '[', line);
        lastlistfield(fs, cc);
        SETARG_B(fs.f.code[pc], luaO_int2fb(cc.na));
    }

    protected static class default_arg {
        protected default_arg() {
        }

        // #pragma pack(push, 1)//省一点空间，无关紧要就是了
        // typedef struct default_arg {
        // lu_byte active: 4;
        // lu_byte emptytable: 4;
        // lu_byte n;
        // expdesc e;
        // } default_arg;
        // #pragma pack(pop)
        protected byte active = 0;
        protected byte emptytable = 0;
        protected byte n = 0;
        protected expdesc e = new expdesc();
    }

    // static void simpleexp_default(LexState *ls, default_arg *arg) {
    // /* simpleexp_default -> FLT | INT | STRING | NIL | TRUE | FALSE | {} | [] |
    // NAME */
    // expdesc *v = &arg->e;
    // switch (ls->t.token) {
    // case TK_FLT: {
    // init_exp(v, VKFLT, 0);
    // v->u.nval = ls->t.seminfo.r;
    // break;
    // }
    // case TK_INT: {
    // init_exp(v, VKINT, 0);
    // v->u.ival = ls->t.seminfo.i;
    // break;
    // }
    // case TK_STRING: {
    // codestring(ls, v, ls->t.seminfo.ts);
    // }
    // case TK_NIL: {
    // init_exp(v, VNIL, 0);
    // break;
    // }
    // case TK_TRUE: {
    // init_exp(v, VTRUE, 0);
    // break;
    // }
    // case TK_FALSE: {
    // init_exp(v, VFALSE, 0);
    // break;
    // }
    // case '{': {//很显然，执行这里时函数还没构建好，所以只支持空表
    // luaX_next(ls);
    // check(ls, '}');
    // init_exp(v, VRELOCABLE, 0);
    // arg->emptytable = 1;
    // break;
    // }
    // case '[': {//很显然，执行这里时函数还没构建好，所以只支持空表
    // luaX_next(ls);
    // check(ls, ']');
    // init_exp(v, VRELOCABLE, 0);
    // arg->emptytable = 2;
    // break;
    // }
    // case TK_NAME: {
    // singlevar(ls, v);
    // break;
    // }
    // default: {
    // luaX_syntaxerror(ls,
    // "defalut argument only support <number>, <string>, <nil>, <boolean>, {}, [],
    // NAME");
    // return;
    // }
    // }
    // luaX_next(ls);
    // }
    protected static void simpleexp_default(LexState ls, default_arg arg) throws LuaError {
        expdesc v = arg.e;
        switch (ls.t.token) {
            case TK_FLT: {
                init_exp(v, VKFLT, 0);
                v.u.nval = ls.t.seminfo.r;
                break;
            }
            case TK_INT: {
                init_exp(v, VKINT, 0);
                v.u.ival = ls.t.seminfo.i;
                break;
            }
            case TK_STRING: {
                codestring(ls, v, ls.t.seminfo.ts);
            }
            case TK_NIL: {
                init_exp(v, VNIL, 0);
                break;
            }
            case TK_TRUE: {
                init_exp(v, VTRUE, 0);
                break;
            }
            case TK_FALSE: {
                init_exp(v, VFALSE, 0);
                break;
            }
            case '{': {
                luaX_next(ls);
                check(ls, '}');
                init_exp(v, VRELOCABLE, 0);
                arg.emptytable = 1;
                break;
            }
            case '[': {
                luaX_next(ls);
                check(ls, ']');
                init_exp(v, VRELOCABLE, 0);
                arg.emptytable = 2;
                break;
            }
            case TK_NAME: {
                singlevar(ls, v);
                break;
            }
            default: {
                luaX_syntaxerror(ls,
                        "默认形参参数只支持：数字、字符串、空值、布尔值、空表（{}或者[]）、变量名");
                return;
            }
        }
        luaX_next(ls);
    }

    // /*
    // * TAG只负责为编辑器与开发提供标准，基本上不参与编译，编译器要做到就是吃掉
    // */
    // static void eatTag(LexState *ls, int muti, int lamb) {
    // //mod by nwdxlgzs
    // if (testnext(ls, '<')) {
    // str_checkname(ls);
    // if (muti) {
    // while (testnext(ls, ',')) {
    // str_checkname(ls);
    // }
    // }
    // checknext(ls, '>');
    // } else if (!lamb && testnext(ls, ':')) {
    // str_checkname(ls);
    // if (muti) {
    // while (testnext(ls, ',')) {
    // str_checkname(ls);
    // }
    // }
    // }
    // }
    protected static void eatTag(LexState ls, boolean muti) throws LuaError {
        eatTag(ls, muti, false);
    }

    protected static void eatTag(LexState ls, boolean muti, boolean lamb) throws LuaError {
        if (testnext(ls, '<')) {
            str_checkname(ls);
            if (muti) {
                while (testnext(ls, ',')) {
                    str_checkname(ls);
                }
            }
            checknext(ls, '>');
        } else if (!lamb && testnext(ls, ':')) {
            str_checkname(ls);
            if (muti) {
                while (testnext(ls, ',')) {
                    str_checkname(ls);
                }
            }
        }
    }

    // static void parlist(LexState *ls, default_arg *Dargs) {
    // /* parlist -> [ param { ',' param } ] */
    // FuncState *fs = ls->fs;
    // Proto *f = fs->f;
    // int nparams = 0;
    // f->is_vararg = 0;
    // if (ls->t.token != ')') { /* is 'parlist' not empty? */
    // do {
    // switch (ls->t.token) {
    // case TK_NAME: { /* param -> NAME */
    // new_localvar(ls, str_checkname(ls));
    // nparams++;
    // //mod by nwdxlgzs
    // eatTag(ls, 0);
    // if (Dargs) {
    // if (testnext(ls, '=')) {
    // simpleexp_default(ls, &Dargs[nparams - 1]);
    // Dargs[nparams - 1].n = nparams;
    // Dargs[nparams - 1].active = 1;
    // }
    // }
    // break;
    // }
    // case TK_DOTS: { /* param -> '...' */
    // luaX_next(ls);
    // f->is_vararg = 2; /* declared vararg */
    // //mod by nwdxlgzs
    // eatTag(ls, 1);
    // break;
    // }
    // default:
    // luaX_syntaxerror(ls, "<name> or '...' expected");
    // }
    // } while (!f->is_vararg && testnext(ls, ','));
    // }
    // adjustlocalvars(ls, nparams);
    // f->numparams = cast_byte(fs->nactvar);
    // luaK_reserveregs(fs, fs->nactvar); /* reserve register for parameters */
    // }
    protected static void parlist(LexState ls, default_arg[] Dargs) throws LuaError {
        FuncState fs = ls.fs;
        Proto f = fs.f;
        int nparams = 0;
        f.is_vararg = 0;
        if (ls.t.token != ')') {
            do {
                switch (ls.t.token) {
                    case TK_NAME: {
                        new_localvar(ls, str_checkname(ls));
                        nparams++;
                        eatTag(ls, false);
                        if (Dargs != null) {
                            if (testnext(ls, '=')) {
                                simpleexp_default(ls, Dargs[nparams - 1]);
                                Dargs[nparams - 1].n = (byte) nparams;
                                Dargs[nparams - 1].active = 1;
                            }
                        }
                        break;
                    }
                    case TK_DOTS: {
                        luaX_next(ls);
                        f.is_vararg = 2;
                        eatTag(ls, true);
                        break;
                    }
                    default:
                        luaX_syntaxerror(ls, "期待形参名字或者'...'");
                }
            } while (f.is_vararg == 0 && testnext(ls, ','));
        }
        adjustlocalvars(ls, nparams);
        f.numparams = (byte) fs.nactvar;
        luaK_reserveregs(fs, fs.nactvar);
    }

    // static void fix_Dargs(LexState *ls, default_arg *Dargs, int size) {
    // //完成缺省参数的赋值
    // for (int i = 0; i < size; i++) {
    // if (Dargs[i].active) {
    // int topreg = ls->fs->freereg++;
    // luaK_nil(ls->fs, topreg, 1);
    // luaK_reserveregs(ls->fs, 1);
    // luaK_codeABC(ls->fs, OP_EQ, 0, topreg, Dargs[i].n - 1);
    // int j = luaK_jump(ls->fs);
    // if (Dargs[i].emptytable) {
    // int reg = ls->fs->freereg++;
    // if (1 == Dargs[i].emptytable) {
    // luaK_codeABC(ls->fs, OP_NEWTABLE, reg, 0, 0);
    // } else {
    // luaK_codeABC(ls->fs, OP_NEWARRAY, reg, 0, 0);
    // }
    // luaK_codeABC(ls->fs, OP_MOVE, 0, reg, Dargs[i].n - 1);
    // ls->fs->freereg--;
    // } else {
    // luaK_exp2nextreg(ls->fs, &Dargs[i].e);
    // luaK_codeABC(ls->fs, OP_MOVE, Dargs[i].n - 1, ls->fs->freereg - 1, 0);
    // }
    // ls->fs->freereg--;
    // luaK_patchtohere(ls->fs, j);
    // }
    // }
    // }
    protected static void fix_Dargs(LexState ls, default_arg[] Dargs, int size) throws LuaError {
        for (int i = 0; i < size; i++) {
            if (Dargs[i].active != 0) {
                int topreg = ls.fs.freereg++;
                luaK_nil(ls.fs, topreg, 1);
                luaK_reserveregs(ls.fs, 1);
                luaK_codeABC(ls.fs, OP_EQ, 0, topreg, Dargs[i].n - 1);
                int j = luaK_jump(ls.fs);
                if (Dargs[i].emptytable != 0) {
                    int reg = ls.fs.freereg++;
                    if (1 == Dargs[i].emptytable) {
                        luaK_codeABC(ls.fs, OP_NEWTABLE, reg, 0, 0);
                    } else {
                        luaK_codeABC(ls.fs, OP_NEWARRAY, reg, 0, 0);
                    }
                    luaK_codeABC(ls.fs, OP_MOVE, 0, reg, Dargs[i].n - 1);
                    ls.fs.freereg--;
                } else {
                    luaK_exp2nextreg(ls.fs, Dargs[i].e);
                    luaK_codeABC(ls.fs, OP_MOVE, Dargs[i].n - 1, ls.fs.freereg - 1, 0);
                }
                ls.fs.freereg--;
                luaK_patchtohere(ls.fs, j);
            }
        }
    }

    // static void body(LexState *ls, expdesc *e, int ismethod, int line) {
    // /* body -> '(' parlist ')' block END */
    // FuncState new_fs;
    // BlockCnt bl;
    // new_fs.f = addprototype(ls);
    // new_fs.f->linedefined = line;
    // open_func(ls, &new_fs, &bl);
    // int b = testnext(ls, '(');
    // //checknext(ls, '(');
    // if (ismethod) {
    // new_localvarliteral(ls, "self"); /* create 'self' parameter */
    // adjustlocalvars(ls, 1);
    // }
    // default_arg Dargs[256] = {0};
    // if (b == 1) {
    // parlist(ls, Dargs);
    // checknext(ls, ')');
    // }
    // //parlist(ls);
    // //checknext(ls, ')');
    // b = testnext(ls, '{');
    //
    // new_localvar(ls, ls->envn);
    // expdesc env;
    // singlevaraux(ls->fs, ls->envn, &env, 1);
    // adjust_assign(ls, 1, 1, &env);
    // adjustlocalvars(ls, 1);
    // //mod by nwdxlgzs
    // fix_Dargs(ls, Dargs, sizeof(Dargs) / sizeof(Dargs[0]));
    // statlist(ls);
    // new_fs.f->lastlinedefined = ls->linenumber;
    // if (b)
    // check_match(ls, '}', TK_FUNCTION, line);
    // else
    // check_match(ls, TK_END, TK_FUNCTION, line);
    // codeclosure(ls, e);
    // close_func(ls);
    // }
    protected static void body(LexState ls, expdesc e, int ismethod, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        FuncState new_fs = new FuncState();
        BlockCnt bl = new BlockCnt();
        new_fs.f = addprototype(ls);
        new_fs.f.linedefined = line;
        new_fs.f.startidx = ls.currentidx;
        open_func(ls, new_fs, bl);
        boolean b = testnext(ls, '(');
        if (ismethod != 0) {
            new_localvarliteral(ls, "self");
            adjustlocalvars(ls, 1);
        }
        default_arg[] Dargs = new default_arg[256];
        for (int i = 0; i < Dargs.length; i++) {
            Dargs[i] = new default_arg();
        }
        if (b) {
            parlist(ls, Dargs);
            checknext(ls, ')');
        }
        b = testnext(ls, '{');
        new_localvar(ls, ls.envn);
        expdesc env = new expdesc();
        singlevaraux(ls.fs, ls.envn, env, 1);
        adjust_assign(ls, 1, 1, env);
        adjustlocalvars(ls, 1);
        fix_Dargs(ls, Dargs, Dargs.length);
        statlist(ls);
        new_fs.f.endidx = ls.currentidx;
        new_fs.f.lastlinedefined = ls.linenumber;
        if (b) {
            if (ls.L.inLexer) {
                AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 1, ls.linenumber));
            }
            check_match(ls, '}', TK_FUNCTION, line);
        } else {
            if (ls.L.inLexer) {
                AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
            }
            check_match(ls, TK_END, TK_FUNCTION, line);
        }
        codeclosure(ls, e);
        close_func(ls, !b);
    }

    // static void lambda_parlist(LexState *ls, default_arg *Dargs) {
    // /* lambda_parlist -> '(' [ param { ',' param } ] ')' */
    // /* lambda_parlist -> [ param { ',' param } ] */
    // if (testnext(ls, '(')) {
    // //mod by nwdxlgzs
    // parlist(ls, Dargs);
    // checknext(ls, ')');
    // return;
    // }
    // FuncState *fs = ls->fs;
    // Proto *f = fs->f;
    // int nparams = 0;
    // f->is_vararg = 0;
    // if (ls->t.token == TK_NAME || ls->t.token == TK_DOTS) {
    // do {
    // switch (ls->t.token) {
    // case TK_NAME: { /* param -> NAME */
    // new_localvar(ls, str_checkname(ls));
    // nparams++;
    // //mod by nwdxlgzs
    // eatTag(ls, 0);
    // if (Dargs) {
    // if (testnext(ls, '=')) {
    // simpleexp_default(ls, &Dargs[nparams - 1]);
    // Dargs[nparams - 1].n = nparams;
    // Dargs[nparams - 1].active = 1;
    // }
    // }
    // break;
    // }
    // case TK_DOTS: { /* param -> '...' */
    // luaX_next(ls);
    // f->is_vararg = 1;
    // //mod by nwdxlgzs
    // eatTag(ls, 1);
    // break;
    // }
    // default:
    // luaX_syntaxerror(ls, "<name> or '...' expected");
    // }
    // } while (!f->is_vararg && testnext(ls, ','));
    // }
    // adjustlocalvars(ls, nparams);
    // f->numparams = cast_byte(fs->nactvar);
    // luaK_reserveregs(fs, fs->nactvar); /* reserve register for parameters */
    // }
    protected static void lambda_parlist(LexState ls, default_arg[] Dargs) throws LuaError {
        if (testnext(ls, '(')) {
            parlist(ls, Dargs);
            checknext(ls, ')');
            return;
        }
        FuncState fs = ls.fs;
        Proto f = fs.f;
        int nparams = 0;
        f.is_vararg = 0;
        if (ls.t.token == TK_NAME || ls.t.token == TK_DOTS) {
            do {
                switch (ls.t.token) {
                    case TK_NAME: {
                        new_localvar(ls, str_checkname(ls));
                        nparams++;
                        eatTag(ls, false, true);
                        if (Dargs != null) {
                            if (testnext(ls, '=')) {
                                simpleexp_default(ls, Dargs[nparams - 1]);
                                Dargs[nparams - 1].n = (byte) nparams;
                                Dargs[nparams - 1].active = 1;
                            }
                        }
                        break;
                    }
                    case TK_DOTS: {
                        luaX_next(ls);
                        f.is_vararg = 1;
                        eatTag(ls, true, true);
                        break;
                    }
                    default:
                        luaX_syntaxerror(ls, "期待形参名字或者'...'");
                }
            } while (f.is_vararg == 0 && testnext(ls, ','));
        }
        adjustlocalvars(ls, nparams);
        f.numparams = (byte) fs.nactvar;
        luaK_reserveregs(fs, fs.nactvar);
    }

    // static void lambda_body(LexState *ls, expdesc *e, int line) {
    // /* lambda_body -> lambda_parlist -> explist */
    // /* lambda_body -> lambda_parlist [ '=>' ] stat */
    // FuncState new_fs;
    // BlockCnt bl;
    // new_fs.f = addprototype(ls);
    // new_fs.f->linedefined = line;
    // open_func(ls, &new_fs, &bl);
    // default_arg Dargs[256] = {0};
    // lambda_parlist(ls, Dargs);
    // if (testnext(ls, TK_LET) || testnext(ls, ':')) {
    // enterlevel(ls);
    // //mod by nwdxlgzs
    // fix_Dargs(ls, Dargs, sizeof(Dargs) / sizeof(Dargs[0]));
    // retstat(ls);
    // lua_assert(ls->fs->f->maxstacksize >= ls->fs->freereg &&
    // ls->fs->freereg >= ls->fs->nactvar);
    // ls->fs->freereg = ls->fs->nactvar; /* free registers */
    // leavelevel(ls);
    // } else {
    // //mod by nwdxlgzs
    // enterlevel(ls);
    // fix_Dargs(ls, Dargs, sizeof(Dargs) / sizeof(Dargs[0]));
    // statement(ls);
    // leavelevel(ls);
    // }
    // new_fs.f->lastlinedefined = ls->linenumber;
    // codeclosure(ls, e);
    // close_func(ls);
    // }
    protected static void lambda_body(LexState ls, expdesc e, int line) throws LuaError {
        FuncState new_fs = new FuncState();
        BlockCnt bl = new BlockCnt();
        new_fs.f = addprototype(ls);
        new_fs.f.linedefined = line;
        new_fs.f.startidx = ls.currentidx;
        open_func(ls, new_fs, bl);
        default_arg[] Dargs = new default_arg[256];
        for (int i = 0; i < Dargs.length; i++) {
            Dargs[i] = new default_arg();
        }
        lambda_parlist(ls, Dargs);
        if (testnext(ls, TK_LET) || testnext(ls, ':')) {
            enterlevel(ls);
            fix_Dargs(ls, Dargs, Dargs.length);
            retstat(ls);
            ls.fs.freereg = ls.fs.nactvar;
            leavelevel(ls);
        } else {
            enterlevel(ls);
            fix_Dargs(ls, Dargs, Dargs.length);
            statement(ls);
            leavelevel(ls);
        }
        ls.lastSymEndidx = ls.currentidx - 6;//lambda
        new_fs.f.endidx = ls.currentidx;
        new_fs.f.lastlinedefined = ls.linenumber;
        codeclosure(ls, e);
        close_func(ls, false);
    }

    // static int explist(LexState *ls, expdesc *v) {
    // /* explist -> expr { ',' expr } */
    // int n = 1; /* at least one expression */
    // expr(ls, v);
    // while (testnext(ls, ',')) {
    // luaK_exp2nextreg(ls->fs, v);
    // expr(ls, v);
    // n++;
    // }
    // return n;
    // }
    protected static int explist(LexState ls, expdesc v) throws LuaError {
        int n = 1;
        expr(ls, v);
        while (testnext(ls, ',')) {
            luaK_exp2nextreg(ls.fs, v);
            expr(ls, v);
            n++;
        }
        return n;
    }

    // static void funcargs(LexState *ls, expdesc *f, int line) {
    // FuncState *fs = ls->fs;
    // expdesc args;
    // int base, nparams;
    // switch (ls->t.token) {
    // case '(': { /* funcargs -> '(' [ explist ] ')' */
    // luaX_next(ls);
    // if (ls->t.token == ')') /* arg list is empty? */
    // args.k = VVOID;
    // else {
    // explist(ls, &args);
    // luaK_setmultret(fs, &args);
    // }
    // check_match(ls, ')', '(', line);
    // break;
    // }
    // case '{': { /* funcargs -> constructor */
    // constructor(ls, &args);
    // break;
    // }
    // case TK_STRING: { /* funcargs -> STRING */
    // codestring(ls, &args, ls->t.seminfo.ts);
    // luaX_next(ls); /* must use 'seminfo' before 'next' */
    // break;
    // }
    // default: {
    // luaX_syntaxerror(ls, "function arguments expected");
    // }
    // }
    // lua_assert(f->k == VNONRELOC);
    // base = f->u.info; /* base register for call */
    // if (hasmultret(args.k))
    // nparams = LUA_MULTRET; /* open call */
    // else {
    // if (args.k != VVOID)
    // luaK_exp2nextreg(fs, &args); /* close last argument */
    // nparams = fs->freereg - (base + 1);
    // }
    // init_exp(f, VCALL, luaK_codeABC(fs, OP_CALL, base, nparams + 1, 2));
    // luaK_fixline(fs, line);
    // fs->freereg = base + 1; /* call remove function and arguments and leaves
    // (unless changed) one result */
    // }
    protected static void funcargs(LexState ls, expdesc f, int line) throws LuaError {
        FuncState fs = ls.fs;
        expdesc args = new expdesc();
        int base, nparams;
        switch (ls.t.token) {
            case '(': {
                luaX_next(ls);
                if (ls.t.token == ')')
                    args.k = VVOID;
                else {
                    explist(ls, args);
                    luaK_setmultret(fs, args);
                }
                check_match(ls, ')', '(', line);
                break;
            }
            case '{': {
                constructor(ls, args);
                break;
            }
            case TK_STRING: {
                codestring(ls, args, ls.t.seminfo.ts);
                luaX_next(ls);
                break;
            }
            default: {
                luaX_syntaxerror(ls, "期待函数的参数");
            }
        }
        base = f.u.info;
        if (hasmultret(args.k))
            nparams = LUA_MULTRET;
        else {
            if (args.k != VVOID)
                luaK_exp2nextreg(fs, args);
            nparams = fs.freereg - (base + 1);
        }
        init_exp(f, VCALL, luaK_codeABC(fs, OP_CALL, base, nparams + 1, 2));
        luaK_fixline(fs, line);
        fs.freereg = (base + 1);
    }

    // static void primaryexp(LexState *ls, expdesc *v) {
    // /* primaryexp -> NAME | '(' expr ')' */
    // switch (ls->t.token) {
    // case '(': {
    // int line = ls->linenumber;
    // luaX_next(ls);
    // expr(ls, v);
    // check_match(ls, ')', '(', line);
    // luaK_dischargevars(ls->fs, v);
    // return;
    // }
    // case TK_NAME: {
    // singlevar(ls, v);
    // return;
    // }
    // default: {
    // luaX_syntaxerror(ls, "unexpected symbol");
    // }
    // }
    // }
    protected static void primaryexp(LexState ls, expdesc v) throws LuaError {
        switch (ls.t.token) {
            case '(': {
                int line = ls.linenumber;
                luaX_next(ls);
                expr(ls, v);
                check_match(ls, ')', '(', line);
                luaK_dischargevars(ls.fs, v);
                return;
            }
            case TK_NAME: {
                String name = ls.t.seminfo.ts;
                singlevar(ls, v);
                return;
            }
            default: {
                luaX_syntaxerror(ls, "出现非法内容" + txtToken(ls, ls.t.token));
            }
        }
    }

    // static void suffixedexp(LexState *ls, expdesc *v) {
    // /* suffixedexp ->
    // primaryexp { '.' NAME | '[' exp ']' | ':' NAME funcargs | funcargs } */
    // FuncState *fs = ls->fs;
    // int line = ls->linenumber;
    // primaryexp(ls, v);
    // for (;;) {
    // switch (ls->t.token) {
    // case '.':
    // case TK_LET: { /* fieldsel */
    // fieldsel(ls, v);
    // break;
    // }
    // case '[': { /* '[' exp1 ']' */
    // expdesc key;
    // luaK_exp2anyregup(fs, v);
    // yindex(ls, &key);
    // luaK_indexed(fs, v, &key);
    // break;
    // }
    // case ':': { /* ':' NAME funcargs */
    // expdesc key;
    // luaX_next(ls);
    // checkname(ls, &key);
    // luaK_self(fs, v, &key);
    // funcargs(ls, v, line);
    // break;
    // }
    // case '(':
    // case TK_STRING:
    // case '{': { /* funcargs */
    // luaK_exp2nextreg(fs, v);
    // funcargs(ls, v, line);
    // break;
    // }
    // default:
    // return;
    // }
    // }
    // }
    protected static void suffixedexp(LexState ls, expdesc v) throws LuaError {
        FuncState fs = ls.fs;
        int line = ls.linenumber;
        primaryexp(ls, v);
        for (; ; ) {
            switch (ls.t.token) {
                case '.':
                case TK_LET: {
                    fieldsel(ls, v);
                    break;
                }
                case '[': {
                    expdesc key = new expdesc();
                    luaK_exp2anyregup(fs, v);
                    yindex(ls, key);
                    luaK_indexed(fs, v, key);
                    break;
                }
                case ':': {
                    luaX_next(ls);
                    if (testnext(ls, '[')) {// v:[b,c] SECTION
                        luaK_exp2nextreg(fs, v);
                        expdesc from = new expdesc();
                        expr(ls, from);
                        luaK_exp2nextreg(fs, from);
                        checknext(ls, ',');
                        expdesc to = new expdesc();
                        expr(ls, to);
                        luaK_exp2nextreg(fs, to);
                        check_match(ls, ']', '[', line);
                        luaK_codeABC(fs, OP_SECTION, v.u.info, from.u.info, to.u.info);
                        fs.freereg -= 2;
                    } else {
                        expdesc key = new expdesc();
                        checkname(ls, key);
                        luaK_self(fs, v, key);
                        funcargs(ls, v, line);
                    }
                    break;
                }
                case '(':
                case TK_STRING:
                case '{': {
                    luaK_exp2nextreg(fs, v);
                    funcargs(ls, v, line);
                    break;
                }
                default:
                    return;
            }
        }
    }

    // static void letmean_body(LexState *ls, expdesc *e, int line, int retmode) {
    // FuncState new_fs;
    // BlockCnt bl;
    // new_fs.f = addprototype(ls);
    // new_fs.f->linedefined = line;
    // open_func(ls, &new_fs, &bl);
    // default_arg Dargs[256] = {0};
    // lambda_parlist(ls, Dargs);
    // checknext(ls, '{');
    // if (retmode) {
    // enterlevel(ls);
    // //mod by nwdxlgzs
    // fix_Dargs(ls, Dargs, sizeof(Dargs) / sizeof(Dargs[0]));
    // retstat(ls);
    // ls->fs->freereg = ls->fs->nactvar;
    // leavelevel(ls);
    // } else {
    // //mod by nwdxlgzs
    // enterlevel(ls);
    // fix_Dargs(ls, Dargs, sizeof(Dargs) / sizeof(Dargs[0]));
    // statlist(ls);
    // leavelevel(ls);
    // }
    // check_match(ls, '}', '{', line);
    // new_fs.f->lastlinedefined = ls->linenumber;
    // codeclosure(ls, e);
    // close_func(ls);
    // }
    protected static void letmean_body(LexState ls, expdesc e, int line, int retmode) throws LuaError {
        FuncState new_fs = new FuncState();
        BlockCnt bl = new BlockCnt();
        new_fs.f = addprototype(ls);
        new_fs.f.linedefined = line;
        open_func(ls, new_fs, bl);
        default_arg[] Dargs = new default_arg[256];
        for (int i = 0; i < Dargs.length; i++) {
            Dargs[i] = new default_arg();
        }
        lambda_parlist(ls, Dargs);
        checknext(ls, '{');
        if (retmode != 0) {
            enterlevel(ls);
            fix_Dargs(ls, Dargs, Dargs.length);
            retstat(ls);
            ls.fs.freereg = ls.fs.nactvar;
            leavelevel(ls);
        } else {
            enterlevel(ls);
            fix_Dargs(ls, Dargs, Dargs.length);
            statlist(ls);
            leavelevel(ls);
        }
        check_match(ls, '}', '{', line);
        new_fs.f.lastlinedefined = ls.linenumber;
        codeclosure(ls, e);
        close_func(ls, false);
    }

    // static void simpleexp(LexState *ls, expdesc *v) {
    // /* simpleexp -> FLT | INT | STRING | NIL | TRUE | FALSE | ... |
    // constructor | FUNCTION body | suffixedexp */
    // switch (ls->t.token) {
    // case TK_FLT: {
    // init_exp(v, VKFLT, 0);
    // v->u.nval = ls->t.seminfo.r;
    // break;
    // }
    // case TK_INT: {
    // init_exp(v, VKINT, 0);
    // v->u.ival = ls->t.seminfo.i;
    // break;
    // }
    // case TK_STRING: {
    // codestring(ls, v, ls->t.seminfo.ts);
    // //mod by nwdxlgzs
    // //==============================================================================
    // luaX_next(ls);
    // if (testnext(ls, ':')) { /* "xx":selffunc(XXX) */
    // expdesc key;
    // luaK_exp2anyregup(ls->fs, v);
    // checkname(ls, &key);
    // luaK_self(ls->fs, v, &key);
    // funcargs(ls, v, ls->linenumber);
    // } else if (testnext(ls, '[')) { /* "xx"[XXX] */
    // expdesc key;
    // luaK_exp2anyregup(ls->fs, v);
    // expr(ls, &key);
    // luaK_exp2val(ls->fs, &key);
    // //testnext(ls, ':')
    // checknext(ls, ']');
    // luaK_indexed(ls->fs, v, &key);
    // }
    // return;
    // // break;
    // //==============================================================================
    // }
    // case TK_NIL: {
    // init_exp(v, VNIL, 0);
    // break;
    // }
    // case TK_TRUE: {
    // init_exp(v, VTRUE, 0);
    // break;
    // }
    // case TK_FALSE: {
    // init_exp(v, VFALSE, 0);
    // break;
    // }
    // case TK_DOTS: { /* vararg */
    // FuncState *fs = ls->fs;
    // check_condition(ls, fs->f->is_vararg,
    // "cannot use '...' outside a vararg function");
    // fs->f->is_vararg = 1; /* function actually uses vararg */
    // init_exp(v, VVARARG, luaK_codeABC(fs, OP_VARARG, 0, 1, 0));
    // break;
    // }
    // case '{': { /* constructor */
    // constructor(ls, v);
    // //mod by nwdxlgzs
    // //==============================================================================
    // if (testnext(ls, '[')) { /* x={...}[xxx] */
    // expdesc key;
    // luaK_exp2anyregup(ls->fs, v);
    // expr(ls, &key);
    // luaK_exp2val(ls->fs, &key);
    // //testnext(ls, ':')
    // checknext(ls, ']');
    // luaK_indexed(ls->fs, v, &key);
    // }
    // return;
    // //==============================================================================
    // }
    // case '[': { /* constructor */
    // constructor2(ls, v);
    // //mod by nwdxlgzs
    // //==============================================================================
    // if (testnext(ls, '[')) { /* x={...}[xxx] */
    // expdesc key;
    // luaK_exp2anyregup(ls->fs, v);
    // expr(ls, &key);
    // luaK_exp2val(ls->fs, &key);
    // //testnext(ls, ':')
    // checknext(ls, ']');
    // luaK_indexed(ls->fs, v, &key);
    // }
    // return;
    // //==============================================================================
    // }
    // case TK_FUNCTION: {
    // luaX_next(ls);
    // //mod by nwdxlgzs
    // eatTag(ls, 1);
    // body(ls, v, 0, ls->linenumber);
    // return;
    // }
    // case TK_LAMBDA: {
    // luaX_next(ls);
    // lambda_body(ls, v, ls->linenumber);
    // return;
    // }
    // case TK_LET: {//mod by nwdxlgzs
    // luaX_next(ls);
    // letmean_body(ls, v, ls->linenumber, 0);
    // return;
    // }
    // case TK_MEAN: {//mod by nwdxlgzs
    // luaX_next(ls);
    // letmean_body(ls, v, ls->linenumber, 1);
    // return;
    // }
    // case TK_WHEN: {
    // whenstat(ls, ls->linenumber, v);
    // return;
    // }
    // default: {
    // suffixedexp(ls, v);
    // return;
    // }
    // }
    // luaX_next(ls);
    // }
    protected static void simpleexp(LexState ls, expdesc v) throws LuaError {
        switch (ls.t.token) {
            case TK_FLT: {
                init_exp(v, VKFLT, 0);
                v.u.nval = ls.t.seminfo.r;
                break;
            }
            case TK_INT: {
                init_exp(v, VKINT, 0);
                v.u.ival = ls.t.seminfo.i;
                break;
            }
            case TK_STRING: {
                codestring(ls, v, ls.t.seminfo.ts);
                luaX_next(ls);
                if (testnext(ls, ':')) {
                    expdesc key = new expdesc();
                    luaK_exp2anyregup(ls.fs, v);
                    checkname(ls, key);
                    luaK_self(ls.fs, v, key);
                    funcargs(ls, v, ls.linenumber);
                } else if (testnext(ls, '[')) {
                    expdesc key = new expdesc();
                    luaK_exp2anyregup(ls.fs, v);
                    expr(ls, key);
                    luaK_exp2val(ls.fs, key);
                    checknext(ls, ']');
                    luaK_indexed(ls.fs, v, key);
                }
                return;
            }
            case TK_NIL: {
                init_exp(v, VNIL, 0);
                break;
            }
            case TK_TRUE: {
                init_exp(v, VTRUE, 0);
                break;
            }
            case TK_FALSE: {
                init_exp(v, VFALSE, 0);
                break;
            }
            case TK_DOTS: {
                FuncState fs = ls.fs;
                check_condition(ls, fs.f.is_vararg != 0, "cannot use '...' outside a vararg function");
                fs.f.is_vararg = 1;
                init_exp(v, VVARARG, luaK_codeABC(fs, OP_VARARG, 0, 1, 0));
                break;
            }
            case '{': {
                constructor(ls, v);
                if (testnext(ls, '[')) {
                    expdesc key = new expdesc();
                    luaK_exp2anyregup(ls.fs, v);
                    expr(ls, key);
                    luaK_exp2val(ls.fs, key);
                    checknext(ls, ']');
                    luaK_indexed(ls.fs, v, key);
                }
                return;
            }
            case '[': {
                constructor2(ls, v);
                if (testnext(ls, '[')) {
                    expdesc key = new expdesc();
                    luaK_exp2anyregup(ls.fs, v);
                    expr(ls, key);
                    luaK_exp2val(ls.fs, key);
                    checknext(ls, ']');
                    luaK_indexed(ls.fs, v, key);
                }
                return;
            }
            case TK_FUNCTION: {
                luaX_next(ls);
                eatTag(ls, true);
                body(ls, v, 0, ls.linenumber);
                return;
            }
            case TK_LAMBDA: {
                luaX_next(ls);
                lambda_body(ls, v, ls.linenumber);
                return;
            }
            case TK_LET: {
                luaX_next(ls);
                letmean_body(ls, v, ls.linenumber, 0);
                return;
            }
            case TK_MEAN: {
                luaX_next(ls);
                letmean_body(ls, v, ls.linenumber, 1);
                return;
            }
            case TK_WHEN: {
                whenstat(ls, ls.linenumber, v);
                return;
            }
            case TK_IF: {
                multi_ternary_expr(ls, v);
                return;
            }
            default: {
                suffixedexp(ls, v);
                return;
            }
        }
        luaX_next(ls);
    }

    // static UnOpr getunopr(int op) {
    // switch (op) {
    // case TK_NOT:
    // return OPR_NOT;
    // case '-':
    // return OPR_MINUS;
    // case '~':
    // return OPR_BNOT;
    // case '#':
    // return OPR_LEN;
    // default:
    // return OPR_NOUNOPR;
    // }
    // }
    protected static int getunopr(int op) {
        switch (op) {
            case TK_NOT:
                return OPR_NOT;
            case '-':
                return OPR_MINUS;
            case '~':
                return OPR_BNOT;
            case '#':
                return OPR_LEN;
            default:
                return OPR_NOUNOPR;
        }
    }

    // static BinOpr getbinopr(int op) {
    // switch (op) {
    // case '+':
    // return OPR_ADD;
    // case '-':
    // return OPR_SUB;
    // case '*':
    // return OPR_MUL;
    // case '%':
    // return OPR_MOD;
    // case '^':
    // return OPR_POW;
    // case '/':
    // return OPR_DIV;
    // case TK_IDIV:
    // return OPR_IDIV;
    // case '&':
    // return OPR_BAND;
    // case '|':
    // return OPR_BOR;
    // case '~':
    // return OPR_BXOR;
    // case TK_SHL:
    // return OPR_SHL;
    // case TK_SHR:
    // return OPR_SHR;
    // case TK_CONCAT:
    // return OPR_CONCAT;
    // case TK_NE:
    // case TK_BXORA:
    // return OPR_NE;
    // case TK_EQ:
    // return OPR_EQ;
    // case '<':
    // return OPR_LT;
    // case TK_LE:
    // return OPR_LE;
    // case '>':
    // return OPR_GT;
    // case TK_GE:
    // return OPR_GE;
    // case TK_AND:
    // return OPR_AND;
    // case TK_OR:
    // return OPR_OR;
    // case TK_IS:
    // return OPR_IS;
    // default:
    // return OPR_NOBINOPR;
    // }
    // }
    protected static int getbinopr(int op) {
        switch (op) {
            case '+':
                return OPR_ADD;
            case '-':
                return OPR_SUB;
            case '*':
                return OPR_MUL;
            case '%':
                return OPR_MOD;
            case '^':
                return OPR_POW;
            case '/':
                return OPR_DIV;
            case TK_IDIV:
                return OPR_IDIV;
            case '&':
                return OPR_BAND;
            case '|':
                return OPR_BOR;
            case '~':
                return OPR_BXOR;
            case TK_SHL:
                return OPR_SHL;
            case TK_SHR:
                return OPR_SHR;
            case TK_CONCAT:
                return OPR_CONCAT;
            case TK_NE:
            case TK_BXORA:
                return OPR_NE;
            case TK_EQ:
                return OPR_EQ;
            case '<':
                return OPR_LT;
            case TK_LE:
                return OPR_LE;
            case '>':
                return OPR_GT;
            case TK_GE:
                return OPR_GE;
            case TK_AND:
                return OPR_AND;
            case TK_OR:
                return OPR_OR;
            case TK_IS:
                return OPR_IS;
            default:
                return OPR_NOBINOPR;
        }
    }

    protected static class priority_ {
        protected priority_(int left, int right) {
            this.left = left;
            this.right = right;
        }

        protected int left;
        protected int right;
    }

    // static const struct {
    // lu_byte left; /* left priority for each binary operator */
    // lu_byte right; /* right priority */
    // } priority[] = { /* ORDER OPR */
    // {10, 10},
    // {10, 10}, /* '+' '-' */
    // {11, 11},
    // {11, 11}, /* '*' '%' */
    // {14, 13}, /* '^' (right associative) */
    // {11, 11},
    // {11, 11}, /* '/' '//' */
    // {6, 6},
    // {4, 4},
    // {5, 5}, /* '&' '|' '~' */
    // {7, 7},
    // {7, 7}, /* '<<' '>>' */
    // {9, 8}, /* '..' (right associative) */
    // {3, 3},
    // {3, 3},
    // {3, 3}, /* ==, <, <= */
    // {3, 3},
    // {3, 3},
    // {3, 3}, /* ~=, >, >= */
    // {3, 3}, /* is */
    // {2, 2},
    // {1, 1} /* and, or */
    // };
    protected static final priority_[] priority = {
            new priority_(10, 10),
            new priority_(10, 10),
            new priority_(11, 11),
            new priority_(11, 11),
            new priority_(14, 13),
            new priority_(11, 11),
            new priority_(11, 11),
            new priority_(6, 6),
            new priority_(4, 4),
            new priority_(5, 5),
            new priority_(7, 7),
            new priority_(7, 7),
            new priority_(9, 8),
            new priority_(3, 3),
            new priority_(3, 3),
            new priority_(3, 3),
            new priority_(3, 3),
            new priority_(3, 3),
            new priority_(3, 3),
            new priority_(3, 3),
            new priority_(2, 2),
            new priority_(1, 1)
    };
    // #define UNARY_PRIORITY 12 /* priority for unary operators */
    protected static final int UNARY_PRIORITY = 12;

    // static BinOpr subexpr(LexState *ls, expdesc *v, int limit) {
    // BinOpr op;
    // UnOpr uop;
    // enterlevel(ls);
    // uop = getunopr(ls->t.token);
    // if (uop != OPR_NOUNOPR) {
    // int line = ls->linenumber;
    // luaX_next(ls);
    // subexpr(ls, v, UNARY_PRIORITY);
    // luaK_prefix(ls->fs, uop, v, line);
    // } else simpleexp(ls, v);
    // /* expand while operators have priorities higher than 'limit' */
    // op = getbinopr(ls->t.token);
    // while (op != OPR_NOBINOPR && priority[op].left > limit) {
    // expdesc v2;
    // BinOpr nextop;
    // int line = ls->linenumber;
    // luaX_next(ls);
    // luaK_infix(ls->fs, op, v);
    // /* read sub-expression with higher priority */
    // nextop = subexpr(ls, &v2, priority[op].right);
    // luaK_posfix(ls->fs, op, v, &v2, line);
    // op = nextop;
    // }
    // leavelevel(ls);
    // return op; /* return first untreated operator */
    // }
    protected static int subexpr(LexState ls, expdesc v, int limit) throws LuaError {
        int op;
        int uop;
        enterlevel(ls);
        uop = getunopr(ls.t.token);
        if (uop != OPR_NOUNOPR) {
            int line = ls.linenumber;
            luaX_next(ls);
            subexpr(ls, v, UNARY_PRIORITY);
            luaK_prefix(ls.fs, uop, v, line);
        } else
            simpleexp(ls, v);
        op = getbinopr(ls.t.token);
        while (op != OPR_NOBINOPR && priority[op].left > limit) {
            expdesc v2 = new expdesc();
            int nextop;
            int line = ls.linenumber;
            luaX_next(ls);
            luaK_infix(ls.fs, op, v);
            nextop = subexpr(ls, v2, priority[op].right);
            luaK_posfix(ls.fs, op, v, v2, line);
            op = nextop;
        }
        leavelevel(ls);
        return op;
    }

    // static void ternary_ret(LexState *ls, expdesc *e) {
    // testnext(ls, TK_THEN);
    // expdesc e1, e2;
    // //创建一个空寄存器，拷贝e到最顶部的v
    // expdesc v = clone(*e);
    // luaK_dischargevars(ls->fs, &v);
    // luaK_exp2nextreg(ls->fs, &v);//强制分配一个顶层的位置
    // int e_reg = v.u.info;//=ls->fs->freereg-1
    // //布尔测试跳转
    // luaK_codeABC(ls->fs, OP_TEST, e_reg, 0, 0);
    // //False的JMP
    // int Fjmp = luaK_jump(ls->fs);
    // //True的表达式
    // luaK_reserveregs(ls->fs, -1);//假装e_reg没人用
    // subexpr(ls, &e1, priority[OPR_AND].right);
    // luaK_exp2nextreg(ls->fs, &e1);
    // luaK_setoneret(ls->fs, &e1);
    // int Ejmp = luaK_jump(ls->fs);
    // luaK_concat(ls->fs, &ls->fs->jpc, Fjmp);
    // luaK_patchtohere(ls->fs, Fjmp);
    // if (ls->t.token == TK_ELSE) {
    // luaX_next(ls);
    // } else {
    // luaX_syntaxerror(ls, "expect else at ternary");
    // }
    // //False的表达式
    // luaK_reserveregs(ls->fs, -1);//假装e_reg没人用
    // subexpr(ls, &e2, priority[OPR_OR].right);
    // luaK_exp2nextreg(ls->fs, &e2);
    // luaK_setoneret(ls->fs, &e2);
    // luaK_concat(ls->fs, &ls->fs->jpc, Ejmp);
    // luaK_patchtohere(ls->fs, Ejmp);
    // *e = v;
    // testnext(ls, TK_END);
    // }
    // protected static void ternary_ret(LexState ls, expdesc e) throws LuaError {
    // ichecknext(ls, TK_THEN);
    // expdesc e1 = new expdesc();
    // expdesc e2 = new expdesc();
    // expdesc v = clone(e);
    // luaK_dischargevars(ls.fs, v);
    // luaK_exp2nextreg(ls.fs, v);
    // int e_reg = v.u.info;
    // luaK_codeABC(ls.fs, OP_TEST, e_reg, 0, 0);
    // int Fjmp = luaK_jump(ls.fs);
    // luaK_reserveregs(ls.fs, -1);
    // subexpr(ls, e1, priority[OPR_AND].right);
    // luaK_exp2nextreg(ls.fs, e1);
    // luaK_setoneret(ls.fs, e1);
    // int Ejmp = luaK_jump(ls.fs);
    // int[] jpc = new int[] { ls.fs.jpc };
    // luaK_concat(ls.fs, jpc, Fjmp);
    // ls.fs.jpc = jpc[0];
    // luaK_patchtohere(ls.fs, Fjmp);
    // if (ls.t.token == TK_ELSE) {
    // luaX_next(ls);
    // } else {
    // luaX_syntaxerror(ls, "expect else at ternary");
    // }
    // luaK_reserveregs(ls.fs, -1);
    // subexpr(ls, e2, priority[OPR_OR].right);
    // luaK_exp2nextreg(ls.fs, e2);
    // luaK_setoneret(ls.fs, e2);
    // jpc[0] = ls.fs.jpc;
    // luaK_concat(ls.fs, jpc, Ejmp);
    // ls.fs.jpc = jpc[0];
    // luaK_patchtohere(ls.fs, Ejmp);
    // // e = v;
    // expdescSet(e, v);
    // ichecknext(ls, TK_END);
    // }
    // protected static void ternary_ret(LexState ls, expdesc e) throws LuaError {
    // ichecknext(ls, TK_THEN);
    // expdesc e1 = new expdesc();
    // expdesc e2 = new expdesc();
    // expdesc v = clone(e);
    // luaK_dischargevars(ls.fs, v);
    // luaK_exp2nextreg(ls.fs, v);
    // int e_reg = v.u.info;
    // luaK_codeABC(ls.fs, OP_TEST, e_reg, 0, 0);
    // int Fjmp = luaK_jump(ls.fs);
    // luaK_reserveregs(ls.fs, -1);
    // subexpr(ls, e1, priority[OPR_AND].right);
    // luaK_exp2nextreg(ls.fs, e1);
    // luaK_setoneret(ls.fs, e1);
    // int Ejmp = luaK_jump(ls.fs);
    // int[] jpc = new int[] { ls.fs.jpc };
    // luaK_concat(ls.fs, jpc, Fjmp);
    // ls.fs.jpc = jpc[0];
    // luaK_patchtohere(ls.fs, Fjmp);
    // if (ls.t.token == TK_ELSE) {
    // luaX_next(ls);
    // } else {
    // luaX_syntaxerror(ls, "expect else at ternary");
    // }
    // luaK_reserveregs(ls.fs, -1);
    // subexpr(ls, e2, priority[OPR_OR].right);
    // luaK_exp2nextreg(ls.fs, e2);
    // luaK_setoneret(ls.fs, e2);
    // jpc[0] = ls.fs.jpc;
    // luaK_concat(ls.fs, jpc, Ejmp);
    // ls.fs.jpc = jpc[0];
    // luaK_patchtohere(ls.fs, Ejmp);
    // // e = v;
    // expdescSet(e, v);
    // ichecknext(ls, TK_END);
    // }

    protected static void single_test_then_retexpr(LexState ls, expdesc recvObj, int[] escapelist) throws LuaError {
        ls.fs.freereg++;// recvObj
        expdesc ret = clone(recvObj);
        BlockCnt bl = new BlockCnt();
        FuncState fs = ls.fs;
        expdesc cntexp = new expdesc();
        int jf;
        luaX_next(ls);// if/elseif
        expr(ls, cntexp);// <bool-exp>
        ichecknext(ls, TK_THEN);// then
        luaK_goiftrue(ls.fs, cntexp);
        enterblock(fs, bl, 0);
        jf = cntexp.f;
        expdesc e = new expdesc();
        expr(ls, e);
        luaK_exp2nextreg(ls.fs, e);
        luaK_setoneret(fs, e);
        luaK_storevar(fs, ret, e);
        leaveblock(fs);
        if (ls.t.token == TK_ELSE ||
                ls.t.token == TK_ELSEIF)
            luaK_concat(fs, escapelist, luaK_jump(fs));
        luaK_patchtohere(fs, jf);
    }

    protected static void single_retexpr(LexState ls, expdesc recvObj) throws LuaError {
        ls.fs.freereg++;// recvObj
        expdesc ret = clone(recvObj);
        FuncState fs = ls.fs;
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, 0);
        expdesc e = new expdesc();
        expr(ls, e);
        luaK_exp2nextreg(ls.fs, e);
        luaK_setoneret(fs, e);
        luaK_storevar(fs, ret, e);
        leaveblock(fs);
    }

    protected static void multi_ternary_expr(LexState ls, expdesc recvObj) throws LuaError {
        int retreg = ls.fs.freereg;
        init_exp(recvObj, VLOCAL, (int) retreg);
        FuncState fs = ls.fs;
        int[] escapelist = {NO_JUMP};
        single_test_then_retexpr(ls, recvObj, escapelist);
        while (ls.t.token == TK_ELSEIF)
            single_test_then_retexpr(ls, recvObj, escapelist);
        if (testnext(ls, TK_ELSE))
            single_retexpr(ls, recvObj);
        luaK_patchtohere(fs, escapelist[0]);
        ls.fs.freereg = retreg + 1;
    }

    protected static void expr(LexState ls, expdesc v) throws LuaError {
        subexpr(ls, v, 0);
        ls.lastSymEndidx = ls.currentidx - 1;
    }

    // static void block(LexState *ls) {
    // /* block -> statlist */
    // FuncState *fs = ls->fs;
    // BlockCnt bl;
    // enterblock(fs, &bl, 0);
    // statlist(ls);
    // leaveblock(fs);
    // }
    protected static void block(LexState ls) throws LuaError {
        FuncState fs = ls.fs;
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, 0);
        statlist(ls);
        leaveblock(fs);
    }

    protected static class LHS_assign {
        protected LHS_assign() {
        }

        // struct LHS_assign {
        // struct LHS_assign *prev;
        // expdesc v; /* variable (global, local, upvalue, or indexed) */
        // };
        protected LHS_assign prev = null;
        protected expdesc v = new expdesc();
    }

    // static void check_conflict(LexState *ls, struct LHS_assign *lh, expdesc *v) {
    // FuncState *fs = ls->fs;
    // int extra = fs->freereg; /* eventual position to save local variable */
    // int conflict = 0;
    // for (; lh; lh = lh->prev) { /* check all previous assignments */
    // if (lh->v.k == VINDEXED) { /* assigning to a table? */
    // /* table is the upvalue/local being assigned now? */
    // if (lh->v.u.ind.vt == v->k && lh->v.u.ind.t == v->u.info) {
    // conflict = 1;
    // lh->v.u.ind.vt = VLOCAL;
    // lh->v.u.ind.t = extra; /* previous assignment will use safe copy */
    // }
    // /* index is the local being assigned? (index cannot be upvalue) */
    // if (v->k == VLOCAL && lh->v.u.ind.idx == v->u.info) {
    // conflict = 1;
    // lh->v.u.ind.idx = extra; /* previous assignment will use safe copy */
    // }
    // }
    // }
    // if (conflict) {
    // /* copy upvalue/local value to a temporary (in position 'extra') */
    // OpCode op = (v->k == VLOCAL) ? OP_MOVE : OP_GETUPVAL;
    // luaK_codeABC(fs, op, extra, v->u.info, 0);
    // luaK_reserveregs(fs, 1);
    // }
    // }
    protected static void check_conflict(LexState ls, LHS_assign lh, expdesc v) throws LuaError {
        FuncState fs = ls.fs;
        int extra = fs.freereg;
        int conflict = 0;
        for (; lh != null; lh = lh.prev) {
            if (lh.v.k == VINDEXED) {
                if (lh.v.u.ind.vt == v.k && lh.v.u.ind.t == v.u.info) {
                    conflict = 1;
                    lh.v.u.ind.vt = VLOCAL;
                    lh.v.u.ind.t = (byte) extra;
                }
                if (v.k == VLOCAL && lh.v.u.ind.idx == v.u.info) {
                    conflict = 1;
                    lh.v.u.ind.idx = (short) extra;
                }
            }
        }
        if (conflict != 0) {
            int op = (v.k == VLOCAL) ? OP_MOVE : OP_GETUPVAL;
            luaK_codeABC(fs, op, extra, v.u.info, 0);
            luaK_reserveregs(fs, 1);
        }
    }

    // static void assignment(LexState *ls, struct LHS_assign *lh, int nvars) {
    // expdesc e;
    // check_condition(ls, vkisvar(lh->v.k), "syntax error");
    // if (testnext(ls, ',')) { /* assignment -> ',' suffixedexp assignment */
    // struct LHS_assign nv;
    // nv.prev = lh;
    // suffixedexp(ls, &nv.v);
    // if (nv.v.k != VINDEXED)
    // check_conflict(ls, lh, &nv.v);
    // checklimit(ls->fs, nvars + ls->L->nCcalls, LUAI_MAXCCALLS,
    // "C levels");
    // assignment(ls, &nv, nvars + 1);
    // } else { /* assignment -> '=' explist */
    // int nexps;
    // checknext(ls, '=');
    // nexps = explist(ls, &e);
    // if (nexps != nvars) {
    // adjust_assign(ls, nvars, nexps, &e);
    // //if (nexps > nvars)
    // // ls->fs->freereg -= nexps - nvars; /* remove extra values */
    // } else {
    // luaK_setoneret(ls->fs, &e); /* close last expression */
    // luaK_storevar(ls->fs, &lh->v, &e);
    // return; /* avoid default */
    // }
    // }
    // init_exp(&e, VNONRELOC, ls->fs->freereg - 1); /* default assignment */
    // luaK_storevar(ls->fs, &lh->v, &e);
    // }
    // #define vkisvar(k) (VLOCAL <= (k) && (k) <= VINDEXED)
    protected static boolean vkisvar(int k) {
        return VLOCAL <= k && k <= VINDEXED;
    }

    protected static void assignment(LexState ls, LHS_assign lh, int nvars) throws LuaError {
        expdesc e = new expdesc();
        check_condition(ls, vkisvar(lh.v.k), "发现语法错误");
        if (testnext(ls, ',')) {
            LHS_assign nv = new LHS_assign();
            nv.prev = lh;
            suffixedexp(ls, nv.v);
            if (nv.v.k != VINDEXED)
                check_conflict(ls, lh, nv.v);
            checklimit(ls.fs, nvars + ls.L.nCcalls, LUAI_MAXCCALLS, "C调用层级");
            assignment(ls, nv, nvars + 1);
        } else {
            int nexps;
            checknext(ls, '=');
            nexps = explist(ls, e);
            if (nexps != nvars) {
                adjust_assign(ls, nvars, nexps, e);
            } else {
                luaK_setoneret(ls.fs, e);
                luaK_storevar(ls.fs, lh.v, e);
                return;
            }
        }
        init_exp(e, VNONRELOC, ls.fs.freereg - 1);
        luaK_storevar(ls.fs, lh.v, e);
    }

    // static int cond(LexState *ls) {
    // /* cond -> exp */
    // expdesc v;
    // expr(ls, &v); /* read condition */
    // if (v.k == VNIL) v.k = VFALSE; /* 'falses' are all equal here */
    // luaK_goiftrue(ls->fs, &v);
    // return v.f;
    // }
    protected static int cond(LexState ls) throws LuaError {
        expdesc v = new expdesc();
        expr(ls, v);
        if (v.k == VNIL)
            v.k = VFALSE;
        luaK_goiftrue(ls.fs, v);
        return v.f;
    }

    // static void gotostat(LexState *ls, int pc) {
    // int line = ls->linenumber;
    // TString *label;
    // int g;
    // if (testnext(ls, TK_GOTO))
    // label = str_checkname(ls);
    // else if (testnext(ls, TK_CONTINUE))
    // label = luaS_new(ls->L, "continue");//mod by nirenr
    // else {
    // luaX_next(ls); /* skip break */
    // label = luaS_new(ls->L, "break");
    // }
    // g = newlabelentry(ls, &ls->dyd->gt, label, line, pc);
    // findlabel(ls, g); /* close it if label already defined */
    // }
    protected static void gotostat(LexState ls, int pc) throws LuaError {
        int line = ls.linenumber;
        String label;
        int g;
        if (testnext(ls, TK_GOTO))
            label = str_checkname(ls);
        else if (testnext(ls, TK_CONTINUE))
            label = "continue";
        else {
            luaX_next(ls);
            label = "break";
        }
        g = newlabelentry(ls, ls.dyd.gt, label, line, pc);
        findlabel(ls, g);
    }

    // static void checkrepeated(FuncState *fs, Labellist *ll, TString *label) {
    // int i;
    // for (i = fs->bl->firstlabel; i < ll->n; i++) {
    // if (eqstr(label, ll->arr[i].name)) {
    // const char *msg = luaO_pushfstring(fs->ls->L,
    // "label '%s' already defined on line %d",
    // getstr(label), ll->arr[i].line);
    // semerror(fs->ls, msg);
    // }
    // }
    // }
    protected static void checkrepeated(FuncState fs, Labellist ll, String label) throws LuaError {
        for (int i = fs.bl.firstlabel; i < ll.n; i++) {
            if (eqstr(label, ll.arr[i].name)) {
                String msg = luaO_pushfstring(fs.ls.L,
                        "标签'%s'已经在%d行定义",
                        (label), ll.arr[i].line);
                semerror(fs.ls, msg);
            }
        }
    }

    // static void skipnoopstat(LexState *ls) {
    // while (ls->t.token == ';' || ls->t.token == TK_DBCOLON)
    // statement(ls);
    // }
    protected static void skipnoopstat(LexState ls) throws LuaError {
        while (ls.t.token == ';' || ls.t.token == TK_DBCOLON)
            statement(ls);
    }

    // static void labelstat(LexState *ls, TString *label, int line) {
    // /* label -> '::' NAME '::' */
    // FuncState *fs = ls->fs;
    // Labellist *ll = &ls->dyd->label;
    // int l; /* index of new label being created */
    // checkrepeated(fs, ll, label); /* check for repeated labels */
    // ichecknext(ls, TK_DBCOLON);//mod by nirenr /* skip double colon */
    // /* create new entry for this label */
    // l = newlabelentry(ls, ll, label, line, luaK_getlabel(fs));
    // skipnoopstat(ls); /* skip other no-op statements */
    // if (block_follow(ls, 0)) { /* label is last no-op statement in the block? */
    // /* assume that locals are already out of scope */
    // ll->arr[l].nactvar = fs->bl->nactvar;
    // }
    // findgotos(ls, &ll->arr[l]);
    // }
    protected static void labelstat(LexState ls, String label, int line) throws LuaError {
        FuncState fs = ls.fs;
        Labellist ll = ls.dyd.label;
        int l;
        checkrepeated(fs, ll, label);
        ichecknext(ls, TK_DBCOLON);
        l = newlabelentry(ls, ll, label, line, luaK_getlabel(fs));
        skipnoopstat(ls);
        if (block_follow(ls, 0)) {
            ll.arr[l].nactvar = fs.bl.nactvar;
        }
        findgotos(ls, ll.arr[l]);
    }

    // static void whilestat(LexState *ls, int line) {
    // /* whilestat -> WHILE cond DO block END */
    // FuncState *fs = ls->fs;
    // int whileinit;
    // int condexit;
    // BlockCnt bl;
    // luaX_next(ls); /* skip WHILE */
    // whileinit = luaK_getlabel(fs);
    // condexit = cond(ls);
    // enterblock(fs, &bl, 1);
    // ichecknext(ls, TK_DO);//mod by nirenr
    // block(ls);
    // continuelabel(ls);//mod by nirenr
    // luaK_jumpto(fs, whileinit);
    // check_match(ls, TK_END, TK_WHILE, line);
    // leaveblock(fs);
    //
    // luaK_patchtohere(fs, condexit); /* false conditions finish the loop */
    // }
    protected static void whilestat(LexState ls, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        FuncState fs = ls.fs;
        int whileinit;
        int condexit;
        BlockCnt bl = new BlockCnt();
        luaX_next(ls);
        whileinit = luaK_getlabel(fs);
        condexit = cond(ls);
        enterblock(fs, bl, 1);
        ichecknext(ls, TK_DO);
        block(ls);
        continuelabel(ls);
        luaK_jumpto(fs, whileinit);
        if (ls.L.inLexer)
            AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
        check_match(ls, TK_END, TK_WHILE, line);
        leaveblock(fs);
        luaK_patchtohere(fs, condexit);
    }

    // static void repeatstat(LexState *ls, int line) {
    // /* repeatstat -> REPEAT block UNTIL cond */
    // int condexit;
    // FuncState *fs = ls->fs;
    // int repeat_init = luaK_getlabel(fs);
    // BlockCnt bl1, bl2;
    // enterblock(fs, &bl1, 1); /* loop block */
    // enterblock(fs, &bl2, 0); /* scope block */
    // luaX_next(ls); /* skip REPEAT */
    // statlist(ls);
    // continuelabel(ls);//mod by nirenr
    // check_match(ls, TK_UNTIL, TK_REPEAT, line);
    // condexit = cond(ls); /* read condition (inside scope block) */
    // if (bl2.upval) /* upvalues? */
    // luaK_patchclose(fs, condexit, bl2.nactvar);
    // leaveblock(fs); /* finish scope */
    // luaK_patchlist(fs, condexit, repeat_init); /* close the loop */
    // leaveblock(fs); /* finish loop */
    // }
    protected static void repeatstat(LexState ls, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        int condexit;
        FuncState fs = ls.fs;
        int repeat_init = luaK_getlabel(fs);
        BlockCnt bl1 = new BlockCnt();
        BlockCnt bl2 = new BlockCnt();
        enterblock(fs, bl1, 1);
        enterblock(fs, bl2, 0);
        luaX_next(ls);
        statlist(ls);
        continuelabel(ls);
        if (ls.L.inLexer)
            AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
        check_match(ls, TK_UNTIL, TK_REPEAT, line);
        condexit = cond(ls);
        if (bl2.upval != 0)
            luaK_patchclose(fs, condexit, bl2.nactvar);
        leaveblock(fs);
        luaK_patchlist(fs, condexit, repeat_init);
        leaveblock(fs);
    }

    // static int exp1(LexState *ls) {
    // expdesc e;
    // int reg;
    // expr(ls, &e);
    // luaK_exp2nextreg(ls->fs, &e);
    // lua_assert(e.k == VNONRELOC);
    // reg = e.u.info;
    // return reg;
    // }
    protected static int exp1(LexState ls) throws LuaError {
        expdesc e = new expdesc();
        int reg;
        expr(ls, e);
        luaK_exp2nextreg(ls.fs, e);
        reg = e.u.info;
        return reg;
    }

    // //foreah是为了for k,v : T do end代替for k,v in pairs(T) do
    // end而存在的，所以动态push进独立的pairs就好
    // static void foreachbody(LexState *ls, int base, int line, int nvars) {
    // expdesc e;
    // FuncState *fs = ls->fs;
    // int prep, endfor;
    // BlockCnt bl;
    // luaK_reserveregs(fs, 1);//腾出一个寄存器用于存储迭代器
    // adjust_assign(ls, 3, explist(ls, &e), &e);
    // luaK_checkstack(fs, 3); /* extra space to call generator */
    // adjustlocalvars(ls, 3); /* control variables */
    // ichecknext(ls, TK_DO);//mod by nirenr
    // luaK_reserveregs(fs, -1);
    // luaK_codeABC(fs, OP_TFOREACH, base, 0,
    // nvars + 2);//B因为迭代器后方还有两个变量（但是解释器强制赋值了2,所以B就不赋值了),C很正常，是CALL返回数量
    // prep = luaK_jump(fs);
    // enterblock(fs, &bl, 0); /* scope for declared variables */
    // adjustlocalvars(ls, nvars);
    // luaK_reserveregs(fs, nvars);
    // block(ls);
    // continuelabel(ls);//mod by nirenr
    // leaveblock(fs); /* end of scope for declared variables */
    // luaK_patchtohere(fs, prep);
    // luaK_codeABC(fs, OP_TFORCALL, base, 0, nvars);
    // luaK_fixline(fs, line);
    // endfor = luaK_codeAsBx(fs, OP_TFORLOOP, base + 2, NO_JUMP);
    // luaK_fixline(fs, line);
    // luaK_patchlist(fs, endfor, prep + 1);
    // }
    protected static void foreachbody(LexState ls, int base, int line, int nvars) throws LuaError {
        expdesc e = new expdesc();
        FuncState fs = ls.fs;
        int prep, endfor;
        BlockCnt bl = new BlockCnt();
        luaK_reserveregs(fs, 1);
        adjust_assign(ls, 3, explist(ls, e), e);
        luaK_checkstack(fs, 3);
        adjustlocalvars(ls, 3);
        ichecknext(ls, TK_DO);
        luaK_reserveregs(fs, -1);
        luaK_codeABC(fs, OP_TFOREACH, base, 0, nvars + 2);
        prep = luaK_jump(fs);
        enterblock(fs, bl, 0);
        adjustlocalvars(ls, nvars);
        luaK_reserveregs(fs, nvars);
        block(ls);
        continuelabel(ls);
        leaveblock(fs);
        luaK_patchtohere(fs, prep);
        luaK_codeABC(fs, OP_TFORCALL, base, 0, nvars);
        luaK_fixline(fs, line);
        endfor = luaK_codeAsBx(fs, OP_TFORLOOP, base + 2, NO_JUMP);
        luaK_fixline(fs, line);
        luaK_patchlist(fs, endfor, prep + 1);
    }

    // static void forbody(LexState *ls, int base, int line, int nvars, int isnum) {
    // /* forbody -> DO block */
    // BlockCnt bl;
    // FuncState *fs = ls->fs;
    // int prep, endfor;
    // adjustlocalvars(ls, 3); /* control variables */
    // ichecknext(ls, TK_DO);//mod by nirenr
    // prep = isnum ? luaK_codeAsBx(fs, OP_FORPREP, base, NO_JUMP) : luaK_jump(fs);
    // enterblock(fs, &bl, 0); /* scope for declared variables */
    // adjustlocalvars(ls, nvars);
    // luaK_reserveregs(fs, nvars);
    // block(ls);
    // continuelabel(ls);//mod by nirenr
    // leaveblock(fs); /* end of scope for declared variables */
    // luaK_patchtohere(fs, prep);
    // if (isnum) /* numeric for? */
    // endfor = luaK_codeAsBx(fs, OP_FORLOOP, base, NO_JUMP);
    // else { /* generic for */
    // luaK_codeABC(fs, OP_TFORCALL, base, 0, nvars);
    // luaK_fixline(fs, line);
    // endfor = luaK_codeAsBx(fs, OP_TFORLOOP, base + 2, NO_JUMP);
    // }
    // luaK_patchlist(fs, endfor, prep + 1);
    // luaK_fixline(fs, line);
    // }
    protected static void forbody(LexState ls, int base, int line, int nvars, int isnum) throws LuaError {
        BlockCnt bl = new BlockCnt();
        FuncState fs = ls.fs;
        int prep, endfor;
        adjustlocalvars(ls, 3);
        ichecknext(ls, TK_DO);
        prep = isnum != 0 ? luaK_codeAsBx(fs, OP_FORPREP, base, NO_JUMP) : luaK_jump(fs);
        enterblock(fs, bl, 0);
        adjustlocalvars(ls, nvars);
        luaK_reserveregs(fs, nvars);
        block(ls);
        continuelabel(ls);
        leaveblock(fs);
        luaK_patchtohere(fs, prep);
        if (isnum != 0)
            endfor = luaK_codeAsBx(fs, OP_FORLOOP, base, NO_JUMP);
        else {
            luaK_codeABC(fs, OP_TFORCALL, base, 0, nvars);
            luaK_fixline(fs, line);
            endfor = luaK_codeAsBx(fs, OP_TFORLOOP, base + 2, NO_JUMP);
        }
        luaK_patchlist(fs, endfor, prep + 1);
        luaK_fixline(fs, line);
    }

    // static void fornum(LexState *ls, TString *varname, int line) {
    // /* fornum -> NAME = exp1,exp1[,exp1] forbody */
    // FuncState *fs = ls->fs;
    // int base = fs->freereg;
    // new_localvarliteral(ls, "(for index)");
    // new_localvarliteral(ls, "(for limit)");
    // new_localvarliteral(ls, "(for step)");
    // new_localvar(ls, varname);
    // checknext(ls, '=');
    // exp1(ls); /* initial value */
    // checknext(ls, ',');
    // exp1(ls); /* limit */
    // if (testnext(ls, ','))
    // exp1(ls); /* optional step */
    // else { /* default step = 1 */
    // luaK_codek(fs, fs->freereg, luaK_intK(fs, 1));
    // luaK_reserveregs(fs, 1);
    // }
    // forbody(ls, base, line, 1, 1);
    // }
    protected static void fornum(LexState ls, String varname, int line) throws LuaError {
        FuncState fs = ls.fs;
        int base = fs.freereg;
        new_localvarliteral(ls, "(for index)");
        new_localvarliteral(ls, "(for limit)");
        new_localvarliteral(ls, "(for step)");
        new_localvar(ls, varname);
        checknext(ls, '=');
        exp1(ls);
        checknext(ls, ',');
        exp1(ls);
        if (testnext(ls, ','))
            exp1(ls);
        else {
            luaK_codek(fs, fs.freereg, luaK_intK(fs, 1));
            luaK_reserveregs(fs, 1);
        }
        forbody(ls, base, line, 1, 1);
    }

    // static void forlist(LexState *ls, TString *indexname) {
    // /* forlist -> NAME {,NAME} IN explist forbody */
    // FuncState *fs = ls->fs;
    // int nvars = 4; /* gen, state, control, plus at least one declared var */
    // int line;
    // int base = fs->freereg;
    // /* create control variables */
    // new_localvarliteral(ls, "(for generator)");
    // new_localvarliteral(ls, "(for state)");
    // new_localvarliteral(ls, "(for control)");
    // /* create declared variables */
    // new_localvar(ls, indexname);
    // while (testnext(ls, ',')) {
    // new_localvar(ls, str_checkname(ls));
    // nvars++;
    // }
    // if (testnext(ls, ':')) {//mod by nwdxlgzs
    // line = ls->linenumber;
    // foreachbody(ls, base, line, nvars - 3);
    // } else {
    // expdesc e;
    // ichecknext(ls, TK_IN);//mod by nirenr
    // line = ls->linenumber;
    // adjust_assign(ls, 3, explist(ls, &e), &e);
    // luaK_checkstack(fs, 3); /* extra space to call generator */
    // forbody(ls, base, line, nvars - 3, 0);
    // }
    // }
    protected static void forlist(LexState ls, String indexname) throws LuaError {
        FuncState fs = ls.fs;
        int nvars = 4;
        int line;
        int base = fs.freereg;
        new_localvarliteral(ls, "(for generator)");
        new_localvarliteral(ls, "(for state)");
        new_localvarliteral(ls, "(for control)");
        new_localvar(ls, indexname);
        while (testnext(ls, ',')) {
            new_localvar(ls, str_checkname(ls));
            nvars++;
        }
        if (testnext(ls, ':')) {
            line = ls.linenumber;
            foreachbody(ls, base, line, nvars - 3);
        } else {
            expdesc e = new expdesc();
            ichecknext(ls, TK_IN);
            line = ls.linenumber;
            adjust_assign(ls, 3, explist(ls, e), e);
            luaK_checkstack(fs, 3);
            forbody(ls, base, line, nvars - 3, 0);
        }
    }

    // static void forstat(LexState *ls, int line) {
    // /* forstat -> FOR (fornum | forlist) END */
    // FuncState *fs = ls->fs;
    // TString *varname;
    // BlockCnt bl;
    // enterblock(fs, &bl, 1); /* scope for loop and control variables */
    // luaX_next(ls); /* skip 'for' */
    // varname = str_checkname(ls); /* first variable name */
    // switch (ls->t.token) {
    // case '=':
    // fornum(ls, varname, line);
    // break;
    // default:
    // forlist(ls, varname);
    // /*case ',': case TK_IN: forlist(ls, varname); break;
    // default: luaX_syntaxerror(ls, "'=' or 'in' expected");*/
    // }
    // check_match(ls, TK_END, TK_FOR, line);
    // leaveblock(fs); /* loop scope ('break' jumps to this point) */
    // }
    protected static void forstat(LexState ls, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        FuncState fs = ls.fs;
        String varname;
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, 1);
        luaX_next(ls);
        varname = str_checkname(ls);
        switch (ls.t.token) {
            case '=':
                fornum(ls, varname, line);
                break;
            default:
                forlist(ls, varname);
        }

        if (ls.L.inLexer)
            AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
        check_match(ls, TK_END, TK_FOR, line);
        leaveblock(fs);
    }

    // static void test_then_block(LexState *ls, int *escapelist) {
    // /* test_then_block -> [IF | ELSEIF] cond THEN block */
    // BlockCnt bl;
    // FuncState *fs = ls->fs;
    // expdesc v;
    // int jf; /* instruction to skip 'then' code (if condition is false) */
    // luaX_next(ls); /* skip IF or ELSEIF */
    // expr(ls, &v); /* read condition */
    // ichecknext(ls, TK_THEN);//mod by nirenr
    // if (ls->t.token == TK_GOTO || ls->t.token == TK_BREAK || ls->t.token ==
    // TK_CONTINUE) {
    // luaK_goiffalse(ls->fs, &v); /* will jump to label if condition is true */
    // enterblock(fs, &bl, 0); /* must enter block before 'goto' */
    // gotostat(ls, v.t); /* handle goto/break */
    //
    // //skipnoopstat(ls); /* skip other no-op statements */
    // //mod by nirenr
    // while (testnext(ls, ';')) {} /* skip semicolons */
    //
    // if (block_follow(ls, 0)) { /* 'goto' is the entire block? */
    // leaveblock(fs);
    // return; /* and that is it */
    // } else /* must skip over 'then' part if condition is false */
    // //jf = luaK_jump(fs);
    // luaX_syntaxerror(ls, "unreachable statement");
    // } else { /* regular case (not goto/break) */
    // luaK_goiftrue(ls->fs, &v); /* skip over block if condition is false */
    // enterblock(fs, &bl, 0);
    // jf = v.f;
    // }
    // statlist(ls); /* 'then' part */
    // leaveblock(fs);
    // if (ls->t.token == TK_ELSE ||
    // ls->t.token == TK_ELSEIF) /* followed by 'else'/'elseif'? */
    // luaK_concat(fs, escapelist, luaK_jump(fs)); /* must jump over it */
    // luaK_patchtohere(fs, jf);
    // }
    protected static void test_then_block(LexState ls, int[] escapelist) throws LuaError {
        BlockCnt bl = new BlockCnt();
        FuncState fs = ls.fs;
        expdesc v = new expdesc();
        int jf = 0;
        luaX_next(ls);
        expr(ls, v);
        ichecknext(ls, TK_THEN);
        if (ls.t.token == TK_GOTO || ls.t.token == TK_BREAK || ls.t.token == TK_CONTINUE) {
            luaK_goiffalse(ls.fs, v);
            enterblock(fs, bl, 0);
            gotostat(ls, v.t);
            while (testnext(ls, ';')) {
            }
            if (block_follow(ls, 0)) {
                leaveblock(fs);
                return;
            } else
                luaX_syntaxerror(ls, "发现无法抵达的语句");
        } else {
            luaK_goiftrue(ls.fs, v);
            enterblock(fs, bl, 0);
            jf = v.f;
        }
        statlist(ls);
        leaveblock(fs);
        if (ls.t.token == TK_ELSE || ls.t.token == TK_ELSEIF)
            luaK_concat(fs, escapelist, luaK_jump(fs));
        luaK_patchtohere(fs, jf);
    }

    // static void ifstat(LexState *ls, int line) {
    // /* ifstat -> IF cond THEN block {ELSEIF cond THEN block} [ELSE block] END */
    // FuncState *fs = ls->fs;
    // int escapelist = NO_JUMP; /* exit list for finished parts */
    // test_then_block(ls, &escapelist); /* IF cond THEN block */
    // while (ls->t.token == TK_ELSEIF)
    // test_then_block(ls, &escapelist); /* ELSEIF cond THEN block */
    // if (testnext(ls, TK_ELSE))
    // block(ls); /* 'else' part */
    // check_match(ls, TK_END, TK_IF, line);
    // luaK_patchtohere(fs, escapelist); /* patch escape list to 'if' end */
    // }
    protected static void ifstat(LexState ls, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        FuncState fs = ls.fs;
        int[] escapelist = {NO_JUMP};
        test_then_block(ls, escapelist);
        while (ls.t.token == TK_ELSEIF)
            test_then_block(ls, escapelist);
        if (testnext(ls, TK_ELSE))
            block(ls);
        if (ls.L.inLexer)
            AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
        check_match(ls, TK_END, TK_IF, line);
        luaK_patchtohere(fs, escapelist[0]);
    }

    // static void single_test_then_block(LexState *ls, int *escapelist) {
    // BlockCnt bl;
    // int line;
    // FuncState *fs = ls->fs;
    // TString *jlb = NULL;
    // int target = NO_JUMP;
    // expdesc v;
    // int jf; /* instruction to skip 'then' code (if condition is false) */
    // luaX_next(ls); /* skip IF or ELSEIF */
    // expr(ls, &v); /* read condition */
    // ichecknext(ls, TK_THEN);
    // line = ls->linenumber;
    // luaK_goiftrue(ls->fs, &v); /* skip over block if condition is false */
    // enterblock(fs, &bl, 0);
    // jf = v.f;
    // statement(ls);
    // leaveblock(fs);
    // if (ls->t.token == TK_ELSE ||
    // ls->t.token == TK_ELSEIF) /* followed by 'else'/'elseif'? */
    // luaK_concat(fs, escapelist, luaK_jump(fs)); /* must jump over it */
    // luaK_patchtohere(fs, jf);
    // }
    protected static void single_test_then_block(LexState ls, int[] escapelist) throws LuaError {
        BlockCnt bl = new BlockCnt();
        int line;
        FuncState fs = ls.fs;
        String jlb = null;
        int target = NO_JUMP;
        expdesc v = new expdesc();
        int jf;
        luaX_next(ls);
        expr(ls, v);
        testnext(ls, TK_THEN);
        line = ls.linenumber;
        luaK_goiftrue(ls.fs, v);
        enterblock(fs, bl, 0);
        jf = v.f;
        statement(ls);
        leaveblock(fs);
        if (ls.t.token == TK_ELSE ||
                ls.t.token == TK_ELSEIF)
            luaK_concat(fs, escapelist, luaK_jump(fs));
        luaK_patchtohere(fs, jf);
    }

    // static void single_block(LexState *ls) {
    // /* block -> statlist */
    // FuncState *fs = ls->fs;
    // BlockCnt bl;
    // enterblock(fs, &bl, 0);
    // statement(ls);
    // leaveblock(fs);
    // }
    protected static void single_block(LexState ls) throws LuaError {
        FuncState fs = ls.fs;
        BlockCnt bl = new BlockCnt();
        enterblock(fs, bl, 0);
        statement(ls);
        leaveblock(fs);
    }

    // static void single_ifstat(LexState *ls, int line) {
    //
    // FuncState *fs = ls->fs;
    // int escapelist = NO_JUMP; /* exit list for finished parts */
    // single_test_then_block(ls, &escapelist); /* IF cond THEN block */
    // while (ls->t.token == TK_ELSEIF)
    // single_test_then_block(ls, &escapelist); /* ELSEIF cond THEN block */
    // if (testnext(ls, TK_ELSE))
    // single_block(ls); /* 'else' part */
    // ichecknext(ls, TK_END);
    // luaK_patchtohere(fs, escapelist); /* patch escape list to 'if' end */
    // }
    protected static void single_ifstat(LexState ls, int line) throws LuaError {
        FuncState fs = ls.fs;
        int[] escapelist = {NO_JUMP};
        single_test_then_block(ls, escapelist);
        while (ls.t.token == TK_ELSEIF)
            single_test_then_block(ls, escapelist);
        if (testnext(ls, TK_ELSE))
            single_block(ls); /* 'else' part */
        ichecknext(ls, TK_END);
        luaK_patchtohere(fs, escapelist[0]);
    }

    // static void whenstat(LexState *ls, int line, expdesc *v) {
    // /* whenstat -> SWITCH control CASE value THEN block [DEFAULT block] END */
    // expdesc whenFunc;
    // int escapelist = NO_JUMP; /* exit list for finished parts */
    // expdesc control;
    // luaX_next(ls); /* skip WHEN */
    // FuncState new_fs;
    // BlockCnt bl;
    // new_fs.f = addprototype(ls);
    // new_fs.f->linedefined = line;
    // open_func(ls, &new_fs, &bl);
    // enterlevel(ls);
    //
    // new_localvar(ls, ls->envn);
    // expdesc env;
    // singlevaraux(ls->fs, ls->envn, &env, 1);
    // adjust_assign(ls, 1, 1, &env);
    // adjustlocalvars(ls, 1);
    //
    // expr(ls, &control); /* read control */
    // FuncState *fs = ls->fs;
    // //check(ls, TK_CASE);
    // ichecknext(ls, TK_DO);
    // while (ls->t.token == TK_CASE) {
    // expdesc vt = clone(control);
    // test_case_block(ls, &escapelist, &vt); /* CASE value THEN block */
    // }
    // if (testnext(ls, TK_DEFAULT)) {
    // block(ls); /* DEFAULT block */
    // }
    // check_match(ls, TK_END, TK_WHEN, line);
    // luaK_patchtohere(fs, escapelist); /* patch escape list to 'switch' end */
    // lua_assert(ls->fs->f->maxstacksize >= ls->fs->freereg &&
    // ls->fs->freereg >= ls->fs->nactvar);
    // ls->fs->freereg = ls->fs->nactvar; /* free registers */
    // leavelevel(ls);
    // new_fs.f->lastlinedefined = ls->linenumber;
    // if (v != NULL) {
    //// LOGE("whenstat v->u.info %d", v->u.info);
    // codeclosure(ls, v);
    //// LOGE("whenstat v->k %d", v->k);
    // } else {
    // codeclosure(ls, &whenFunc);
    // luaK_setmultret(ls->fs, &whenFunc);
    // }
    // new_fs.f->is_vararg = 0;
    // new_fs.f->numparams = 0;
    // close_func(ls);
    // fs = ls->fs;// update ls->fs
    // //CALL
    // if (v != NULL) {
    // int base = v->u.info;
    // int nparams = fs->freereg - (base + 1);
    // init_exp(v, VCALL, luaK_codeABC(fs, OP_CALL, base, nparams + 1, 2));
    // luaK_fixline(fs, line);
    // fs->freereg = base + 1; /* call remove function and arguments and leaves
    // (unless changed) one result */
    // } else {
    // luaK_codeABC(fs, OP_CALL, ls->fs->freereg - 1, 1, 0);
    // luaK_fixline(ls->fs, line);
    // }
    // }
    protected static void whenstat(LexState ls, int line, expdesc v) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        expdesc whenFunc = new expdesc();
        int[] escapelist = {NO_JUMP};
        expdesc control = new expdesc();
        luaX_next(ls);
        FuncState new_fs = new FuncState();
        BlockCnt bl = new BlockCnt();
        new_fs.f = addprototype(ls);
        new_fs.f.linedefined = line;
        new_fs.f.startidx = ls.currentidx;
        open_func(ls, new_fs, bl);
        enterlevel(ls);
        new_localvar(ls, ls.envn);
        expdesc env = new expdesc();
        singlevaraux(ls.fs, ls.envn, env, 1);
        adjust_assign(ls, 1, 1, env);
        adjustlocalvars(ls, 1);
        expr(ls, control);
        ichecknext(ls, TK_DO);
        while (ls.t.token == TK_CASE) {
            expdesc vt = clone(control);
            test_case_block(ls, escapelist, vt);
        }
        if (testnext(ls, TK_DEFAULT)) {
            block(ls);
        }
        if (ls.L.inLexer)
            AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
        check_match(ls, TK_END, TK_WHEN, line);
        luaK_patchtohere(ls.fs, escapelist[0]);
        ls.fs.freereg = ls.fs.nactvar;
        leavelevel(ls);
        new_fs.f.endidx = ls.currentidx;
        new_fs.f.lastlinedefined = ls.linenumber;
        if (v != null) {
            codeclosure(ls, v);
        } else {
            codeclosure(ls, whenFunc);
            luaK_setmultret(ls.fs, whenFunc);
        }
        new_fs.f.is_vararg = 0;
        new_fs.f.numparams = 0;
        close_func(ls);
        FuncState fs = ls.fs;
        if (v != null) {
            int base = v.u.info;
            int nparams = fs.freereg - (base + 1);
            init_exp(v, VCALL, luaK_codeABC(fs, OP_CALL, base, nparams + 1, 2));
            luaK_fixline(fs, line);
            fs.freereg = (base + 1);
        } else {
            luaK_codeABC(fs, OP_CALL, ls.fs.freereg - 1, 1, 0);
            luaK_fixline(ls.fs, line);
        }
    }

    // static expdesc clone(expdesc e2) {
    // expdesc e1;
    // e1.f = e2.f;
    // e1.k = e2.k;
    // e1.t = e2.t;
    // e1.u.ind.t = e2.u.ind.t;
    // e1.u.ind.idx = e2.u.ind.idx;
    // e1.u.ind.vt = e2.u.ind.vt;
    // e1.u.info = e2.u.info;
    // e1.u.nval = e2.u.nval;
    // e1.u.ival = e2.u.ival;
    // return e1;
    // }
    protected static expdesc clone(expdesc e2) {
        expdesc e1 = new expdesc();
        e1.f = e2.f;
        e1.k = e2.k;
        e1.t = e2.t;
        e1.u.ind.t = e2.u.ind.t;
        e1.u.ind.idx = e2.u.ind.idx;
        e1.u.ind.vt = e2.u.ind.vt;
        e1.u.info = e2.u.info;
        e1.u.nval = e2.u.nval;
        e1.u.ival = e2.u.ival;
        return e1;
    }

    // static void test_case_block(LexState *ls, int *escapelist, expdesc *control)
    // {
    // /* test_case_block -> CASE value THEN block */
    // BlockCnt bl;
    // FuncState *fs = ls->fs;
    // expdesc v;
    // expdesc gv = clone(*control);
    // int jf; /* instruction to skip 'then' code (if condition is false) */
    //
    // luaX_next(ls); /* skip CASE */
    // enterlevel(ls);
    // luaK_infix(ls->fs, OPR_EQ, control);
    // expr(ls, &v); /* read condition */
    // luaK_posfix(ls->fs, OPR_EQ, control, &v, ls->linenumber);
    // while (testnext(ls, ',')) {
    // expdesc c = clone(gv);
    // expdesc v2;
    // luaK_infix(ls->fs, OPR_EQ, &c);
    // expr(ls, &v2); /* read condition */
    // luaK_posfix(ls->fs, OPR_EQ, &c, &v2, ls->linenumber);
    // luaK_infix(ls->fs, OPR_OR, control);
    // luaK_posfix(ls->fs, OPR_OR, control, &c, ls->linenumber);
    // }
    // leavelevel(ls);
    // ichecknext(ls, TK_THEN);
    //
    // if (ls->t.token == TK_GOTO || ls->t.token == TK_BREAK || ls->t.token ==
    // TK_CONTINUE) {
    // luaK_goiffalse(ls->fs, control); /* will jump to label if condition is true
    // */
    // enterblock(fs, &bl, 0); /* must enter block before 'goto' */
    // gotostat(ls, control->t); /* handle goto/break */
    //
    // //skipnoopstat(ls); /* skip other no-op statements */
    // //mod by nirenr
    // while (testnext(ls, ';')) {} /* skip semicolons */
    //
    // if (block_follow(ls, 0)) { /* 'goto' is the entire block? */
    // leaveblock(fs);
    // return; /* and that is it */
    // } else /* must skip over 'then' part if condition is false */
    // //jf = luaK_jump(fs);
    // luaX_syntaxerror(ls, "unreachable statement");
    // } else { /* regular case (not goto/break) */
    // luaK_goiftrue(ls->fs, control); /* skip over block if condition is false */
    // enterblock(fs, &bl, 0);
    // jf = control->f;
    // }
    // statlist(ls); /* `then' part */
    // leaveblock(fs);
    //
    // if (ls->t.token == TK_CASE ||
    // ls->t.token == TK_DEFAULT) /* followed by 'default'/'case'? */
    // luaK_concat(fs, escapelist, luaK_jump(fs)); /* must jump over it */
    // luaK_patchtohere(fs, jf);
    // }
    protected static void test_case_block(LexState ls, int[] escapelist, expdesc control) throws LuaError {
        BlockCnt bl = new BlockCnt();
        FuncState fs = ls.fs;
        expdesc v = new expdesc();
        expdesc gv = clone(control);
        int jf = 0;
        luaX_next(ls);
        enterlevel(ls);
        luaK_infix(ls.fs, OPR_EQ, control);
        expr(ls, v);
        luaK_posfix(ls.fs, OPR_EQ, control, v, ls.linenumber);
        while (testnext(ls, ',')) {
            expdesc c = clone(gv);
            expdesc v2 = new expdesc();
            luaK_infix(ls.fs, OPR_EQ, c);
            expr(ls, v2);
            luaK_posfix(ls.fs, OPR_EQ, c, v2, ls.linenumber);
            luaK_infix(ls.fs, OPR_OR, control);
            luaK_posfix(ls.fs, OPR_OR, control, c, ls.linenumber);
        }
        leavelevel(ls);
        ichecknext(ls, TK_THEN);
        if (ls.t.token == TK_GOTO || ls.t.token == TK_BREAK || ls.t.token == TK_CONTINUE) {
            luaK_goiffalse(ls.fs, control);
            enterblock(fs, bl, 0);
            gotostat(ls, control.t);
            while (testnext(ls, ';')) {
            }
            if (block_follow(ls, 0)) {
                leaveblock(fs);
                return;
            } else
                luaX_syntaxerror(ls, "发现无法抵达的语句");
        } else {
            luaK_goiftrue(ls.fs, control);
            enterblock(fs, bl, 0);
            jf = control.f;
        }
        statlist(ls);
        leaveblock(fs);
        if (ls.t.token == TK_CASE || ls.t.token == TK_DEFAULT)
            luaK_concat(fs, escapelist, luaK_jump(fs));
        luaK_patchtohere(fs, jf);
    }

    // static void switchstat(LexState *ls, int line) {
    // /* switchstat -> SWITCH control CASE value THEN block [DEFAULT block] END */
    // int escapelist = NO_JUMP; /* exit list for finished parts */
    // expdesc control;
    // luaX_next(ls); /* skip SWITCH */
    // expr(ls, &control); /* read control */
    // FuncState *fs = ls->fs;
    // //check(ls, TK_CASE);
    // ichecknext(ls, TK_DO);
    // while (ls->t.token == TK_CASE) {
    // expdesc vt = clone(control);
    // test_case_block(ls, &escapelist, &vt); /* CASE value THEN block */
    // }
    // if (testnext(ls, TK_DEFAULT)) {
    // block(ls); /* DEFAULT block */
    // }
    // check_match(ls, TK_END, TK_SWITCH, line);
    // luaK_patchtohere(fs, escapelist); /* patch escape list to 'switch' end */
    // }
    protected static void switchstat(LexState ls, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        int[] escapelist = {NO_JUMP};
        expdesc control = new expdesc();
        luaX_next(ls);
        expr(ls, control);
        FuncState fs = ls.fs;
        ichecknext(ls, TK_DO);
        while (ls.t.token == TK_CASE) {
            expdesc vt = clone(control);
            test_case_block(ls, escapelist, vt);
        }
        if (testnext(ls, TK_DEFAULT)) {
            block(ls);
        }
        if (ls.L.inLexer)
            AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
        check_match(ls, TK_END, TK_SWITCH, line);
        luaK_patchtohere(fs, escapelist[0]);
    }

    // static void localfunc(LexState *ls) {
    // expdesc b;
    // FuncState *fs = ls->fs;
    // //mod by nwdxlgzs
    // eatTag(ls, 1);
    // new_localvar(ls, str_checkname(ls)); /* new local variable */
    // adjustlocalvars(ls, 1); /* enter its scope */
    // body(ls, &b, 0, ls->linenumber); /* function created in next register */
    // /* debug information will only see the variable after this point! */
    // getlocvar(fs, b.u.info)->startpc = fs->pc;
    // }
    protected static void localfunc(LexState ls) throws LuaError {
        expdesc b = new expdesc();
        FuncState fs = ls.fs;
        eatTag(ls, true);
        new_localvar(ls, str_checkname(ls));
        adjustlocalvars(ls, 1);
        body(ls, b, 0, ls.linenumber);
        getlocvar(fs, b.u.info).startpc = fs.pc;
    }

    // static void closefunc(LexState *ls, FuncState *fs) {
    // new_localvar(ls, luaS_newliteral(ls->L, "(defer)"));
    // exp1(ls);
    // markupval(fs, fs->nactvar);
    // adjustlocalvars(ls, 1);
    // luaK_codeABC(fs, OP_TBC, fs->nactvar - 1, 0, 0);
    // }
    protected static void closefunc(LexState ls, FuncState fs) throws LuaError {
        new_localvar(ls, "(defer)");
        exp1(ls);
        markupval(fs, fs.nactvar);
        adjustlocalvars(ls, 1);
        luaK_codeABC(fs, OP_TBC, fs.nactvar - 1, 0, 0);
    }

    // static void commonlocalstat(LexState *ls) {
    // /* stat -> LOCAL NAME {',' NAME} ['=' explist] */
    // int nvars = 0;
    // int nexps;
    // if (testnext(ls, '<')) {
    // str_checkname(ls);
    // checknext(ls, '>');
    // }
    // expdesc e;
    // do {
    // new_localvar(ls, str_checkname(ls));
    // nvars++;
    // } while (testnext(ls, ','));
    // if (testnext(ls, '='))
    // nexps = explist(ls, &e);
    // else {
    // e.k = VVOID;
    // nexps = 0;
    // }
    // adjust_assign(ls, nvars, nexps, &e);
    // adjustlocalvars(ls, nvars);
    // }
    protected static void commonlocalstat(LexState ls) throws LuaError {
        int nvars = 0;
        int nexps;
        if (testnext(ls, '<')) {
            str_checkname(ls);
            checknext(ls, '>');
        }
        expdesc e = new expdesc();
        do {
            new_localvar(ls, str_checkname(ls));
            nvars++;
        } while (testnext(ls, ','));
        if (testnext(ls, '='))
            nexps = explist(ls, e);
        else {
            e.k = VVOID;
            nexps = 0;
        }
        adjust_assign(ls, nvars, nexps, e);
        adjustlocalvars(ls, nvars);
    }

    // static void tocloselocalstat(LexState *ls) {
    // FuncState *fs = ls->fs;
    // /*TString *attr = str_checkname(ls);
    // if (strcmp(getstr(attr), "toclose") != 0)
    // luaK_semerror(ls,
    // luaO_pushfstring(ls->L, "unknown attribute '%s'", getstr(attr)));*/
    // if (itestnext(ls, TK_FUNCTION)) {
    // closefunc(ls, fs);
    // return;
    // }
    // new_localvar(ls, str_checkname(ls));
    // checknext(ls, '=');
    // exp1(ls);
    // markupval(fs, fs->nactvar);
    // adjustlocalvars(ls, 1);
    // luaK_codeABC(fs, OP_TBC, fs->nactvar - 1, 0, 0);
    // }
    protected static void tocloselocalstat(LexState ls) throws LuaError {
        FuncState fs = ls.fs;
        if (itestnext(ls, TK_FUNCTION)) {
            closefunc(ls, fs);
            return;
        }
        new_localvar(ls, str_checkname(ls));
        checknext(ls, '=');
        exp1(ls);
        markupval(fs, fs.nactvar);
        adjustlocalvars(ls, 1);
        luaK_codeABC(fs, OP_TBC, fs.nactvar - 1, 0, 0);
    }

    // static void selflocalstat(LexState *ls) {
    // FuncState *fs = ls->fs;
    // if (itestnext(ls, TK_FUNCTION)) {
    // closefunc(ls, fs);
    // return;
    // }
    // int nvars = 0;
    // expdesc e;
    // do {
    // //str_checkname_nonext
    // TString *ts;
    // //check(ls, TK_NAME);
    // //mod by nirenr
    // if (ls->t.token != TK_NAME && (ls->t.token > TK_WHILE || ls->t.token <
    // FIRST_RESERVED))
    // error_expected(ls, TK_NAME);
    // ts = ls->t.seminfo.ts;
    // //localvar声明和赋值
    // new_localvar(ls, ts);
    // expr(ls, &e);
    // luaK_exp2nextreg(fs, &e);
    // nvars++;
    // } while (testnext(ls, ','));
    // adjust_assign(ls, nvars, nvars, &e);
    // adjustlocalvars(ls, nvars);
    // }
    protected static void selflocalstat(LexState ls) throws LuaError {
        FuncState fs = ls.fs;
        if (itestnext(ls, TK_FUNCTION)) {
            closefunc(ls, fs);
            return;
        }
        int nvars = 0;
        expdesc e = new expdesc();
        do {
            String ts;
            if (ls.t.token != TK_NAME && (ls.t.token > TK_WHILE || ls.t.token < FIRST_RESERVED))
                error_expected(ls, TK_NAME);
            ts = ls.t.seminfo.ts;
            new_localvar(ls, ts);
            expr(ls, e);
            luaK_exp2nextreg(fs, e);
            nvars++;
        } while (testnext(ls, ','));
        adjust_assign(ls, nvars, nvars, e);
        adjustlocalvars(ls, nvars);
    }

    // static void localstat(LexState *ls) {
    // /* stat -> LOCAL NAME {',' NAME} ['=' explist]
    // | LOCAL *toclose NAME '=' exp */
    // if (testnext(ls, '*'))
    // tocloselocalstat(ls);
    // else if (testnext(ls, ':'))
    // selflocalstat(ls);
    // else
    // commonlocalstat(ls);
    // }
    protected static void localstat(LexState ls) throws LuaError {
        if (testnext(ls, '*'))
            tocloselocalstat(ls);
        else if (testnext(ls, ':'))
            selflocalstat(ls);
        else
            commonlocalstat(ls);
    }

    // static void deferbody(LexState *ls, expdesc *e, int line) {
    // FuncState new_fs;
    // BlockCnt bl;
    // new_fs.f = addprototype(ls);
    // new_fs.f->linedefined = line;
    // open_func(ls, &new_fs, &bl);
    /// * if (testnext(ls, '(')) {
    // parlist(ls);
    // checknext(ls, ')');
    // }*/
    // statement(ls);
    // new_fs.f->lastlinedefined = ls->linenumber;
    // codeclosure(ls, e);
    // close_func(ls);
    // }
    protected static void deferbody(LexState ls, expdesc e, int line) throws LuaError {
        FuncState new_fs = new FuncState();
        BlockCnt bl = new BlockCnt();
        new_fs.f = addprototype(ls);
        new_fs.f.linedefined = line;
        new_fs.f.startidx = ls.currentidx;
        open_func(ls, new_fs, bl);
        statement(ls);
        new_fs.f.endidx = ls.currentidx;
        new_fs.f.lastlinedefined = ls.linenumber;
        codeclosure(ls, e);
        close_func(ls);
    }

    // static void deferstat(LexState *ls) {
    // expdesc b;
    // FuncState *fs = ls->fs;
    // new_localvar(ls, luaS_newliteral(ls->L, "(defer)")); /* new local variable */
    // deferbody(ls, &b, ls->linenumber); /* function created in next register */
    // markupval(fs, fs->nactvar);
    // adjustlocalvars(ls, 1); /* enter its scope */
    // luaK_codeABC(fs, OP_TBC, fs->nactvar - 1, 0, 0);
    // /* debug information will only see the variable after this point! */
    // getlocvar(fs, b.u.info)->startpc = fs->pc;
    // }
    protected static void deferstat(LexState ls) throws LuaError {
        expdesc b = new expdesc();
        FuncState fs = ls.fs;
        new_localvar(ls, "(defer)");
        deferbody(ls, b, ls.linenumber);
        markupval(fs, fs.nactvar);
        adjustlocalvars(ls, 1);
        luaK_codeABC(fs, OP_TBC, fs.nactvar - 1, 0, 0);
        getlocvar(fs, b.u.info).startpc = fs.pc;
    }

    // static int funcname(LexState *ls, expdesc *v) {
    // /* funcname -> NAME {fieldsel} [':' NAME] */
    // int ismethod = 0;
    // singlevar(ls, v);
    // while (ls->t.token == '.' || ls->t.token == TK_LET)
    // fieldsel(ls, v);
    // if (ls->t.token == ':') {
    // ismethod = 1;
    // fieldsel(ls, v);
    // }
    // return ismethod;
    // }
    protected static int funcname(LexState ls, expdesc v) throws LuaError {
        int ismethod = 0;
        singlevar(ls, v);
        while (ls.t.token == '.' || ls.t.token == TK_LET)
            fieldsel(ls, v);
        if (ls.t.token == ':') {
            ismethod = 1;
            fieldsel(ls, v);
        }
        return ismethod;
    }

    // static void funcstat(LexState *ls, int line) {
    // /* funcstat -> FUNCTION funcname body */
    // int ismethod;
    // expdesc v, b;
    // luaX_next(ls); /* skip FUNCTION */
    // //mod by nwdxlgzs
    // eatTag(ls, 1);
    // ismethod = funcname(ls, &v);
    // body(ls, &b, ismethod, line);
    // luaK_storevar(ls->fs, &v, &b);
    // luaK_fixline(ls->fs, line); /* definition "happens" in the first line */
    // }
    protected static void funcstat(LexState ls, int line) throws LuaError {
        int ismethod;
        expdesc v = new expdesc();
        expdesc b = new expdesc();
        luaX_next(ls);
        eatTag(ls, true);
        ismethod = funcname(ls, v);
        body(ls, b, ismethod, line);
        luaK_storevar(ls.fs, v, b);
        luaK_fixline(ls.fs, line);
    }

    // static void freereg(FuncState *fs, int reg) {
    // if (!ISK(reg) && reg >= fs->nactvar) {
    // fs->freereg--;
    // lua_assert(reg == fs->freereg);
    // }
    // }
    protected static void freereg(FuncState fs, int reg) {
        if (!ISK(reg) && reg >= fs.nactvar) {
            fs.freereg--;
        }
    }

    // static void freeexps(FuncState *fs, expdesc *e1, expdesc *e2) {
    // int r1 = (e1->k == VNONRELOC) ? e1->u.info : -1;
    // int r2 = (e2->k == VNONRELOC) ? e2->u.info : -1;
    // if (r1 > r2) {
    // freereg(fs, r1);
    // freereg(fs, r2);
    // } else {
    // freereg(fs, r2);
    // freereg(fs, r1);
    // }
    // }
    protected static void freeexps(FuncState fs, expdesc e1, expdesc e2) {
        int r1 = (e1.k == VNONRELOC) ? e1.u.info : -1;
        int r2 = (e2.k == VNONRELOC) ? e2.u.info : -1;
        if (r1 > r2) {
            freereg(fs, r1);
            freereg(fs, r2);
        } else {
            freereg(fs, r2);
            freereg(fs, r1);
        }
    }

    // static void exprstat(LexState *ls) {
    // /* stat -> func | assignment */
    // FuncState *fs = ls->fs;
    // struct LHS_assign v;
    // suffixedexp(ls, &v.v);
    // //mod by nwdxlgzs
    // if (ls->t.token >= TK_ADDA && ls->t.token <= TK_CONCATA) {
    // BinOpr op = cast(BinOpr, ls->t.token - TK_ADDA);
    // luaX_next(ls);
    // expdesc a = clone(v.v);
    // luaK_infix(fs, op, &a);
    // expdesc e;
    // expr(ls, &e);
    // luaK_posfix(fs, op, &a, &e, ls->linenumber);
    // luaK_storevar(fs, &v.v, &a);
    // } else if (ls->t.token == TK_SELFADD) {
    // luaX_next(ls);
    // expdesc a = clone(v.v);
    // luaK_infix(fs, OPR_ADD, &a);
    // expdesc e;
    // init_exp(&e, VKINT, 0);
    // e.u.ival = 1;
    // luaK_posfix(fs, OPR_ADD, &a, &e, ls->linenumber);
    // luaK_storevar(fs, &v.v, &a);
    // } else if (ls->t.token == '=' || ls->t.token == ',') { /* stat -> assignment
    // ? */
    // v.prev = NULL;
    // assignment(ls, &v, 1);
    // } else { /* stat -> func */
    // check_condition(ls, v.v.k == VCALL, "syntax error");
    // SETARG_C(getinstruction(fs, &v.v), 1); /* call statement uses no results */
    // }
    // }
    protected static void exprstat(LexState ls) throws LuaError {
        FuncState fs = ls.fs;
        LHS_assign v = new LHS_assign();
        suffixedexp(ls, v.v);
        if (ls.t.token >= TK_ADDA && ls.t.token <= TK_CONCATA) {
            int op = ls.t.token - TK_ADDA;
            luaX_next(ls);
            expdesc a = clone(v.v);
            luaK_infix(fs, op, a);
            expdesc e = new expdesc();
            expr(ls, e);
            luaK_posfix(fs, op, a, e, ls.linenumber);
            luaK_storevar(fs, v.v, a);
        } else if (ls.t.token == TK_SELFADD) {
            luaX_next(ls);
            expdesc a = clone(v.v);
            luaK_infix(fs, OPR_ADD, a);
            expdesc e = new expdesc();
            init_exp(e, VKINT, 0);
            e.u.ival = 1;
            luaK_posfix(fs, OPR_ADD, a, e, ls.linenumber);
            luaK_storevar(fs, v.v, a);
        } else if (ls.t.token == '=' || ls.t.token == ',') {
            v.prev = null;
            assignment(ls, v, 1);
        } else {
            check_condition(ls, v.v.k == VCALL, "发现语法错误");
            SETARG_C(getinstruction(fs, v.v), 1);
        }
    }

    // static void luaK_autoOpRet(FuncState *fs, int first, int nret) {
    // luaK_codeABC(fs, fs->trymode ? OP_TRYRETURN : OP_RETURN, first, nret + 1, 0);
    // }
    protected static void luaK_autoOpRet(FuncState fs, int first, int nret) throws LuaError {
        luaK_codeABC(fs, fs.trymode != 0 ? OP_TRYRETURN : OP_RETURN, first, nret + 1, 0);
    }

    // static void retstat(LexState *ls) {
    // /* stat -> RETURN [explist] [';'] */
    // FuncState *fs = ls->fs;
    // expdesc e;
    // int first, nret; /* registers with returned values */
    // if (block_follow(ls, 1) || ls->t.token == ';')
    // first = nret = 0; /* return no values */
    // else {
    // nret = explist(ls, &e); /* optional return values */
    // if (hasmultret(e.k)) {
    // luaK_setmultret(fs, &e);
    // if (e.k == VCALL && nret == 1) { /* tail call? */
    // SET_OPCODE(getinstruction(fs, &e), OP_TAILCALL);
    // lua_assert(GETARG_A(getinstruction(fs, &e)) == fs->nactvar);
    // }
    // first = fs->nactvar;
    // nret = LUA_MULTRET; /* return all values */
    // } else {
    // if (nret == 1) /* only one single value? */
    // first = luaK_exp2anyreg(fs, &e);
    // else {
    // luaK_exp2nextreg(fs, &e); /* values must go to the stack */
    // first = fs->nactvar; /* return all active values */
    // lua_assert(nret == fs->freereg - first);
    // }
    // }
    // }
    // luaK_autoOpRet(fs, first, nret);
    // testnext(ls, ';'); /* skip optional semicolon */
    // }
    protected static void retstat(LexState ls) throws LuaError {
        FuncState fs = ls.fs;
        expdesc e = new expdesc();
        int first, nret;
        if (block_follow(ls, 1) || ls.t.token == ';')
            first = nret = 0;
        else {
            nret = explist(ls, e);
            if (hasmultret(e.k)) {
                luaK_setmultret(fs, e);
                if (e.k == VCALL && nret == 1) {
                    SET_OPCODE(getinstruction(fs, e), OP_TAILCALL);
                }
                first = fs.nactvar;
                nret = LUA_MULTRET;
            } else {
                if (nret == 1)
                    first = luaK_exp2anyreg(fs, e);
                else {
                    luaK_exp2nextreg(fs, e);
                    first = fs.nactvar;
                }
            }
        }
        luaK_autoOpRet(fs, first, nret);
        testnext(ls, ';');
    }

    protected static class OP_VPair {
        protected OP_VPair() {
        }

        protected OP_VPair(String name, int op) {
            this.name = name;
            this.op = op;
        }

        // typedef struct OP_VPair {
        // const char *name;
        // const int op;
        // } OP_VPair;
        protected String name;
        protected int op;
    }

    // static const OP_VPair opVPair[] = {
    // {"OP_MOVE", OP_MOVE},
    // {"OP_LOADK", OP_LOADK},
    // {"OP_LOADKX", OP_LOADKX},
    // {"OP_LOADBOOL", OP_LOADBOOL},
    // {"OP_LOADNIL", OP_LOADNIL},
    // {"OP_GETUPVAL", OP_GETUPVAL},
    // {"OP_GETTABUP", OP_GETTABUP},
    // {"OP_GETTABLE", OP_GETTABLE},
    // {"OP_SETTABUP", OP_SETTABUP},
    // {"OP_SETUPVAL", OP_SETUPVAL},
    // {"OP_SETTABLE", OP_SETTABLE},
    // {"OP_NEWTABLE", OP_NEWTABLE},
    // {"OP_SELF", OP_SELF},
    // {"OP_ADD", OP_ADD},
    // {"OP_SUB", OP_SUB},
    // {"OP_MUL", OP_MUL},
    // {"OP_MOD", OP_MOD},
    // {"OP_POW", OP_POW},
    // {"OP_DIV", OP_DIV},
    // {"OP_IDIV", OP_IDIV},
    // {"OP_BAND", OP_BAND},
    // {"OP_BOR", OP_BOR},
    // {"OP_BXOR", OP_BXOR},
    // {"OP_SHL", OP_SHL},
    // {"OP_SHR", OP_SHR},
    // {"OP_UNM", OP_UNM},
    // {"OP_BNOT", OP_BNOT},
    // {"OP_NOT", OP_NOT},
    // {"OP_LEN", OP_LEN},
    // {"OP_CONCAT", OP_CONCAT},
    // {"OP_JMP", OP_JMP},
    // {"OP_EQ", OP_EQ},
    // {"OP_LT", OP_LT},
    // {"OP_LE", OP_LE},
    // {"OP_TEST", OP_TEST},
    // {"OP_TESTSET", OP_TESTSET},
    // {"OP_CALL", OP_CALL},
    // {"OP_TAILCALL", OP_TAILCALL},
    // {"OP_RETURN", OP_RETURN},
    // {"OP_FORLOOP", OP_FORLOOP},
    // {"OP_FORPREP", OP_FORPREP},
    // {"OP_TFORCALL", OP_TFORCALL},
    // {"OP_TFORLOOP", OP_TFORLOOP},
    // {"OP_SETLIST", OP_SETLIST},
    // {"OP_CLOSURE", OP_CLOSURE},
    // {"OP_VARARG", OP_VARARG},
    // {"OP_EXTRAARG", OP_EXTRAARG},
    // {"OP_TBC", OP_TBC},
    // {"OP_NEWARRAY", OP_NEWARRAY},
    // {"OP_TFOREACH", OP_TFOREACH},
    // {"OP_SECTION", OP_SECTION},
    // {"OP_IS", OP_IS},
    // {"OP_TRY", OP_TRY},
    // {"OP_TRYRETURN", OP_TRYRETURN},
    // };
    protected static final OP_VPair[] opVPair = {
            new OP_VPair("OP_MOVE", OP_MOVE),
            new OP_VPair("OP_LOADK", OP_LOADK),
            new OP_VPair("OP_LOADKX", OP_LOADKX),
            new OP_VPair("OP_LOADBOOL", OP_LOADBOOL),
            new OP_VPair("OP_LOADNIL", OP_LOADNIL),
            new OP_VPair("OP_GETUPVAL", OP_GETUPVAL),
            new OP_VPair("OP_GETTABUP", OP_GETTABUP),
            new OP_VPair("OP_GETTABLE", OP_GETTABLE),
            new OP_VPair("OP_SETTABUP", OP_SETTABUP),
            new OP_VPair("OP_SETUPVAL", OP_SETUPVAL),
            new OP_VPair("OP_SETTABLE", OP_SETTABLE),
            new OP_VPair("OP_NEWTABLE", OP_NEWTABLE),
            new OP_VPair("OP_SELF", OP_SELF),
            new OP_VPair("OP_ADD", OP_ADD),
            new OP_VPair("OP_SUB", OP_SUB),
            new OP_VPair("OP_MUL", OP_MUL),
            new OP_VPair("OP_MOD", OP_MOD),
            new OP_VPair("OP_POW", OP_POW),
            new OP_VPair("OP_DIV", OP_DIV),
            new OP_VPair("OP_IDIV", OP_IDIV),
            new OP_VPair("OP_BAND", OP_BAND),
            new OP_VPair("OP_BOR", OP_BOR),
            new OP_VPair("OP_BXOR", OP_BXOR),
            new OP_VPair("OP_SHL", OP_SHL),
            new OP_VPair("OP_SHR", OP_SHR),
            new OP_VPair("OP_UNM", OP_UNM),
            new OP_VPair("OP_BNOT", OP_BNOT),
            new OP_VPair("OP_NOT", OP_NOT),
            new OP_VPair("OP_LEN", OP_LEN),
            new OP_VPair("OP_CONCAT", OP_CONCAT),
            new OP_VPair("OP_JMP", OP_JMP),
            new OP_VPair("OP_EQ", OP_EQ),
            new OP_VPair("OP_LT", OP_LT),
            new OP_VPair("OP_LE", OP_LE),
            new OP_VPair("OP_TEST", OP_TEST),
            new OP_VPair("OP_TESTSET", OP_TESTSET),
            new OP_VPair("OP_CALL", OP_CALL),
            new OP_VPair("OP_TAILCALL", OP_TAILCALL),
            new OP_VPair("OP_RETURN", OP_RETURN),
            new OP_VPair("OP_FORLOOP", OP_FORLOOP),
            new OP_VPair("OP_FORPREP", OP_FORPREP),
            new OP_VPair("OP_TFORCALL", OP_TFORCALL),
            new OP_VPair("OP_TFORLOOP", OP_TFORLOOP),
            new OP_VPair("OP_SETLIST", OP_SETLIST),
            new OP_VPair("OP_CLOSURE", OP_CLOSURE),
            new OP_VPair("OP_VARARG", OP_VARARG),
            new OP_VPair("OP_EXTRAARG", OP_EXTRAARG),
            new OP_VPair("OP_TBC", OP_TBC),
            new OP_VPair("OP_NEWARRAY", OP_NEWARRAY),
            new OP_VPair("OP_TFOREACH", OP_TFOREACH),
            new OP_VPair("OP_SECTION", OP_SECTION),
            new OP_VPair("OP_IS", OP_IS),
            new OP_VPair("OP_TRY", OP_TRY),
            new OP_VPair("OP_TRYRETURN", OP_TRYRETURN)
    };

    // static void asmstat(LexState *ls) {
    // FuncState *fs = ls->fs;
    // luaX_next(ls);
    // checknext(ls, '(');
    // TString *SC = str_checkname(ls);
    // if (strcmp(getstr(SC),
    // "nwdxlgzs为前瞻版提供的内联汇编方案有权无偿索要内联汇编实现以改进功能或进行审查") !=
    // 0) {
    // luaX_syntaxerror(ls, "asm code header error");
    // }
    // testnext(ls, ',');
    // const TString *iABC_mode = luaS_newliteral(ls->L, "iABC");
    // const TString *iABx_mode = luaS_newliteral(ls->L, "iABx");
    // const TString *iAsBx_mode = luaS_newliteral(ls->L, "iAsBx");
    // const TString *iAx_mode = luaS_newliteral(ls->L, "iAx");
    // const TString *bins_mode = luaS_newliteral(ls->L, "bins");
    // const TString *pass_mode = luaS_newliteral(ls->L, "pass");
    // const TString *setval_mode = luaS_newliteral(ls->L, "setval");
    // while (!testnext(ls, ')')) {//直到遇到右括号
    // //检查统配模式
    // const TString *mode = str_checkname(ls);
    // if (mode == iABC_mode) {
    // checknext(ls, '(');
    // int op, a, b, c;
    // if (ls->t.token == TK_INT) {
    // op = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // TString *opname = str_checkname(ls);
    // int find = 0;
    // for (int i = 0; i < sizeof(opVPair) / sizeof(OP_VPair); i++) {
    // if (strcasecmp(getstr(opname), opVPair[i].name) == 0
    // || strcasecmp(getstr(opname), opVPair[i].name + 3) == 0) {
    // op = opVPair[i].op;
    // find = 1;
    // break;
    // }
    // }
    // if (!find) luaX_syntaxerror(ls, "unexpected opname with iABC mode");
    // } else luaX_syntaxerror(ls, "unexpected token with iABC mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // a = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // expdesc e;
    // singlevar(ls, &e);
    // if (e.k != VLOCAL)
    // luaX_syntaxerror(ls, "unexpected ra name reg index with iABC mode");
    // a = e.u.info;
    // } else luaX_syntaxerror(ls, "unexpected A value with iABC mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // b = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // expdesc e;
    // singlevar(ls, &e);
    // if (e.k != VLOCAL)
    // luaX_syntaxerror(ls, "unexpected rb name reg index with iABC mode");
    // b = e.u.info;
    // } else luaX_syntaxerror(ls, "unexpected B value with iABC mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // c = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // expdesc e;
    // singlevar(ls, &e);
    // if (e.k != VLOCAL)
    // luaX_syntaxerror(ls, "unexpected rc name reg index with iABC mode");
    // c = e.u.info;
    // } else luaX_syntaxerror(ls, "unexpected C value with iABC mode");
    // luaK_codeABC(fs, op, a, b, c);
    // } else if (mode == iABx_mode) {
    // checknext(ls, '(');
    // int op, a, bx;
    // if (ls->t.token == TK_INT) {
    // op = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // TString *opname = str_checkname(ls);
    // int find = 0;
    // for (int i = 0; i < sizeof(opVPair) / sizeof(OP_VPair); i++) {
    // if (strcasecmp(getstr(opname), opVPair[i].name) == 0
    // || strcasecmp(getstr(opname), opVPair[i].name + 3) == 0) {
    // op = opVPair[i].op;
    // find = 1;
    // break;
    // }
    // }
    // if (!find) luaX_syntaxerror(ls, "unexpected opname with iABx mode");
    // } else luaX_syntaxerror(ls, "unexpected token with iABx mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // a = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // expdesc e;
    // singlevar(ls, &e);
    // if (e.k != VLOCAL)
    // luaX_syntaxerror(ls, "unexpected ra name reg index with iABx mode");
    // a = e.u.info;
    // } else luaX_syntaxerror(ls, "unexpected A value with iABx mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // bx = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else luaX_syntaxerror(ls, "unexpected Bx value with iABx mode");
    // luaK_codeABx(fs, op, a, bx);
    // } else if (mode == iAsBx_mode) {
    // checknext(ls, '(');
    // int op, a, sbx;
    // if (ls->t.token == TK_INT) {
    // op = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // TString *opname = str_checkname(ls);
    // int find = 0;
    // for (int i = 0; i < sizeof(opVPair) / sizeof(OP_VPair); i++) {
    // if (strcasecmp(getstr(opname), opVPair[i].name) == 0
    // || strcasecmp(getstr(opname), opVPair[i].name + 3) == 0) {
    // op = opVPair[i].op;
    // find = 1;
    // break;
    // }
    // }
    // if (!find) luaX_syntaxerror(ls, "unexpected opname with iAsBx mode");
    // } else luaX_syntaxerror(ls, "unexpected token with iAsBx mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // a = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // expdesc e;
    // singlevar(ls, &e);
    // if (e.k != VLOCAL)
    // luaX_syntaxerror(ls, "unexpected ra name reg index with iAsBxs mode");
    // a = e.u.info;
    // } else luaX_syntaxerror(ls, "unexpected A value with iAsBx mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // sbx = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else luaX_syntaxerror(ls, "unexpected sBx value with iAsBx mode");
    // luaK_codeAsBx(fs, op, a, sbx);
    // } else if (mode == iAx_mode) {
    // checknext(ls, '(');
    // int op, ax;
    // if (ls->t.token == TK_INT) {
    // op = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else if (ls->t.token == TK_NAME) {
    // TString *opname = str_checkname(ls);
    // int find = 0;
    // for (int i = 0; i < sizeof(opVPair) / sizeof(OP_VPair); i++) {
    // if (strcasecmp(getstr(opname), opVPair[i].name) == 0
    // || strcasecmp(getstr(opname), opVPair[i].name + 3) == 0) {
    // op = opVPair[i].op;
    // find = 1;
    // break;
    // }
    // }
    // if (!find) luaX_syntaxerror(ls, "unexpected opname with iAx mode");
    // } else luaX_syntaxerror(ls, "unexpected token with iAx mode");
    // checknext(ls, ',');
    // if (ls->t.token == TK_INT) {
    // ax = ls->t.seminfo.i;
    // luaX_next(ls);
    // } else luaX_syntaxerror(ls, "unexpected Ax value with iAx mode");
    // luaK_codeABC(fs, op, 0, 0, 0);
    // SETARG_Ax(fs->f->code[fs->pc - 1], ax);
    // } else if (mode == bins_mode) {
    // checknext(ls, '(');
    // const TString *bins = str_check(ls);
    // const int bins_l = tsslen(bins);
    // const char *bins_s = getstr(bins);
    // char bins_c[bins_l + sizeof(Instruction) * 2];
    // memcpy(bins_c, bins_s, bins_l);
    // memset(bins_c + bins_l, 0, sizeof(Instruction) * 2);
    // int codesz = bins_l / sizeof(Instruction);
    // if (bins_l % sizeof(Instruction))codesz++;
    // const int pc = fs->pc;
    // for (int i = 0; i < codesz; i++) {
    // luaK_codeABC(fs, 0, 0, 0, 0);
    // }
    // memcpy(fs->f->code + pc, bins_c, sizeof(Instruction) * codesz);
    // } else if (mode == pass_mode) {
    // checknext(ls, '(');
    // const TString *bins = str_check(ls);
    // const int bins_l = tsslen(bins);
    // const char *bins_s = getstr(bins);
    // char bins_c[bins_l + sizeof(Instruction) * 2];
    // memcpy(bins_c, bins_s, bins_l);
    // memset(bins_c + bins_l, 0, sizeof(Instruction) * 2);
    // int codesz = bins_l / sizeof(Instruction);
    // if (bins_l % sizeof(Instruction))codesz++;
    // luaK_codeAsBx(fs, OP_JMP, 0, codesz);
    // const int pc = fs->pc;
    // for (int i = 0; i < codesz; i++) {
    // luaK_codeABC(fs, 0, 0, 0, 0);
    // }
    // memcpy(fs->f->code + pc, bins_c, sizeof(Instruction) * codesz);
    // } else if (mode == setval_mode) {
    // checknext(ls, '(');
    // expdesc e;
    // if (ls->t.token == TK_INT) {
    // int ra = ls->t.seminfo.i;
    // luaX_next(ls);
    // if (ra < 0) luaX_syntaxerror(ls, "unexpected reg index with const mode");
    // init_exp(&e, VLOCAL, ra);
    // } else if (ls->t.token == TK_NAME) {
    // singlevar(ls, &e);
    // } else luaX_syntaxerror(ls, "unexpected raOrVal value with const mode");
    // checknext(ls, ',');
    // expdesc v;
    // simpleexp(ls, &v);
    // luaK_exp2nextreg(fs, &v);
    // luaK_setoneret(fs, &v);
    // luaK_storevar(fs, &e, &v);
    // } else luaX_syntaxerror(ls, "unexpected asm mode");
    // check_match(ls, ')', '(', ls->linenumber);
    // testnext(ls, ',');
    // }
    // }
    /*
    protected static void asmstat(LexState ls) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        FuncState fs = ls.fs;
        luaX_next(ls);
        checknext(ls, '(');
        if (ls.t.token != TK_NAME) {
            luaX_syntaxerror(ls, "请加入大括号内的内容以激活内联汇编：{nwdxlgzs为前瞻版提供的内联汇编方案有权无偿索要内联汇编实现以改进功能或进行审查}");
        }
        String SC = str_checkname(ls);
        if (!SC.equals("nwdxlgzs为前瞻版提供的内联汇编方案有权无偿索要内联汇编实现以改进功能或进行审查")) {
            luaX_syntaxerror(ls, "内联汇编警示词错误");
        }
        testnext(ls, ',');
        // const TString *iABC_mode = luaS_newliteral(ls->L, "iABC");
        // const TString *iABx_mode = luaS_newliteral(ls->L, "iABx");
        // const TString *iAsBx_mode = luaS_newliteral(ls->L, "iAsBx");
        // const TString *iAx_mode = luaS_newliteral(ls->L, "iAx");
        // const TString *bins_mode = luaS_newliteral(ls->L, "bins");
        // const TString *pass_mode = luaS_newliteral(ls->L, "pass");
        // const TString *setval_mode = luaS_newliteral(ls->L, "setval");
        ASMBlock[] ASMs = {
                new ASMBlock("iABC"),
                new ASMBlock("iABx"),
                new ASMBlock("iAsBx"),
                new ASMBlock("iAx"),
                new ASMBlock("bins"),
                new ASMBlock("pass"),
                new ASMBlock("setval")
        };
        for (ASMBlock a : ASMs) {
            a.startidx = ls.lastnameidx;
        }
        int eidx = 0;
        while (!testnext(ls, ')')) {
            String mode = str_checkname(ls);
            if (mode.equals("iABC")) {
                checknext(ls, '(');
                long op = 0, a = 0, b = 0, c = 0;
                if (ls.t.token == TK_INT) {
                    op = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    String opname = str_checkname(ls);
                    int find = 0;
                    for (OP_VPair anOpVPair : opVPair) {
                        if (opname.equalsIgnoreCase(anOpVPair.name) ||
                                opname.equalsIgnoreCase(anOpVPair.name.substring(3))) {
                            op = anOpVPair.op;
                            find = 1;
                            break;
                        }
                    }
                    if (find == 0)
                        luaX_syntaxerror(ls, "内联汇编的iABC模式中发现未知的OP");
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABC模式中发现未被期待的内容");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    a = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    expdesc e = new expdesc();
                    singlevar(ls, e);
                    if (e.k != VLOCAL)
                        luaX_syntaxerror(ls, "内联汇编的iABC模式中RA使用了一个非寄存器模式的变量名");
                    a = e.u.info;
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABC模式中发现了一个不被期待的A值");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    b = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    expdesc e = new expdesc();
                    singlevar(ls, e);
                    if (e.k != VLOCAL)
                        luaX_syntaxerror(ls, "内联汇编的iABC模式中RB使用了一个非寄存器模式的变量名");
                    b = e.u.info;
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABC模式中发现了一个不被期待的B值");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    c = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    expdesc e = new expdesc();
                    singlevar(ls, e);
                    if (e.k != VLOCAL)
                        luaX_syntaxerror(ls, "内联汇编的iABC模式中RC使用了一个非寄存器模式的变量名");
                    c = e.u.info;
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABC模式中发现了一个不被期待的C值");
                luaK_codeABC(fs, (int) op, (int) a, (int) b, (int) c);
            } else if (mode.equals("iABx")) {
                checknext(ls, '(');
                long op = 0, a = 0, bx = 0;
                if (ls.t.token == TK_INT) {
                    op = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    String opname = str_checkname(ls);
                    int find = 0;
                    for (OP_VPair anOpVPair : opVPair) {
                        if (opname.equalsIgnoreCase(anOpVPair.name) ||
                                opname.equalsIgnoreCase(anOpVPair.name.substring(3))) {
                            op = anOpVPair.op;
                            find = 1;
                            break;
                        }
                    }
                    if (find == 0)
                        luaX_syntaxerror(ls, "内联汇编的iABx模式中发现未知的OP");
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABx模式中发现了一个不被期待的内容");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    a = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    expdesc e = new expdesc();
                    singlevar(ls, e);
                    if (e.k != VLOCAL)
                        luaX_syntaxerror(ls, "内联汇编的iABx模式中RA使用了一个非寄存器模式的变量名");
                    a = e.u.info;
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABx模式中发现了一个不被期待的A值");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    bx = ls.t.seminfo.i;
                    luaX_next(ls);
                } else
                    luaX_syntaxerror(ls, "内联汇编的iABx模式中发现了一个不被期待的Bx值");
                luaK_codeABx(fs, (int) op, (int) a, (int) bx);
            } else if (mode.equals("iAsBx")) {
                checknext(ls, '(');
                long op = 0, a = 0, sbx = 0;
                if (ls.t.token == TK_INT) {
                    op = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    String opname = str_checkname(ls);
                    int find = 0;
                    for (OP_VPair anOpVPair : opVPair) {
                        if (opname.equalsIgnoreCase(anOpVPair.name) ||
                                opname.equalsIgnoreCase(anOpVPair.name.substring(3))) {
                            op = anOpVPair.op;
                            find = 1;
                            break;
                        }
                    }
                    if (find == 0)
                        luaX_syntaxerror(ls, "内联汇编的iAsBx模式中发现未知的OP");
                } else
                    luaX_syntaxerror(ls, "内联汇编的iAsBx模式中发现了一个不被期待的内容");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    a = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    expdesc e = new expdesc();
                    singlevar(ls, e);
                    if (e.k != VLOCAL)
                        luaX_syntaxerror(ls, "内联汇编的iAsBx模式中RA使用了一个非寄存器模式的变量名");
                    a = e.u.info;
                } else
                    luaX_syntaxerror(ls, "内联汇编的iAsBx模式中发现了一个不被期待的A值");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    sbx = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == '-') {
                    luaX_next(ls);
                    if (ls.t.token == TK_INT) {
                        sbx = -ls.t.seminfo.i;
                        luaX_next(ls);
                    } else
                        luaX_syntaxerror(ls, "内联汇编的iAsBx模式中发现了一个不被期待的sBx值");
                } else
                    luaX_syntaxerror(ls, "内联汇编的iAsBx模式中发现了一个不被期待的sBx值");
                luaK_codeAsBx(fs, (int) op, (int) a, (int) sbx);
            } else if (mode.equals("iAx")) {
                checknext(ls, '(');
                long op = 0, ax = 0;
                if (ls.t.token == TK_INT) {
                    op = ls.t.seminfo.i;
                    luaX_next(ls);
                } else if (ls.t.token == TK_NAME) {
                    String opname = str_checkname(ls);
                    int find = 0;
                    for (OP_VPair anOpVPair : opVPair) {
                        if (opname.equalsIgnoreCase(anOpVPair.name) ||
                                opname.equalsIgnoreCase(anOpVPair.name.substring(3))) {
                            op = anOpVPair.op;
                            find = 1;
                            break;
                        }
                    }
                    if (find == 0)
                        luaX_syntaxerror(ls, "内联汇编的iAx模式中发现未知的OP");
                } else
                    luaX_syntaxerror(ls, "内联汇编的iAx模式中发现了一个不被期待的内容");
                checknext(ls, ',');
                if (ls.t.token == TK_INT) {
                    ax = ls.t.seminfo.i;
                    luaX_next(ls);
                } else
                    luaX_syntaxerror(ls, "内联汇编的iAx模式中发现了一个不被期待的Ax值");
                luaK_codeABC(fs, (int) op, 0, 0, 0);
                SETARG_Ax(fs.f.code[fs.pc - 1], (int) ax);
            } else if (mode.equals("bins")) {
                checknext(ls, '(');
                String bins = str_check(ls);
                byte[] bins_s = bins.getBytes();
                int bins_l = bins_s.length;
                byte[] bins_c = new byte[bins_l + 2 * 4];
                System.arraycopy(bins_s, 0, bins_c, 0, bins_l);
                Arrays.fill(bins_c, bins_l, bins_l + 2 * 4, (byte) 0);
                int codesz = bins_l / 4;
                if (bins_l % 4 != 0)
                    codesz++;
                int pc = fs.pc;
                for (int i = 0; i < codesz; i++) {
                    luaK_codeABC(fs, 0, 0, 0, 0);
                }
                Instruction[] bins_c_i = new Instruction[codesz];
                for (int i = 0; i < codesz; i++) {
                    bins_c_i[i] = new Instruction();
                    bins_c_i[i].i = (bins_c[i * 4] & 0xff) | ((bins_c[i * 4 + 1] & 0xff) << 8)
                            | ((bins_c[i * 4 + 2] & 0xff) << 16) | ((bins_c[i * 4 + 3] & 0xff) << 24);
                }
                memcpy(fs.f.code, pc, bins_c_i, codesz);
            } else if (mode.equals("pass")) {
                checknext(ls, '(');
                String bins = str_check(ls);
                byte[] bins_s = bins.getBytes();
                int bins_l = bins_s.length;
                byte[] bins_c = new byte[bins_l + 2 * 4];
                System.arraycopy(bins_s, 0, bins_c, 0, bins_l);
                Arrays.fill(bins_c, bins_l, bins_l + 2 * 4, (byte) 0);
                int codesz = bins_l / 4;
                if (bins_l % 4 != 0)
                    codesz++;
                luaK_codeAsBx(fs, OP_JMP, 0, codesz);
                int pc = fs.pc;
                for (int i = 0; i < codesz; i++) {
                    luaK_codeABC(fs, 0, 0, 0, 0);
                }
                Instruction[] bins_c_i = new Instruction[codesz];
                for (int i = 0; i < codesz; i++) {
                    bins_c_i[i] = new Instruction();
                    bins_c_i[i].i = (bins_c[i * 4] & 0xff) | ((bins_c[i * 4 + 1] & 0xff) << 8)
                            | ((bins_c[i * 4 + 2] & 0xff) << 16) | ((bins_c[i * 4 + 3] & 0xff) << 24);
                }
                memcpy(fs.f.code, pc, bins_c_i, codesz);
            } else if (mode.equals("setval")) {
                checknext(ls, '(');
                expdesc e = new expdesc();
                if (ls.t.token == TK_INT) {
                    long ra = ls.t.seminfo.i;
                    luaX_next(ls);
                    if (ra < 0)
                        luaX_syntaxerror(ls, "内联汇编的setval模式中寄存器地址不能为负数");
                    init_exp(e, VLOCAL, (int) ra);
                } else if (ls.t.token == TK_NAME) {
                    singlevar(ls, e);
                } else
                    luaX_syntaxerror(ls, "内联汇编的setval模式中发现一个既不是寄存器地址也不是变量名的值");
                checknext(ls, ',');
                expdesc v = new expdesc();
                simpleexp(ls, v);
                luaK_exp2nextreg(fs, v);
                luaK_setoneret(fs, v);
                luaK_storevar(fs, e, v);
            } else
                luaX_syntaxerror(ls, "内联汇编中使用了不受支持的模式：" + mode);
            check_match(ls, ')', '(', ls.linenumber);
            testnext(ls, ',');
            if (ls.t.token == ')') {
                eidx = ls.currentidx - 1;
                if (ls.L.inLexer)
                    AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 1, ls.linenumber));
            }
        }
        for (ASMBlock a : ASMs) {
            a.endidx = eidx;
            fs.f.asm_blocks.add(a);
        }
    }*/

    // static int trybody(LexState *ls, expdesc *e, int token, int line) {
    // FuncState new_fs = {0};
    // new_fs.trymode = 1;
    // BlockCnt bl;
    // new_fs.f = addprototype(ls);
    // new_fs.f->linedefined = line;
    // open_func(ls, &new_fs, &bl);
    // if (token == TK_CATCH) {
    // if (testnext(ls, '(')) {
    // parlist(ls, NULL);//不会吧不会吧，有人想catch一个自己预设的参数？
    // checknext(ls, ')');
    // }
    // }
    // int left = testnext(ls, '{');
    // statlist(ls);
    // new_fs.f->lastlinedefined = ls->linenumber;
    // codeclosure(ls, e);
    // close_func(ls);
    // if (left)
    // check_match(ls, '}', token, line);
    // return left;
    // }
    protected static boolean trybody(LexState ls, expdesc e, int token, int line) throws LuaError {
        int startidx = ls.currentidx;
        int startline = ls.linenumber;
        FuncState new_fs = new FuncState();
        new_fs.trymode = 1;
        BlockCnt bl = new BlockCnt();
        new_fs.f = addprototype(ls);
        new_fs.f.linedefined = line;
        new_fs.f.startidx = ls.currentidx;
        open_func(ls, new_fs, bl);
        if (token == TK_CATCH) {
            if (testnext(ls, '(')) {
                parlist(ls, null);
                checknext(ls, ')');
            }
        }
        boolean left = testnext(ls, '{');
        statlist(ls);
        new_fs.f.endidx = ls.currentidx;
        new_fs.f.lastlinedefined = ls.linenumber;
        codeclosure(ls, e);
        close_func(ls, !left);
        if (left) {
            if (ls.L.inLexer) {
                AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.lastidx - 1, ls.linenumber));
            }
            check_match(ls, '}', token, line);
        }
        return left;
    }

    // static void trystat(LexState *ls) {
    // FuncState *fs = ls->fs;
    // int startidx = fs->f->sizecode;
    // int startline = ls->linenumber;
    // int ra = 0, rb = 0, rc = 0;
    // int line = ls->linenumber;
    // luaX_next(ls);
    // expdesc b;
    // int left = trybody(ls, &b, TK_TRY, ls->linenumber);
    // ra = b.u.info;
    // if (testnext(ls, TK_CATCH)) {
    // left = trybody(ls, &b, TK_CATCH, ls->linenumber);
    // rb = b.u.info;
    // }
    // if (testnext(ls, TK_FINALLY)) {
    // left = trybody(ls, &b, TK_FINALLY, ls->linenumber);
    // rc = b.u.info;
    // }
    // if (!left)
    // check_match(ls, TK_END, TK_TRY, line);
    // luaK_codeABC(fs, OP_TRY, ra, rb, rc);
    // }
    protected static void trystat(LexState ls) throws LuaError {
        int startidx = ls.currentidx;
        FuncState fs = ls.fs;
        int ra = 0, rb = 0, rc = 0;
        int line = ls.linenumber;
        luaX_next(ls);
        expdesc b = new expdesc();
        boolean left = trybody(ls, b, TK_TRY, ls.linenumber);
        ra = b.u.info;
        if (testnext(ls, TK_CATCH)) {
            left = trybody(ls, b, TK_CATCH, ls.linenumber);
            rb = b.u.info;
        }
        if (testnext(ls, TK_FINALLY)) {
            left = trybody(ls, b, TK_FINALLY, ls.linenumber);
            rc = b.u.info;
        }
        if (!left) {
            if (ls.L.inLexer)
                AluaParser.LexState_lines.add(new Rect(startidx, line, ls.currentidx - 3, ls.linenumber));
            check_match(ls, TK_END, TK_TRY, line);
        }
        luaK_codeABC(fs, OP_TRY, ra, rb, rc);
    }

    // static void statement(LexState *ls) {
    // int line = ls->linenumber; /* may be needed for error messages */
    // enterlevel(ls);
    // switch (ls->t.token) {
    // case ';': { /* stat -> ';' (empty statement) */
    // luaX_next(ls); /* skip ';' */
    // break;
    // }
    // case TK_TRY: {
    // trystat(ls);
    // break;
    // }
    // case TK__ASM__: {/* stat -> asmstat */
    // asmstat(ls);
    // break;
    // }
    //// case TK_WITH: {//mod by nwdxlgzs /* stat -> withstat */
    //// withstat(ls, line);
    //// break;
    //// }
    // case TK_WHEN: { /* stat -> single_ifstat */
    // single_ifstat(ls, line);
    // break;
    // }
    // case TK_IF: { /* stat -> ifstat */
    // ifstat(ls, line);
    // break;
    // }
    // case TK_SWITCH: { /* stat -> switchstat */
    // switchstat(ls, line);
    // break;
    // }
    // case TK_WHILE: { /* stat -> whilestat */
    // whilestat(ls, line);
    // break;
    // }
    // case TK_DO: { /* stat -> DO block END */
    // luaX_next(ls); /* skip DO */
    // block(ls);
    // check_match(ls, TK_END, TK_DO, line);
    // break;
    // }
    // case TK_FOR: { /* stat -> forstat */
    // forstat(ls, line);
    // break;
    // }
    // case TK_REPEAT: { /* stat -> repeatstat */
    // repeatstat(ls, line);
    // break;
    // }
    // case TK_FUNCTION: { /* stat -> funcstat */
    // funcstat(ls, line);
    // break;
    // }
    // case '$':
    // case TK_LOCAL: { /* stat -> localstat */
    // luaX_next(ls); /* skip LOCAL */
    // if (testnext(ls, TK_FUNCTION)) /* local function? */
    // localfunc(ls);
    // else
    // localstat(ls);
    // break;
    // }
    // case TK_DEFER:
    // luaX_next(ls); /* skip DEFER */
    // deferstat(ls);
    // break;
    // case TK_DBCOLON: { /* stat -> label */
    // luaX_next(ls); /* skip double colon */
    // labelstat(ls, str_checkname(ls), line);
    // break;
    // }
    // case TK_RETURN: { /* stat -> retstat */
    // luaX_next(ls); /* skip RETURN */
    // retstat(ls);
    // break;
    // }
    // case TK_CONTINUE://mod by nirenr
    // case TK_BREAK: { /* stat -> breakstat */
    // gotostat(ls, luaK_jump(ls->fs));
    // while (testnext(ls, ';')) {} /* skip semicolons */
    // if (!block_follow(ls, 1))
    // luaX_syntaxerror(ls, "unreachable statement");
    // break;
    // }
    // case TK_GOTO: { /* stat -> 'goto' NAME */
    // gotostat(ls, luaK_jump(ls->fs));
    // break;
    // }
    // default: { /* stat -> func | assignment */
    // exprstat(ls);
    // break;
    // }
    // }
    // lua_assert(ls->fs->f->maxstacksize >= ls->fs->freereg &&
    // ls->fs->freereg >= ls->fs->nactvar);
    // ls->fs->freereg = ls->fs->nactvar; /* free registers */
    // leavelevel(ls);
    // }
    protected static void statement(LexState ls) throws LuaError {
        int line = ls.linenumber;
        enterlevel(ls);
        switch (ls.t.token) {
            case ';': {
                luaX_next(ls);
                break;
            }
            case TK_TRY: {
                trystat(ls);
                break;
            }
            /*case TK__ASM__: {
                asmstat(ls);
                break;
            }*/
            case TK_WHEN: {
                single_ifstat(ls, line);
                break;
            }
            case TK_IF: {
                ifstat(ls, line);
                break;
            }
            case TK_SWITCH: {
                switchstat(ls, line);
                break;
            }
            case TK_WHILE: {
                whilestat(ls, line);
                break;
            }
            case TK_DO: {
                int startidx = ls.currentidx;
                int startline = ls.linenumber;
                luaX_next(ls);
                block(ls);
                if (ls.L.inLexer)
                    AluaParser.LexState_lines.add(new Rect(startidx, startline, ls.currentidx - 3, ls.linenumber));
                check_match(ls, TK_END, TK_DO, line);
                break;
            }
            case TK_FOR: {
                forstat(ls, line);
                break;
            }
            case TK_REPEAT: {
                repeatstat(ls, line);
                break;
            }
            case TK_FUNCTION: {
                funcstat(ls, line);
                break;
            }
            case '$':
            case TK_LOCAL: {
                luaX_next(ls);
                if (testnext(ls, TK_FUNCTION))
                    localfunc(ls);
                else
                    localstat(ls);
                break;
            }
            case TK_DEFER:
                luaX_next(ls);
                deferstat(ls);
                break;
            case TK_DBCOLON: {
                luaX_next(ls);
                labelstat(ls, str_checkname(ls), line);
                break;
            }
            case TK_RETURN: {
                luaX_next(ls);
                retstat(ls);
                break;
            }
            case TK_CONTINUE:
            case TK_BREAK: {
                gotostat(ls, luaK_jump(ls.fs));
                while (testnext(ls, ';')) {
                }
                if (!block_follow(ls, 1))
                    luaX_syntaxerror(ls, "发现无法抵达的语句");
                break;
            }
            case TK_GOTO: {
                gotostat(ls, luaK_jump(ls.fs));
                break;
            }
            default: {
                exprstat(ls);
                break;
            }
        }
        ls.fs.freereg = ls.fs.nactvar;
        leavelevel(ls);
    }

    // static void mainfunc(LexState *ls, FuncState *fs) {
    // BlockCnt bl;
    // expdesc v;
    // open_func(ls, fs, &bl);
    // fs->f->is_vararg = 2; /* main function is always declared vararg */
    // init_exp(&v, VLOCAL, 0); /* create and... */
    // newupvalue(fs, ls->envn, &v); /* ...set environment upvalue */
    // new_localvar(ls, ls->envn);
    // expdesc env;
    // singlevaraux(ls->fs, ls->envn, &env, 1);
    // adjust_assign(ls, 1, 1, &env);
    // adjustlocalvars(ls, 1);
    // luaX_next(ls); /* read first token */
    // if (ls->t.token == TK_STRING || ls->t.token == '{')
    // retstat(ls);
    // else
    // statlist(ls); /* parse main body */
    // check(ls, TK_EOS);
    // close_func(ls);
    // }
    protected static void mainfunc(LexState ls, FuncState fs) throws LuaError {
        if (ls.L.inLexer) {
            AluaParser.LexState_globals.clear();
            AluaParser.LexState_valueMap.clear();
            AluaParser.LexState_tokens.clear();
            AluaParser.LexState_lines.clear();
            AluaParser.LexState_errormsg = null;
            AluaParser.LexState_errorline = -1;
            AluaParser.LexState_erroridx = -1;
        }
        BlockCnt bl = new BlockCnt();
        expdesc v = new expdesc();
        open_func(ls, fs, bl);
        fs.f.is_vararg = 2;
        init_exp(v, VLOCAL, 0);
        newupvalue(fs, ls.envn, v);
        new_localvar(ls, ls.envn);
        expdesc env = new expdesc();
        singlevaraux(ls.fs, ls.envn, env, 1);
        adjust_assign(ls, 1, 1, env);
        adjustlocalvars(ls, 1);
        luaX_next(ls);
        if (ls.t.token == TK_STRING || ls.t.token == '{')
            retstat(ls);
        else
            statlist(ls);
        check(ls, TK_EOS);
        close_func(ls);
        if (ls.L.inLexer) {
            for (Rect line : AluaParser.LexState_lines) {
                line.top -= 1;
                line.bottom -= 1;
            }
        }
    }

    // LClosure *luaY_parser(lua_State *L, ZIO *z, Mbuffer *buff,
    // Dyndata *dyd, const char *name, int firstchar) {
    // LexState lexstate;
    // FuncState funcstate;
    // LClosure *cl = luaF_newLclosure(L, 1); /* create main closure */
    // setclLvalue(L, L->top, cl); /* anchor it (to avoid being collected) */
    // luaD_inctop(L);
    // lexstate.h = luaH_new(L); /* create table for scanner */
    // sethvalue(L, L->top, lexstate.h); /* anchor it */
    // luaD_inctop(L);
    // funcstate.f = cl->p = luaF_newproto(L);
    // funcstate.f->source = luaS_new(L, name); /* create and anchor TString */
    // lua_assert(iswhite(funcstate.f)); /* do not need barrier here */
    // lexstate.buff = buff;
    // lexstate.dyd = dyd;
    // dyd->actvar.n = dyd->gt.n = dyd->label.n = 0;
    // luaX_setinput(L, &lexstate, z, funcstate.f->source, firstchar);
    // mainfunc(&lexstate, &funcstate);
    // lua_assert(!funcstate.prev && funcstate.nups == 1 && !lexstate.fs);
    // /* all scopes should be correctly finished */
    // lua_assert(dyd->actvar.n == 0 && dyd->gt.n == 0 && dyd->label.n == 0);
    // L->top--; /* remove scanner's table */
    // return cl; /* closure is on the stack, too */
    // }
    protected static LClosure luaY_parser(lua_State L, ZIO z, Mbuffer buff, Dyndata dyd, String name, int firstchar)
            throws LuaError {
        LexState lexstate = new LexState();
        FuncState funcstate = new FuncState();
        LClosure cl = new LClosure();
        lexstate.h = new Table();
        funcstate.f = cl.p = new Proto();
        funcstate.f.source = name;
        lexstate.buff = buff;
        lexstate.dyd = dyd;
        dyd.actvar.n = dyd.gt.n = dyd.label.n = 0;
        luaX_setinput(L, lexstate, z, funcstate.f.source, firstchar);
        mainfunc(lexstate, funcstate);
        // mainfunc.call(lexstate, funcstate);
        return cl;
    }

    protected static LexState D_luaY_parser(lua_State L, ZIO z, Mbuffer buff, Dyndata dyd, String name, int firstchar) {
        LexState lexstate = new LexState();
        FuncState funcstate = new FuncState();
        LClosure cl = new LClosure();
        lexstate.h = new Table();
        funcstate.f = cl.p = new Proto();
        funcstate.f.source = name;
        lexstate.buff = buff;
        lexstate.dyd = dyd;
        dyd.actvar.n = dyd.gt.n = dyd.label.n = 0;
        luaX_setinput(L, lexstate, z, funcstate.f.source, firstchar);
        return lexstate;
    }

}
