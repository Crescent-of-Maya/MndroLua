package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.llex.*;
import static com.luaeditor2.aluasyntax.llimits.*;
import static com.luaeditor2.aluasyntax.lmem.*;
import static com.luaeditor2.aluasyntax.lobject.*;
import static com.luaeditor2.aluasyntax.lopcodes.*;
import static com.luaeditor2.aluasyntax.lparser.*;
import static com.luaeditor2.aluasyntax.ltable.*;
import static com.luaeditor2.aluasyntax.lua.*;
import static com.luaeditor2.aluasyntax.lvm.*;

import com.luaeditor2.aluasyntax.structs.*;

public class lcode {
    protected lcode() {
    }

    //    #define NO_JUMP (-1)
    protected static final int NO_JUMP = -1;

    //    int luaK_stringK(FuncState *fs, TString *s) {
//        TValue o;
//        setsvalue(fs->ls->L, &o, s);
//        return addk(fs, &o, &o);  /* use string itself as key */
//    }
    protected static int luaK_stringK(FuncState fs, String s) {
        TValue o = new TValue(s);
        return addk(fs, o, o);
    }

    //    static int addk(FuncState *fs, TValue *key, TValue *v) {
//        lua_State *L = fs->ls->L;
//        Proto *f = fs->f;
//        TValue *idx = luaH_set(L, fs->ls->h, key);  /* index scanner table */
//        int k, oldsize;
//        if (ttisinteger(idx)) {  /* is there an index there? */
//            k = cast_int(ivalue(idx));
//            /* correct value? (warning: must distinguish floats from integers!) */
//            if (k < fs->nk && ttype(&f->k[k]) == ttype(v) &&
//                    luaV_rawequalobj(&f->k[k], v))
//            return k;  /* reuse index */
//        }
//        /* constant not found; create a new entry */
//        oldsize = f->sizek;
//        k = fs->nk;
//    /* numerical value does not need GC barrier;
//       table has no metatable, so it does not need to invalidate cache */
//        setivalue(idx, k);
//        luaM_growvector(L, f->k, k, f->sizek, TValue, MAXARG_Ax, "constants");
//        while (oldsize < f->sizek) setnilvalue(&f->k[oldsize++]);
//        setobj(L, &f->k[k], v);
//        fs->nk++;
//        luaC_barrier(L, f, v);
//        return k;
//    }
    protected static int addk(FuncState fs, TValue key, TValue v) {
        lua_State L = fs.ls.L;
        Proto f = fs.f;
        TValue idx = luaH_set(L, fs.ls.h, key);
        int k, oldsize;
        if (ttisinteger(idx)) {
            k = (int) (ivalue(idx));
            if (k < fs.nk && ttype(f.k[k]) == ttype(v) &&
                    luaV_rawequalobj(f.k[k], v))
                return k;
        }
        oldsize = f.sizek;
        k = fs.nk;
        setivalue(idx, k);
//        luaM_growvector(L, f.k, k, f.sizek, TValue, MAXARG_Ax, "constants");
        f.k = luaM_realloc(L, f.k, f.sizek, f.sizek + 1);
        f.sizek++;
        while (oldsize < f.sizek) setnilvalue(f.k[oldsize++]);
        setobj(L, f.k[k], v);
        fs.nk++;
        return k;
    }

    //    void luaK_indexed(FuncState *fs, expdesc *t, expdesc *k) {
//        lua_assert(!hasjumps(t) && (vkisinreg(t->k) || t->k == VUPVAL));
//        t->u.ind.t = t->u.info;  /* register or upvalue index */
//        t->u.ind.idx = luaK_exp2RK(fs, k);  /* R/K index for key */
//        t->u.ind.vt = (t->k == VUPVAL) ? VUPVAL : VLOCAL;
//        t->k = VINDEXED;
//    }
    protected static void luaK_indexed(FuncState fs, expdesc t, expdesc k) throws LuaError {
        t.u.ind.t = (byte) t.u.info;  /* register or upvalue index */
        t.u.ind.idx = (short) luaK_exp2RK(fs, k);  /* R/K index for key */
        t.u.ind.vt = (byte) ((t.k == VUPVAL) ? VUPVAL : VLOCAL);
        t.k = VINDEXED;
    }

    //    int luaK_exp2RK(FuncState *fs, expdesc *e) {
//        luaK_exp2val(fs, e);
//        switch (e->k) {  /* move constants to 'k' */
//            case VTRUE:
//                e->u.info = boolK(fs, 1);
//            goto vk;
//            case VFALSE:
//                e->u.info = boolK(fs, 0);
//            goto vk;
//            case VNIL:
//                e->u.info = nilK(fs);
//            goto vk;
//            case VKINT:
//                e->u.info = luaK_intK(fs, e->u.ival);
//            goto vk;
//            case VKFLT:
//                e->u.info = luaK_numberK(fs, e->u.nval);
//            goto vk;
//            case VK:
//                vk:
//                e->k = VK;
//                if (e->u.info <= MAXINDEXRK)  /* constant fits in 'argC'? */
//                    return RKASK(e->u.info);
//                else break;
//            default:
//                break;
//        }
//        /* not a constant in the right range: put it in a register */
//        return luaK_exp2anyreg(fs, e);
//    }
    protected static int luaK_exp2RK(FuncState fs, expdesc e) throws LuaError {
        luaK_exp2val(fs, e);
        switch (e.k) {  /* move constants to 'k' */
            case VTRUE:
                e.u.info = boolK(fs, 1);
                e.k = VK;
                if (e.u.info <= MAXINDEXRK)
                    return RKASK(e.u.info);
                else break;
            case VFALSE:
                e.u.info = boolK(fs, 0);
                e.k = VK;
                if (e.u.info <= MAXINDEXRK)
                    return RKASK(e.u.info);
                else break;
            case VNIL:
                e.u.info = nilK(fs);
                e.k = VK;
                if (e.u.info <= MAXINDEXRK)
                    return RKASK(e.u.info);
                else break;
            case VKINT:
                e.u.info = luaK_intK(fs, e.u.ival);
                e.k = VK;
                if (e.u.info <= MAXINDEXRK)
                    return RKASK(e.u.info);
                else break;
            case VKFLT:
                e.u.info = luaK_numberK(fs, e.u.nval);
                e.k = VK;
                if (e.u.info <= MAXINDEXRK)
                    return RKASK(e.u.info);
                else break;
            case VK:
                e.k = VK;
                if (e.u.info <= MAXINDEXRK)
                    return RKASK(e.u.info);
                else break;
            default:
                break;
        }
        return luaK_exp2anyreg(fs, e);
    }

    //    static int nilK(FuncState *fs) {
//        TValue k, v;
//        setnilvalue(&v);
//        /* cannot use nil as key; instead use table itself to represent nil */
//        sethvalue(fs->ls->L, &k, fs->ls->h);
//        return addk(fs, &k, &v);
//    }s
    protected static int nilK(FuncState fs) {
        TValue k = new TValue();
        TValue v = new TValue();
        setnilvalue(v);
        sethvalue(fs.ls.L, k, fs.ls.h);
        return addk(fs, k, v);
    }

    //    static int boolK(FuncState *fs, int b) {
//        TValue o;
//        setbvalue(&o, b);
//        return addk(fs, &o, &o);  /* use boolean itself as key */
//    }
    protected static int boolK(FuncState fs, int b) {
        TValue o = new TValue();
        setbvalue(o, b);
        return addk(fs, o, o);
    }

    //    void luaK_exp2val(FuncState *fs, expdesc *e) {
//        if (hasjumps(e))
//            luaK_exp2anyreg(fs, e);
//        else
//            luaK_dischargevars(fs, e);
//    }
    protected static void luaK_exp2val(FuncState fs, expdesc e) throws LuaError {
        if (hasjumps(e))
            luaK_exp2anyreg(fs, e);
        else
            luaK_dischargevars(fs, e);
    }

    //#define hasjumps(e)    ((e)->t != (e)->f)
    protected static boolean hasjumps(expdesc e) {
        return ((e).t != (e).f);
    }

    //    int luaK_exp2anyreg(FuncState *fs, expdesc *e) {
//        luaK_dischargevars(fs, e);
//        if (e->k == VNONRELOC) {  /* expression already has a register? */
//            if (!hasjumps(e))  /* no jumps? */
//                return e->u.info;  /* result is already in a register */
//            if (e->u.info >= fs->nactvar) {  /* reg. is not a local? */
//                exp2reg(fs, e, e->u.info);  /* put final result in it */
//                return e->u.info;
//            }
//        }
//        luaK_exp2nextreg(fs, e);  /* otherwise, use next available register */
//        return e->u.info;
//    }
    protected static int luaK_exp2anyreg(FuncState fs, expdesc e) throws LuaError {
        luaK_dischargevars(fs, e);
        if (e.k == VNONRELOC) {
            if (!hasjumps(e))
                return e.u.info;
            if (e.u.info >= fs.nactvar) {
                exp2reg(fs, e, e.u.info);
                return e.u.info;
            }
        }
        luaK_exp2nextreg(fs, e);
        return e.u.info;
    }

    //    void luaK_exp2nextreg(FuncState *fs, expdesc *e) {
//        luaK_dischargevars(fs, e);
//        freeexp(fs, e);
//        luaK_reserveregs(fs, 1);
//        exp2reg(fs, e, fs->freereg - 1);
//    }
    protected static void luaK_exp2nextreg(FuncState fs, expdesc e) throws LuaError {
        luaK_dischargevars(fs, e);
        freeexp(fs, e);
        luaK_reserveregs(fs, 1);
        exp2reg(fs, e, fs.freereg - 1);
    }

    //    void luaK_reserveregs(FuncState *fs, int n) {
//        luaK_checkstack(fs, n);
//        fs->freereg += n;
//    }
    protected static void luaK_reserveregs(FuncState fs, int n) throws LuaError {
        luaK_checkstack(fs, n);
        fs.freereg += n;
    }

    //    void luaK_checkstack(FuncState *fs, int n) {
//        int newstack = fs->freereg + n;
//        if (newstack > fs->f->maxstacksize) {
//            if (newstack >= MAXREGS)
//                luaX_syntaxerror(fs->ls,
//                        "function or expression needs too many registers");
//            fs->f->maxstacksize = cast_byte(newstack);
//        }
//    }
//    #define MAXREGS        255
    protected static final int MAXREGS = 255;

    protected static void luaK_checkstack(FuncState fs, int n) throws LuaError {
        int newstack = fs.freereg + n;
        if (newstack > (fs.f.maxstacksize & 0xff)) {
            if (newstack >= MAXREGS)
                luaX_syntaxerror(fs.ls,
                        "函数或表达式需要太多寄存器，所需数量为" + newstack + "（不得大于" + MAXREGS + "）");
            fs.f.maxstacksize = (byte) newstack;
        }
    }

    //    static void freeexp(FuncState *fs, expdesc *e) {
//        if (e->k == VNONRELOC)
//            freereg(fs, e->u.info);
//    }
    protected static void freeexp(FuncState fs, expdesc e) throws LuaError {
        if (e.k == VNONRELOC)
            freereg(fs, e.u.info);
    }

    //    static void exp2reg(FuncState *fs, expdesc *e, int reg) {
//        discharge2reg(fs, e, reg);
//        if (e->k == VJMP)  /* expression itself is a test? */
//            luaK_concat(fs, &e->t, e->u.info);  /* put this jump in 't' list */
//        if (hasjumps(e)) {
//            int final;  /* position after whole expression */
//            int p_f = NO_JUMP;  /* position of an eventual LOAD false */
//            int p_t = NO_JUMP;  /* position of an eventual LOAD true */
//            if (need_value(fs, e->t) || need_value(fs, e->f)) {
//                int fj = (e->k == VJMP) ? NO_JUMP : luaK_jump(fs);
//                p_f = code_loadbool(fs, reg, 0, 1);
//                p_t = code_loadbool(fs, reg, 1, 0);
//                luaK_patchtohere(fs, fj);
//            }
//            final = luaK_getlabel(fs);
//            patchlistaux(fs, e->f, final, reg, p_f);
//            patchlistaux(fs, e->t, final, reg, p_t);
//        }
//        e->f = e->t = NO_JUMP;
//        e->u.info = reg;
//        e->k = VNONRELOC;
//    }
    protected static void exp2reg(FuncState fs, expdesc e, int reg) throws LuaError {
        discharge2reg(fs, e, reg);
        if (e.k == VJMP) {
            int[] l1 = new int[]{e.t};
            luaK_concat(fs, l1, e.u.info);
            e.t = l1[0];
        }
        if (hasjumps(e)) {
            int finl;
            int p_f = NO_JUMP;
            int p_t = NO_JUMP;
            if (need_value(fs, e.t) || need_value(fs, e.f)) {
                int fj = (e.k == VJMP) ? NO_JUMP : luaK_jump(fs);
                p_f = code_loadbool(fs, reg, 0, 1);
                p_t = code_loadbool(fs, reg, 1, 0);
                luaK_patchtohere(fs, fj);
            }
            finl = luaK_getlabel(fs);
            patchlistaux(fs, e.f, finl, reg, p_f);
            patchlistaux(fs, e.t, finl, reg, p_t);
        }
        e.f = e.t = NO_JUMP;
        e.u.info = reg;
        e.k = VNONRELOC;
    }

    //    void luaK_patchtohere(FuncState *fs, int list) {
//        luaK_getlabel(fs);  /* mark "here" as a jump target */
//        luaK_concat(fs, &fs->jpc, list);
//    }
    protected static void luaK_patchtohere(FuncState fs, int list) throws LuaError {
        luaK_getlabel(fs);
        int[] jpc = new int[]{fs.jpc};
        luaK_concat(fs, jpc, list);
        fs.jpc = jpc[0];
    }

    //    static int code_loadbool(FuncState *fs, int A, int b, int jump) {
//        luaK_getlabel(fs);  /* those instructions may be jump targets */
//        return luaK_codeABC(fs, OP_LOADBOOL, A, b, jump);
//    }
    protected static int code_loadbool(FuncState fs, int A, int b, int jump) throws LuaError {
        luaK_getlabel(fs);
        return luaK_codeABC(fs, OP_LOADBOOL, A, b, jump);
    }

    //    int luaK_getlabel(FuncState *fs) {
//        fs->lasttarget = fs->pc;
//        return fs->pc;
//    }
    protected static int luaK_getlabel(FuncState fs) {
        fs.lasttarget = fs.pc;
        return fs.pc;
    }


    //    int luaK_jump(FuncState *fs) {
//        int jpc = fs->jpc;  /* save list of jumps to here */
//        int j;
//        fs->jpc = NO_JUMP;  /* no more jumps to here */
//        j = luaK_codeAsBx(fs, OP_JMP, 0, NO_JUMP);
//        luaK_concat(fs, &j, jpc);  /* keep them on hold */
//        return j;
//    }
//    #define luaK_codeAsBx(fs, o, A, sBx)    luaK_codeABx(fs,o,A,(sBx)+MAXARG_sBx)
    protected static int luaK_codeAsBx(FuncState fs, int o, int A, int sBx) throws LuaError {
        return luaK_codeABx(fs, o, A, (sBx) + MAXARG_sBx);
    }

    protected static int luaK_jump(FuncState fs) throws LuaError {
        int jpc = fs.jpc;
        int j;
        fs.jpc = NO_JUMP;
        j = luaK_codeAsBx(fs, OP_JMP, 0, NO_JUMP);
        luaK_concat(fs, new int[]{j}, jpc);
        return j;
    }

    //    static int code_loadbool(FuncState *fs, int A, int b, int jump) {

    //    static int need_value(FuncState *fs, int list) {
//        for (; list != NO_JUMP; list = getjump(fs, list)) {
//            Instruction i = *getjumpcontrol(fs, list);
//            if (GET_OPCODE(i) != OP_TESTSET) return 1;
//        }
//        return 0;  /* not found */
//    }
    protected static boolean need_value(FuncState fs, int list) {
        for (; list != NO_JUMP; list = getjump(fs, list)) {
            Instruction i = getjumpcontrol(fs, list);
            if (GET_OPCODE(i) != OP_TESTSET) return true;
        }
        return false;
    }

    //    void luaK_concat(FuncState *fs, int *l1, int l2) {
//        if (l2 == NO_JUMP) return;  /* nothing to concatenate? */
//        else if (*l1 == NO_JUMP)  /* no original list? */
//        *l1 = l2;  /* 'l1' points to 'l2' */
//    else {
//            int list = *l1;
//            int next;
//            while ((next = getjump(fs, list)) != NO_JUMP)  /* find last element */
//                list = next;
//            fixjump(fs, list, l2);  /* last element links to 'l2' */
//        }
//    }
    protected static void luaK_concat(FuncState fs, int[] l1, int l2) throws LuaError {
        if (l2 == NO_JUMP) return;
        else if (l1[0] == NO_JUMP)
            l1[0] = l2;
        else {
            int list = l1[0];
            int next;
            while ((next = getjump(fs, list)) != NO_JUMP)
                list = next;
            fixjump(fs, list, l2);
        }
    }

    //    static void discharge2reg(FuncState *fs, expdesc *e, int reg) {
//        luaK_dischargevars(fs, e);
//        switch (e->k) {
//            case VNIL: {
//                luaK_nil(fs, reg, 1);
//                break;
//            }
//            case VFALSE:
//            case VTRUE: {
//                luaK_codeABC(fs, OP_LOADBOOL, reg, e->k == VTRUE, 0);
//                break;
//            }
//            case VK: {
//                luaK_codek(fs, reg, e->u.info);
//                break;
//            }
//            case VKFLT: {
//                luaK_codek(fs, reg, luaK_numberK(fs, e->u.nval));
//                break;
//            }
//            case VKINT: {
//                luaK_codek(fs, reg, luaK_intK(fs, e->u.ival));
//                break;
//            }
//            case VRELOCABLE: {
//                Instruction *pc = &getinstruction(fs, e);
//                SETARG_A(*pc, reg);  /* instruction will put result in 'reg' */
//                break;
//            }
//            case VNONRELOC: {
//                if (reg != e->u.info)
//                    luaK_codeABC(fs, OP_MOVE, reg, e->u.info, 0);
//                break;
//            }
//            default: {
//                lua_assert(e->k == VJMP);
//                return;  /* nothing to do... */
//            }
//        }
//        e->u.info = reg;
//        e->k = VNONRELOC;
//    }
    protected static void discharge2reg(FuncState fs, expdesc e, int reg) throws LuaError {
        luaK_dischargevars(fs, e);
        switch (e.k) {
            case VNIL: {
                luaK_nil(fs, reg, 1);
                break;
            }
            case VFALSE:
            case VTRUE: {
                luaK_codeABC(fs, OP_LOADBOOL, reg, e.k == VTRUE ? 1 : 0, 0);
                break;
            }
            case VK: {
                luaK_codek(fs, reg, e.u.info);
                break;
            }
            case VKFLT: {
                luaK_codek(fs, reg, luaK_numberK(fs, e.u.nval));
                break;
            }
            case VKINT: {
                luaK_codek(fs, reg, luaK_intK(fs, e.u.ival));
                break;
            }
            case VRELOCABLE: {
                Instruction pc = getinstruction(fs, e);
                SETARG_A(pc, reg);
                break;
            }
            case VNONRELOC: {
                if (reg != e.u.info)
                    luaK_codeABC(fs, OP_MOVE, reg, e.u.info, 0);
                break;
            }
            default: {
                return;
            }
        }
        e.u.info = reg;
        e.k = VNONRELOC;
    }

    //    int luaK_intK(FuncState *fs, lua_Integer n) {
//        TValue k, o;
//        setpvalue(&k, cast(void*, cast(size_t, n)));
//        setivalue(&o, n);
//        return addk(fs, &k, &o);
//    }
    protected static int luaK_intK(FuncState fs, long n) {
        TValue k = new TValue();
        TValue o = new TValue();
        setpvalue(k, n);
        setivalue(o, n);
        return addk(fs, k, o);
    }

    //    static int luaK_numberK(FuncState *fs, lua_Number r) {
//        TValue o;
//        setfltvalue(&o, r);
//        return addk(fs, &o, &o);  /* use number itself as key */
//    }
    protected static int luaK_numberK(FuncState fs, double r) {
        TValue o = new TValue();
        setfltvalue(o, r);
        return addk(fs, o, o);
    }

    //    int luaK_codek(FuncState *fs, int reg, int k) {
//        if (k <= MAXARG_Bx)
//            return luaK_codeABx(fs, OP_LOADK, reg, k);
//        else {
//            int p = luaK_codeABx(fs, OP_LOADKX, reg, 0);
//            codeextraarg(fs, k);
//            return p;
//        }
//    }
    protected static int luaK_codek(FuncState fs, int reg, int k) throws LuaError {
        if (k <= MAXARG_Bx)
            return luaK_codeABx(fs, OP_LOADK, reg, k);
        else {
            int p = luaK_codeABx(fs, OP_LOADKX, reg, 0);
            codeextraarg(fs, k);
            return p;
        }
    }

    //    static int codeextraarg(FuncState *fs, int a) {
//        lua_assert(a <= MAXARG_Ax);
//        return luaK_code(fs, CREATE_Ax(OP_EXTRAARG, a));
//    }
    protected static int codeextraarg(FuncState fs, int a) throws LuaError {
        return luaK_code(fs, CREATE_Ax(OP_EXTRAARG, a));
    }


    //    int luaK_codeABx(FuncState *fs, OpCode o, int a, unsigned int bc) {
//        lua_assert(getOpMode(o) == iABx || getOpMode(o) == iAsBx);
//        lua_assert(getCMode(o) == OpArgN);
//        lua_assert(a <= MAXARG_A && bc <= MAXARG_Bx);
//        return luaK_code(fs, CREATE_ABx(o, a, bc));
//    }
    protected static int luaK_codeABx(FuncState fs, int o, int a, int bc) throws LuaError {
        return luaK_code(fs, CREATE_ABx(o, a, bc));
    }

    //    void luaK_nil(FuncState *fs, int from, int n) {
//        Instruction *previous;
//        int l = from + n - 1;  /* last register to set nil */
//        if (fs->pc > fs->lasttarget) {  /* no jumps to current position? */
//            previous = &fs->f->code[fs->pc - 1];
//            if (GET_OPCODE(*previous) == OP_LOADNIL) {  /* previous is LOADNIL? */
//                int pfrom = GETARG_A(*previous);  /* get previous range */
//                int pl = pfrom + GETARG_B(*previous);
//                if ((pfrom <= from && from <= pl + 1) ||
//                        (from <= pfrom && pfrom <= l + 1)) {  /* can connect both? */
//                    if (pfrom < from) from = pfrom;  /* from = min(from, pfrom) */
//                    if (pl > l) l = pl;  /* l = max(l, pl) */
//                    SETARG_A(*previous, from);
//                    SETARG_B(*previous, l - from);
//                    return;
//                }
//            }  /* else go through */
//        }
//        luaK_codeABC(fs, OP_LOADNIL, from, n - 1, 0);  /* else no optimization */
//    }
    protected static void luaK_nil(FuncState fs, int from, int n) throws LuaError {
        int l = from + n - 1;
        if (fs.pc > fs.lasttarget) {
            Instruction previous = fs.f.code[fs.pc - 1];
            if (GET_OPCODE(previous) == OP_LOADNIL) {
                int pfrom = GETARG_A(previous);
                int pl = pfrom + GETARG_B(previous);
                if ((pfrom <= from && from <= pl + 1) ||
                        (from <= pfrom && pfrom <= l + 1)) {
                    if (pfrom < from) from = pfrom;
                    if (pl > l) l = pl;
                    SETARG_A(previous, from);
                    SETARG_B(previous, l - from);
                    return;
                }
            }
        }
        luaK_codeABC(fs, OP_LOADNIL, from, n - 1, 0);
    }

    //    void luaK_dischargevars(FuncState *fs, expdesc *e) {
//        switch (e->k) {
//            case VLOCAL: {  /* already in a register */
//                e->k = VNONRELOC;  /* becomes a non-relocatable value */
//                break;
//            }
//            case VUPVAL: {  /* move value to some (pending) register */
//                e->u.info = luaK_codeABC(fs, OP_GETUPVAL, 0, e->u.info, 0);
//                e->k = VRELOCABLE;
//                break;
//            }
//            case VINDEXED: {
//                OpCode op;
//                freereg(fs, e->u.ind.idx);
//                if (e->u.ind.vt == VLOCAL) {  /* is 't' in a register? */
//                    freereg(fs, e->u.ind.t);
//                    op = OP_GETTABLE;
//                } else {
//                    lua_assert(e->u.ind.vt == VUPVAL);
//                    op = OP_GETTABUP;  /* 't' is in an upvalue */
//                }
//                e->u.info = luaK_codeABC(fs, op, 0, e->u.ind.t, e->u.ind.idx);
//                e->k = VRELOCABLE;
//                break;
//            }
//            case VVARARG:
//            case VCALL: {
//                luaK_setoneret(fs, e);
//                break;
//            }
//            default:
//                break;  /* there is one value available (somewhere) */
//        }
//    }
    protected static void luaK_dischargevars(FuncState fs, expdesc e) throws LuaError {
        switch (e.k) {
            case VLOCAL: {
                e.k = VNONRELOC;
                break;
            }
            case VUPVAL: {
                e.u.info = luaK_codeABC(fs, OP_GETUPVAL, 0, e.u.info, 0);
                e.k = VRELOCABLE;
                break;
            }
            case VINDEXED: {
                int op;
                freereg(fs, e.u.ind.idx);
                if (e.u.ind.vt == VLOCAL) {
                    freereg(fs, e.u.ind.t);
                    op = OP_GETTABLE;
                } else {
                    op = OP_GETTABUP;
                }
                e.u.info = luaK_codeABC(fs, op, 0, e.u.ind.t, e.u.ind.idx);
                e.k = VRELOCABLE;
                break;
            }
            case VVARARG:
            case VCALL: {
                luaK_setoneret(fs, e);
                break;
            }
            default:
                break;
        }
    }

    //    void luaK_setoneret(FuncState *fs, expdesc *e) {
//        if (e->k == VCALL) {  /* expression is an open function call? */
//            /* already returns 1 value */
//            lua_assert(GETARG_C(getinstruction(fs, e)) == 2);
//            e->k = VNONRELOC;  /* result has fixed position */
//            e->u.info = GETARG_A(getinstruction(fs, e));
//        } else if (e->k == VVARARG) {
//            SETARG_B(getinstruction(fs, e), 2);
//            e->k = VRELOCABLE;  /* can relocate its simple result */
//        }
//    }
    protected static void luaK_setoneret(FuncState fs, expdesc e) throws LuaError {
        if (e.k == VCALL) {
            e.k = VNONRELOC;
            e.u.info = GETARG_A(getinstruction(fs, e));
        } else if (e.k == VVARARG) {
            SETARG_B(getinstruction(fs, e), 2);
            e.k = VRELOCABLE;
        }
    }

    //    #define getinstruction(fs, e)    ((fs)->f->code[(e)->u.info])
    protected static Instruction getinstruction(FuncState fs, expdesc e) {
        return fs.f.code[e.u.info];
    }

    //    static void freereg(FuncState *fs, int reg) {
//        if (!ISK(reg) && reg >= fs->nactvar) {
//            fs->freereg--;
//            lua_assert(reg == fs->freereg);
//        }
//    }
    protected static void freereg(FuncState fs, int reg) throws LuaError {
        if (!ISK(reg) && reg >= fs.nactvar) {
            fs.freereg--;
        }
    }

    //    int luaK_codeABC(FuncState *fs, OpCode o, int a, int b, int c) {
//        lua_assert(getOpMode(o) == iABC);
//        lua_assert(getBMode(o) != OpArgN || b == 0);
//        lua_assert(getCMode(o) != OpArgN || c == 0);
//        lua_assert(a <= MAXARG_A && b <= MAXARG_B && c <= MAXARG_C);
//        return luaK_code(fs, CREATE_ABC(o, a, b, c));
//    }
    protected static int luaK_codeABC(FuncState fs, int o, int a, int b, int c) throws LuaError {
        return luaK_code(fs, CREATE_ABC(o, a, b, c));
    }

    //    static int luaK_code(FuncState *fs, Instruction i) {
//        Proto *f = fs->f;
//        dischargejpc(fs);  /* 'pc' will change */
//        /* put new instruction in code array */
//        luaM_growvector(fs->ls->L, f->code, fs->pc, f->sizecode, Instruction,
//                MAX_INT, "opcodes");
//        f->code[fs->pc] = i;
//        /* save corresponding line information */
//        luaM_growvector(fs->ls->L, f->lineinfo, fs->pc, f->sizelineinfo, int,
//        MAX_INT, "opcodes");
//        f->lineinfo[fs->pc] = fs->ls->lastline;
//        return fs->pc++;
//    }
    protected static int luaK_code(FuncState fs, int i) throws LuaError {
        Proto f = fs.f;
        dischargejpc(fs);
//        luaM_growvector(fs->ls->L, f->code, fs->pc, f->sizecode, Instruction,
//                MAX_INT, "opcodes");
        f.code = luaM_realloc(fs.ls.L, f.code, f.sizecode, f.sizecode + 1);
        f.sizecode++;
        f.code[fs.pc].i = i;
//        luaM_growvector(fs->ls->L, f->lineinfo, fs->pc, f->sizelineinfo, int,
//        MAX_INT, "opcodes");
        f.lineinfo = luaM_realloc(fs.ls.L, f.lineinfo, f.sizelineinfo, f.sizelineinfo + 1);
        f.sizelineinfo++;
        f.lineinfo[fs.pc] = fs.ls.lastline;
        return fs.pc++;
    }

    //    static void dischargejpc(FuncState *fs) {
//        patchlistaux(fs, fs->jpc, fs->pc, NO_REG, fs->pc);
//        fs->jpc = NO_JUMP;
//    }
    protected static void dischargejpc(FuncState fs) throws LuaError {
        patchlistaux(fs, fs.jpc, fs.pc, NO_REG, fs.pc);
        fs.jpc = NO_JUMP;
    }

    //    static void patchlistaux(FuncState *fs, int list, int vtarget, int reg,
//                             int dtarget) {
//        while (list != NO_JUMP) {
//            int next = getjump(fs, list);
//            if (patchtestreg(fs, list, reg))
//                fixjump(fs, list, vtarget);
//            else
//                fixjump(fs, list, dtarget);  /* jump to default target */
//            list = next;
//        }
//    }
    protected static void patchlistaux(FuncState fs, int list, int vtarget, int reg,
                                       int dtarget) throws LuaError {
        while (list != NO_JUMP) {
            int next = getjump(fs, list);
            if (patchtestreg(fs, list, reg))
                fixjump(fs, list, vtarget);
            else
                fixjump(fs, list, dtarget);
            list = next;
        }
    }

    //    static int getjump(FuncState *fs, int pc) {
//        int offset = GETARG_sBx(fs->f->code[pc]);
//        if (offset == NO_JUMP)  /* point to itself represents end of list */
//            return NO_JUMP;  /* end of list */
//        else
//            return (pc + 1) + offset;  /* turn offset into absolute position */
//    }
    protected static int getjump(FuncState fs, int pc) {
        int offset = GETARG_sBx(fs.f.code[pc]);
        if (offset == NO_JUMP)
            return NO_JUMP;
        else
            return (pc + 1) + offset;
    }

    //    static int patchtestreg(FuncState *fs, int node, int reg) {
//        Instruction *i = getjumpcontrol(fs, node);
//        if (GET_OPCODE(*i) != OP_TESTSET)
//        return 0;  /* cannot patch other instructions */
//        if (reg != NO_REG && reg != GETARG_B(*i))
//        SETARG_A(*i, reg);
//    else {
//        /* no register to put value or register already has the value;
//           change instruction to simple test */
//        *i = CREATE_ABC(OP_TEST, GETARG_B(*i), 0, GETARG_C(*i));
//        }
//        return 1;
//    }
    protected static boolean patchtestreg(FuncState fs, int node, int reg) {
        Instruction i = getjumpcontrol(fs, node);
        if (GET_OPCODE(i) != OP_TESTSET)
            return false;
        if (reg != NO_REG && reg != GETARG_B(i))
            SETARG_A(i, reg);
        else {
            i.i = CREATE_ABC(OP_TEST, GETARG_B(i), 0, GETARG_C(i));
        }
        return true;
    }

    //    static Instruction *getjumpcontrol(FuncState *fs, int pc) {
//        Instruction *pi = &fs->f->code[pc];
//        if (pc >= 1 && testTMode(GET_OPCODE(*(pi - 1))))
//        return pi - 1;
//    else
//        return pi;
//    }
    protected static Instruction getjumpcontrol(FuncState fs, int pc) {
        Instruction pi = fs.f.code[pc];
        if (pc < 1)
            return pi;
        else {
            Instruction pi1 = fs.f.code[pc - 1];
            if (testTMode(GET_OPCODE(pi1))) {
                return pi1;
            } else {
                return pi;
            }
        }
    }

    //    static void fixjump(FuncState *fs, int pc, int dest) {
//        Instruction *jmp = &fs->f->code[pc];
//        int offset = dest - (pc + 1);
//        lua_assert(dest != NO_JUMP);
//        if (abs(offset) > MAXARG_sBx)
//            luaX_syntaxerror(fs->ls, "control structure too long");
//        SETARG_sBx(*jmp, offset);
//    }
    protected static void fixjump(FuncState fs, int pc, int dest) throws LuaError {
        Instruction jmp = fs.f.code[pc];
        int offset = dest - (pc + 1);
        if (Math.abs(offset) > MAXARG_sBx)
            luaX_syntaxerror(fs.ls, "控制流结构过长无法跳转");
        SETARG_sBx(jmp, offset);
    }

    //    void luaK_setreturns(FuncState *fs, expdesc *e, int nresults) {
//        if (e->k == VCALL) {  /* expression is an open function call? */
//            SETARG_C(getinstruction(fs, e), nresults + 1);
//        } else if (e->k == VVARARG) {
//            Instruction *pc = &getinstruction(fs, e);
//            SETARG_B(*pc, nresults + 1);
//            SETARG_A(*pc, fs->freereg);
//            luaK_reserveregs(fs, 1);
//        } else
//            lua_assert(nresults == LUA_MULTRET);
//    }
    protected static void luaK_setreturns(FuncState fs, expdesc e, int nresults) throws LuaError {
        if (e.k == VCALL) {
            SETARG_C(getinstruction(fs, e), nresults + 1);
        } else if (e.k == VVARARG) {
            Instruction pc = getinstruction(fs, e);
            SETARG_B(pc, nresults + 1);
            SETARG_A(pc, fs.freereg);
            luaK_reserveregs(fs, 1);
        }
    }

    //    void luaK_patchclose(FuncState *fs, int list, int level) {
//        level++;  /* argument is +1 to reserve 0 as non-op */
//        for (; list != NO_JUMP; list = getjump(fs, list)) {
//            lua_assert(GET_OPCODE(fs->f->code[list]) == OP_JMP &&
//                    (GETARG_A(fs->f->code[list]) == 0 ||
//                            GETARG_A(fs->f->code[list]) >= level));
//            SETARG_A(fs->f->code[list], level);
//        }
//    }
    protected static void luaK_patchclose(FuncState fs, int list, int level) throws LuaError {
        level++;
        for (; list != NO_JUMP; list = getjump(fs, list)) {
            SETARG_A(fs.f.code[list], level);
        }
    }

    //    void luaK_ret(FuncState *fs, int first, int nret) {
//        luaK_codeABC(fs, OP_RETURN, first, nret + 1, 0);
//    }
    protected static void luaK_ret(FuncState fs, int first, int nret) throws LuaError {
        luaK_codeABC(fs, OP_RETURN, first, nret + 1, 0);
    }

    //    void luaK_exp2anyregup(FuncState *fs, expdesc *e) {
//        if (e->k != VUPVAL || hasjumps(e))
//            luaK_exp2anyreg(fs, e);
//    }
    protected static void luaK_exp2anyregup(FuncState fs, expdesc e) throws LuaError {
        if (e.k != VUPVAL || hasjumps(e))
            luaK_exp2anyreg(fs, e);
    }

    //    void luaK_setlist(FuncState *fs, int base, int nelems, int tostore) {
//        int c = (nelems - 1) / LFIELDS_PER_FLUSH + 1;
//        int b = (tostore == LUA_MULTRET) ? 0 : tostore;
//        lua_assert(tostore != 0 && tostore <= LFIELDS_PER_FLUSH);
//        if (c <= MAXARG_C)
//            luaK_codeABC(fs, OP_SETLIST, base, b, c);
//        else if (c <= MAXARG_Ax) {
//            luaK_codeABC(fs, OP_SETLIST, base, b, 0);
//            codeextraarg(fs, c);
//        } else
//            luaX_syntaxerror(fs->ls, "constructor too long");
//        fs->freereg = base + 1;  /* free registers with list values */
//    }
    protected static void luaK_setlist(FuncState fs, int base, int nelems, int tostore) throws LuaError {
        int c = (nelems - 1) / LFIELDS_PER_FLUSH + 1;
        int b = (tostore == LUA_MULTRET) ? 0 : tostore;
        if (c <= MAXARG_C)
            luaK_codeABC(fs, OP_SETLIST, base, b, c);
        else if (c <= MAXARG_Ax) {
            luaK_codeABC(fs, OP_SETLIST, base, b, 0);
            codeextraarg(fs, c);
        } else
            luaX_syntaxerror(fs.ls, "表的构造函数过长");
        fs.freereg = (base + 1);
    }

    //#define luaK_setmultret(fs, e)    luaK_setreturns(fs, e, LUA_MULTRET)
    protected static void luaK_setmultret(FuncState fs, expdesc e) throws LuaError {
        luaK_setreturns(fs, e, LUA_MULTRET);
    }

    //    void luaK_fixline(FuncState *fs, int line) {
//        fs->f->lineinfo[fs->pc - 1] = line;
//    }
    protected static void luaK_fixline(FuncState fs, int line) {
        fs.f.lineinfo[fs.pc - 1] = line;
    }

    //    void luaK_self(FuncState *fs, expdesc *e, expdesc *key) {
//        int ereg;
//        luaK_exp2anyreg(fs, e);
//        ereg = e->u.info;  /* register where 'e' was placed */
//        freeexp(fs, e);
//        e->u.info = fs->freereg;  /* base register for op_self */
//        e->k = VNONRELOC;  /* self expression has a fixed register */
//        luaK_reserveregs(fs, 2);  /* function and 'self' produced by op_self */
//        luaK_codeABC(fs, OP_SELF, e->u.info, ereg, luaK_exp2RK(fs, key));
//        freeexp(fs, key);
//    }
    protected static void luaK_self(FuncState fs, expdesc e, expdesc key) throws LuaError {
        int ereg;
        luaK_exp2anyreg(fs, e);
        ereg = e.u.info;
        freeexp(fs, e);
        e.u.info = fs.freereg;
        e.k = VNONRELOC;
        luaK_reserveregs(fs, 2);
        luaK_codeABC(fs, OP_SELF, e.u.info, ereg, luaK_exp2RK(fs, key));
        freeexp(fs, key);
    }

    //    typedef enum UnOpr {
//        OPR_MINUS, OPR_BNOT, OPR_NOT, OPR_LEN, OPR_NOUNOPR
//    } UnOpr;
    protected static final int OPR_MINUS = 0;
    protected static final int OPR_BNOT = OPR_MINUS + 1;
    protected static final int OPR_NOT = OPR_BNOT + 1;
    protected static final int OPR_LEN = OPR_NOT + 1;
    protected static final int OPR_NOUNOPR = OPR_LEN + 1;
    //    typedef enum BinOpr {
//        OPR_ADD, OPR_SUB, OPR_MUL, OPR_MOD, OPR_POW,
//        OPR_DIV,
//        OPR_IDIV,
//        OPR_BAND, OPR_BOR, OPR_BXOR,
//        OPR_SHL, OPR_SHR,
//        OPR_CONCAT,
//        OPR_EQ, OPR_LT, OPR_LE,
//        OPR_NE, OPR_GT, OPR_GE,
//        OPR_IS,
//        OPR_AND, OPR_OR,
//        OPR_NOBINOPR
//    } BinOpr;
    protected static final int OPR_ADD = 0;
    protected static final int OPR_SUB = OPR_ADD + 1;
    protected static final int OPR_MUL = OPR_SUB + 1;
    protected static final int OPR_MOD = OPR_MUL + 1;
    protected static final int OPR_POW = OPR_MOD + 1;
    protected static final int OPR_DIV = OPR_POW + 1;
    protected static final int OPR_IDIV = OPR_DIV + 1;
    protected static final int OPR_BAND = OPR_IDIV + 1;
    protected static final int OPR_BOR = OPR_BAND + 1;
    protected static final int OPR_BXOR = OPR_BOR + 1;
    protected static final int OPR_SHL = OPR_BXOR + 1;
    protected static final int OPR_SHR = OPR_SHL + 1;
    protected static final int OPR_CONCAT = OPR_SHR + 1;
    protected static final int OPR_EQ = OPR_CONCAT + 1;
    protected static final int OPR_LT = OPR_EQ + 1;
    protected static final int OPR_LE = OPR_LT + 1;
    protected static final int OPR_NE = OPR_LE + 1;
    protected static final int OPR_GT = OPR_NE + 1;
    protected static final int OPR_GE = OPR_GT + 1;
    protected static final int OPR_IS = OPR_GE + 1;
    protected static final int OPR_AND = OPR_IS + 1;
    protected static final int OPR_OR = OPR_AND + 1;
    protected static final int OPR_NOBINOPR = OPR_OR + 1;

    //    void luaK_prefix(FuncState *fs, UnOpr op, expdesc *e, int line) {
//        static expdesc ef = {VKINT, {0}, NO_JUMP, NO_JUMP};  /* fake 2nd operand */
//        switch (op) {
//            case OPR_MINUS:
//            case OPR_BNOT:
//                if (constfolding(fs, op + LUA_OPUNM, e, &ef))
//                break;
//            /* FALLTHROUGH */
//            case OPR_LEN:
//                codeunexpval(fs, cast(OpCode, op + OP_UNM), e, line);
//                break;
//            case OPR_NOT:
//                codenot(fs, e);
//                break;
//            default:
//                lua_assert(0);
//        }
//    }
    protected static void luaK_prefix(FuncState fs, int op, expdesc e, int line) throws LuaError {
        expdesc ef = new expdesc();
        ef.k = VKINT;
        ef.u.ival = 0;
        ef.t = NO_JUMP;
        ef.f = NO_JUMP;
        switch (op) {
            case OPR_MINUS:
            case OPR_BNOT:
                if (constfolding(fs, op + LUA_OPUNM, e, ef))
                    break;
            case OPR_LEN:
                codeunexpval(fs, op + OP_UNM, e, line);
                break;
            case OPR_NOT:
                codenot(fs, e);
                break;
            default:
                break;
        }
    }

    //    static void codenot(FuncState *fs, expdesc *e) {
//        luaK_dischargevars(fs, e);
//        switch (e->k) {
//            case VNIL:
//            case VFALSE: {
//                e->k = VTRUE;  /* true == not nil == not false */
//                break;
//            }
//            case VK:
//            case VKFLT:
//            case VKINT:
//            case VTRUE: {
//                e->k = VFALSE;  /* false == not "x" == not 0.5 == not 1 == not true */
//                break;
//            }
//            case VJMP: {
//                negatecondition(fs, e);
//                break;
//            }
//            case VRELOCABLE:
//            case VNONRELOC: {
//                discharge2anyreg(fs, e);
//                freeexp(fs, e);
//                e->u.info = luaK_codeABC(fs, OP_NOT, 0, e->u.info, 0);
//                e->k = VRELOCABLE;
//                break;
//            }
//            default:
//                lua_assert(0);  /* cannot happen */
//        }
//        /* interchange true and false lists */
//        {
//            int temp = e->f;
//            e->f = e->t;
//            e->t = temp;
//        }
//        removevalues(fs, e->f);  /* values are useless when negated */
//        removevalues(fs, e->t);
//    }
    protected static void codenot(FuncState fs, expdesc e) throws LuaError {
        luaK_dischargevars(fs, e);
        switch (e.k) {
            case VNIL:
            case VFALSE: {
                e.k = VTRUE;
                break;
            }
            case VK:
            case VKFLT:
            case VKINT:
            case VTRUE: {
                e.k = VFALSE;
                break;
            }
            case VJMP: {
                negatecondition(fs, e);
                break;
            }
            case VRELOCABLE:
            case VNONRELOC: {
                discharge2anyreg(fs, e);
                freeexp(fs, e);
                e.u.info = luaK_codeABC(fs, OP_NOT, 0, e.u.info, 0);
                e.k = VRELOCABLE;
                break;
            }
            default:
                break;
        }
        int temp = e.f;
        e.f = e.t;
        e.t = temp;
        removevalues(fs, e.f);
        removevalues(fs, e.t);
    }

    //    static void removevalues(FuncState *fs, int list) {
//        for (; list != NO_JUMP; list = getjump(fs, list))
//            patchtestreg(fs, list, NO_REG);
//    }
    protected static void removevalues(FuncState fs, int list) throws LuaError {
        for (; list != NO_JUMP; list = getjump(fs, list))
            patchtestreg(fs, list, NO_REG);
    }

    //    static void discharge2anyreg(FuncState *fs, expdesc *e) {
//        if (e->k != VNONRELOC) {  /* no fixed register yet? */
//            luaK_reserveregs(fs, 1);  /* get a register */
//            discharge2reg(fs, e, fs->freereg - 1);  /* put value there */
//        }
//    }
    protected static void discharge2anyreg(FuncState fs, expdesc e) throws LuaError {
        if (e.k != VNONRELOC) {
            luaK_reserveregs(fs, 1);
            discharge2reg(fs, e, fs.freereg - 1);
        }
    }

    //    static void negatecondition(FuncState *fs, expdesc *e) {
//        Instruction *pc = getjumpcontrol(fs, e->u.info);
//        lua_assert(testTMode(GET_OPCODE(*pc)) && GET_OPCODE(*pc) != OP_TESTSET &&
//                GET_OPCODE(*pc) != OP_TEST);
//        SETARG_A(*pc, !(GETARG_A(*pc)));
//    }
    protected static void negatecondition(FuncState fs, expdesc e) throws LuaError {
        Instruction pc = getjumpcontrol(fs, e.u.info);
        if (testTMode(GET_OPCODE(pc)) && GET_OPCODE(pc) != OP_TESTSET &&
                GET_OPCODE(pc) != OP_TEST) {
            int a = GETARG_A(pc);
            SETARG_A(pc, a == 0 ? 1 : 0);
        }
    }

    //    static void codeunexpval(FuncState *fs, OpCode op, expdesc *e, int line) {
//        int r = luaK_exp2anyreg(fs, e);  /* opcodes operate only on registers */
//        freeexp(fs, e);
//        e->u.info = luaK_codeABC(fs, op, 0, r, 0);  /* generate opcode */
//        e->k = VRELOCABLE;  /* all those operations are relocatable */
//        luaK_fixline(fs, line);
//    }
    protected static void codeunexpval(FuncState fs, int op, expdesc e, int line) throws LuaError {
        int r = luaK_exp2anyreg(fs, e);
        freeexp(fs, e);
        e.u.info = luaK_codeABC(fs, op, 0, r, 0);
        e.k = VRELOCABLE;
        luaK_fixline(fs, line);
    }

    //    static int constfolding(FuncState *fs, int op, expdesc *e1, expdesc *e2) {
//        TValue v1, v2, res;
//        if (!tonumeral(e1, &v1) || !tonumeral(e2, &v2) || !validop(op, &v1, &v2))
//        return 0;  /* non-numeric operands or not safe to fold */
//        luaO_arith(fs->ls->L, op, &v1, &v2, &res);  /* does operation */
//        if (ttisinteger(&res)) {
//            e1->k = VKINT;
//            e1->u.ival = ivalue(&res);
//        } else {  /* folds neither NaN nor 0.0 (to avoid problems with -0.0) */
//            lua_Number n = fltvalue(&res);
//            if (luai_numisnan(n) || n == 0)
//                return 0;
//            e1->k = VKFLT;
//            e1->u.nval = n;
//        }
//        return 1;
//    }
    protected static boolean constfolding(FuncState fs, int op, expdesc e1, expdesc e2) throws LuaError {
        TValue v1 = new TValue();
        TValue v2 = new TValue();
        TValue res = new TValue();
        if (!tonumeral(e1, v1) || !tonumeral(e2, v2) || !validop(op, v1, v2))
            return false;
        luaO_arith(fs.ls.L, op, v1, v2, res);
        if (ttisinteger(res)) {
            e1.k = VKINT;
            e1.u.ival = ivalue(res);
        } else {
            double n = fltvalue(res);
            if (luai_numisnan(n) || n == 0)
                return false;
            e1.k = VKFLT;
            e1.u.nval = n;
        }
        return true;
    }

    //    static int validop(int op, TValue *v1, TValue *v2) {
//        switch (op) {
//            case LUA_OPBAND:
//            case LUA_OPBOR:
//            case LUA_OPBXOR:
//            case LUA_OPSHL:
//            case LUA_OPSHR:
//            case LUA_OPBNOT: {  /* conversion errors */
//                lua_Integer i;
//                return (tointeger(v1, &i) && tointeger(v2, &i));
//            }
//            case LUA_OPDIV:
//            case LUA_OPIDIV:
//            case LUA_OPMOD:  /* division by 0 */
//                return (nvalue(v2) != 0);
//            default:
//                return 1;  /* everything else is valid */
//        }
//    }
    protected static boolean validop(int op, TValue v1, TValue v2) {
        switch (op) {
            case LUA_OPBAND:
            case LUA_OPBOR:
            case LUA_OPBXOR:
            case LUA_OPSHL:
            case LUA_OPSHR:
            case LUA_OPBNOT: {
                long[] i = new long[]{0};
                return (tointeger(v1, i) && tointeger(v2, i));
            }
            case LUA_OPDIV:
            case LUA_OPIDIV:
            case LUA_OPMOD:
                return (nvalue(v2) != 0);
            default:
                return true;
        }
    }

    //    static int tonumeral(expdesc *e, TValue *v) {
//        if (hasjumps(e))
//            return 0;  /* not a numeral */
//        switch (e->k) {
//            case VKINT:
//                if (v) setivalue(v, e->u.ival);
//                return 1;
//            case VKFLT:
//                if (v) setfltvalue(v, e->u.nval);
//                return 1;
//            default:
//                return 0;
//        }
//    }
    protected static boolean tonumeral(expdesc e, TValue v) {
        if (hasjumps(e))
            return false;
        switch (e.k) {
            case VKINT:
                if (v != null) setivalue(v, e.u.ival);
                return true;
            case VKFLT:
                if (v != null) setfltvalue(v, e.u.nval);
                return true;
            default:
                return false;
        }
    }

    //    void luaK_infix(FuncState *fs, BinOpr op, expdesc *v) {
//        switch (op) {
//            case OPR_AND: {
//                luaK_goiftrue(fs, v);  /* go ahead only if 'v' is true */
//                break;
//            }
//            case OPR_OR: {
//                luaK_goiffalse(fs, v);  /* go ahead only if 'v' is false */
//                break;
//            }
//            case OPR_CONCAT: {
//                luaK_exp2nextreg(fs, v);  /* operand must be on the 'stack' */
//                break;
//            }
//            case OPR_ADD:
//            case OPR_SUB:
//            case OPR_MUL:
//            case OPR_DIV:
//            case OPR_IDIV:
//            case OPR_MOD:
//            case OPR_POW:
//            case OPR_BAND:
//            case OPR_BOR:
//            case OPR_BXOR:
//            case OPR_SHL:
//            case OPR_SHR: {
//                if (!tonumeral(v, NULL))
//                    luaK_exp2RK(fs, v);
//                /* else keep numeral, which may be folded with 2nd operand */
//                break;
//            }
//            case OPR_IS:
//            default: {
//                luaK_exp2RK(fs, v);
//                break;
//            }
//        }
//    }
    protected static void luaK_infix(FuncState fs, int op, expdesc v) throws LuaError {
        switch (op) {
            case OPR_AND: {
                luaK_goiftrue(fs, v);
                break;
            }
            case OPR_OR: {
                luaK_goiffalse(fs, v);
                break;
            }
            case OPR_CONCAT: {
                luaK_exp2nextreg(fs, v);
                break;
            }
            case OPR_ADD:
            case OPR_SUB:
            case OPR_MUL:
            case OPR_DIV:
            case OPR_IDIV:
            case OPR_MOD:
            case OPR_POW:
            case OPR_BAND:
            case OPR_BOR:
            case OPR_BXOR:
            case OPR_SHL:
            case OPR_SHR: {
                if (!tonumeral(v, null))
                    luaK_exp2RK(fs, v);
                break;
            }
            case OPR_IS:
            default: {
                luaK_exp2RK(fs, v);
                break;
            }
        }
    }

    //    void luaK_goiftrue(FuncState *fs, expdesc *e) {
//        int pc;  /* pc of new jump */
//        luaK_dischargevars(fs, e);
//        switch (e->k) {
//            case VJMP: {  /* condition? */
//                negatecondition(fs, e);  /* jump when it is false */
//                pc = e->u.info;  /* save jump position */
//                break;
//            }
//            case VK:
//            case VKFLT:
//            case VKINT:
//            case VTRUE: {
//                pc = NO_JUMP;  /* always true; do nothing */
//                break;
//            }
//            default: {
//                pc = jumponcond(fs, e, 0);  /* jump when false */
//                break;
//            }
//        }
//        luaK_concat(fs, &e->f, pc);  /* insert new jump in false list */
//        luaK_patchtohere(fs, e->t);  /* true list jumps to here (to go through) */
//        e->t = NO_JUMP;
//    }
    protected static void luaK_goiftrue(FuncState fs, expdesc e) throws LuaError {
        int pc;
        luaK_dischargevars(fs, e);
        switch (e.k) {
            case VJMP: {
                negatecondition(fs, e);
                pc = e.u.info;
                break;
            }
            case VK:
            case VKFLT:
            case VKINT:
            case VTRUE: {
                pc = NO_JUMP;
                break;
            }
            default: {
                pc = jumponcond(fs, e, 0);
                break;
            }
        }
        int[] ef = new int[]{e.f};
        luaK_concat(fs, ef, pc);
        e.f = ef[0];
        luaK_patchtohere(fs, e.t);
        e.t = NO_JUMP;
    }

    //    static int jumponcond(FuncState *fs, expdesc *e, int cond) {
//        if (e->k == VRELOCABLE) {
//            Instruction ie = getinstruction(fs, e);
//            if (GET_OPCODE(ie) == OP_NOT) {
//                fs->pc--;  /* remove previous OP_NOT */
//                return condjump(fs, OP_TEST, GETARG_B(ie), 0, !cond);
//            }
//            /* else go through */
//        }
//        discharge2anyreg(fs, e);
//        freeexp(fs, e);
//        return condjump(fs, OP_TESTSET, NO_REG, e->u.info, cond);
//    }
    protected static int jumponcond(FuncState fs, expdesc e, int cond) throws LuaError {
        if (e.k == VRELOCABLE) {
            Instruction ie = getinstruction(fs, e);
            if (GET_OPCODE(ie) == OP_NOT) {
                fs.pc--;
                return condjump(fs, OP_TEST, GETARG_B(ie), 0, (cond == 0) ? 1 : 0);
            }
        }
        discharge2anyreg(fs, e);
        freeexp(fs, e);
        return condjump(fs, OP_TESTSET, NO_REG, e.u.info, cond);
    }

    //    static int condjump(FuncState *fs, OpCode op, int A, int B, int C) {
//        luaK_codeABC(fs, op, A, B, C);
//        return luaK_jump(fs);
//    }
    protected static int condjump(FuncState fs, int op, int A, int B, int C) throws LuaError {
        luaK_codeABC(fs, op, A, B, C);
        return luaK_jump(fs);
    }

    //    void luaK_goiffalse(FuncState *fs, expdesc *e) {
//        int pc;  /* pc of new jump */
//        luaK_dischargevars(fs, e);
//        switch (e->k) {
//            case VJMP: {
//                pc = e->u.info;  /* already jump if true */
//                break;
//            }
//            case VNIL:
//            case VFALSE: {
//                pc = NO_JUMP;  /* always false; do nothing */
//                break;
//            }
//            default: {
//                pc = jumponcond(fs, e, 1);  /* jump if true */
//                break;
//            }
//        }
//        luaK_concat(fs, &e->t, pc);  /* insert new jump in 't' list */
//        luaK_patchtohere(fs, e->f);  /* false list jumps to here (to go through) */
//        e->f = NO_JUMP;
//    }
    protected static void luaK_goiffalse(FuncState fs, expdesc e) throws LuaError {
        int pc;
        luaK_dischargevars(fs, e);
        switch (e.k) {
            case VJMP: {
                pc = e.u.info;
                break;
            }
            case VNIL:
            case VFALSE: {
                pc = NO_JUMP;
                break;
            }
            default: {
                pc = jumponcond(fs, e, 1);
                break;
            }
        }
        int[] et = new int[]{e.t};
        luaK_concat(fs, et, pc);
        e.t = et[0];
        luaK_patchtohere(fs, e.f);
        e.f = NO_JUMP;
    }

    //    void luaK_posfix(FuncState *fs, BinOpr op,
//                     expdesc *e1, expdesc *e2, int line) {
//        switch (op) {
//            case OPR_IS: {
//                codebinexpval(fs, OP_IS, e1, e2, line);
//                break;
//            }
//            case OPR_AND: {
//                lua_assert(e1->t == NO_JUMP);  /* list closed by 'luK_infix' */
//                luaK_dischargevars(fs, e2);
//                luaK_concat(fs, &e2->f, e1->f);
//            *e1 = *e2;
//                break;
//            }
//            case OPR_OR: {
//                lua_assert(e1->f == NO_JUMP);  /* list closed by 'luK_infix' */
//                luaK_dischargevars(fs, e2);
//                luaK_concat(fs, &e2->t, e1->t);
//            *e1 = *e2;
//                break;
//            }
//            case OPR_CONCAT: {
//                luaK_exp2val(fs, e2);
//                if (e2->k == VRELOCABLE &&
//                        GET_OPCODE(getinstruction(fs, e2)) == OP_CONCAT) {
//                    lua_assert(e1->u.info == GETARG_B(getinstruction(fs, e2)) - 1);
//                    freeexp(fs, e1);
//                    SETARG_B(getinstruction(fs, e2), e1->u.info);
//                    e1->k = VRELOCABLE;
//                    e1->u.info = e2->u.info;
//                } else {
//                    luaK_exp2nextreg(fs, e2);  /* operand must be on the 'stack' */
//                    codebinexpval(fs, OP_CONCAT, e1, e2, line);
//                }
//                break;
//            }
//            case OPR_ADD:
//            case OPR_SUB:
//            case OPR_MUL:
//            case OPR_DIV:
//            case OPR_IDIV:
//            case OPR_MOD:
//            case OPR_POW:
//            case OPR_BAND:
//            case OPR_BOR:
//            case OPR_BXOR:
//            case OPR_SHL:
//            case OPR_SHR: {
//                if (!constfolding(fs, op + LUA_OPADD, e1, e2))
//                    codebinexpval(fs, cast(OpCode, op + OP_ADD), e1, e2, line);
//                break;
//            }
//            case OPR_EQ:
//            case OPR_LT:
//            case OPR_LE:
//            case OPR_NE:
//            case OPR_GT:
//            case OPR_GE: {
//                codecomp(fs, op, e1, e2);
//                break;
//            }
//            default:
//                lua_assert(0);
//        }
//    }
    protected static void luaK_posfix(FuncState fs, int op, expdesc e1, expdesc e2, int line) throws LuaError {
        switch (op) {
            case OPR_IS: {
                codebinexpval(fs, OP_IS, e1, e2, line);
                break;
            }
            case OPR_AND: {
                luaK_dischargevars(fs, e2);
                int[] ef = new int[]{e2.f};
                luaK_concat(fs, ef, e1.f);
                e1.f = ef[0];
                e1 = e2;
                break;
            }
            case OPR_OR: {
                luaK_dischargevars(fs, e2);
                int[] et = new int[]{e2.t};
                luaK_concat(fs, et, e1.t);
                e1.t = et[0];
                e1 = e2;
                break;
            }
            case OPR_CONCAT: {
                luaK_exp2val(fs, e2);
                if (e2.k == VRELOCABLE &&
                        GET_OPCODE(getinstruction(fs, e2)) == OP_CONCAT) {
                    freeexp(fs, e1);
                    SETARG_B(getinstruction(fs, e2), e1.u.info);
                    e1.k = VRELOCABLE;
                    e1.u.info = e2.u.info;
                } else {
                    luaK_exp2nextreg(fs, e2);
                    codebinexpval(fs, OP_CONCAT, e1, e2, line);
                }
                break;
            }
            case OPR_ADD:
            case OPR_SUB:
            case OPR_MUL:
            case OPR_DIV:
            case OPR_IDIV:
            case OPR_MOD:
            case OPR_POW:
            case OPR_BAND:
            case OPR_BOR:
            case OPR_BXOR:
            case OPR_SHL:
            case OPR_SHR: {
                if (!constfolding(fs, op + LUA_OPADD, e1, e2))
                    codebinexpval(fs, op + OP_ADD, e1, e2, line);
                break;
            }
            case OPR_EQ:
            case OPR_LT:
            case OPR_LE:
            case OPR_NE:
            case OPR_GT:
            case OPR_GE: {
                codecomp(fs, op, e1, e2);
                break;
            }
            default:
                break;
        }
    }

    //    static void codecomp(FuncState *fs, BinOpr opr, expdesc *e1, expdesc *e2) {
//        int rk1 = (e1->k == VK) ? RKASK(e1->u.info)
//                : check_exp(e1->k == VNONRELOC, e1->u.info);
//        int rk2 = luaK_exp2RK(fs, e2);
//        freeexps(fs, e1, e2);
//        switch (opr) {
//            case OPR_NE: {  /* '(a ~= b)' ==> 'not (a == b)' */
//                e1->u.info = condjump(fs, OP_EQ, 0, rk1, rk2);
//                break;
//            }
//            case OPR_GT:
//            case OPR_GE: {
//                /* '(a > b)' ==> '(b < a)';  '(a >= b)' ==> '(b <= a)' */
//                OpCode op = cast(OpCode, (opr - OPR_NE) + OP_EQ);
//                e1->u.info = condjump(fs, op, 1, rk2, rk1);  /* invert operands */
//                break;
//            }
//            case OPR_IS: {
//                e1->u.info = condjump(fs, OP_IS, 1, rk1, rk2);
//                break;
//            }
//            default: {  /* '==', '<', '<=' use their own opcodes */
//                OpCode op = cast(OpCode, (opr - OPR_EQ) + OP_EQ);
//                e1->u.info = condjump(fs, op, 1, rk1, rk2);
//                break;
//            }
//        }
//        e1->k = VJMP;
//    }
    protected static void codecomp(FuncState fs, int opr, expdesc e1, expdesc e2) throws LuaError {
        int rk1 = (e1.k == VK) ? RKASK(e1.u.info)
                : e1.u.info;
        int rk2 = luaK_exp2RK(fs, e2);
        freeexps(fs, e1, e2);
        switch (opr) {
            case OPR_NE: {
                e1.u.info = condjump(fs, OP_EQ, 0, rk1, rk2);
                break;
            }
            case OPR_GT:
            case OPR_GE: {
                int op = (opr - OPR_NE) + OP_EQ;
                e1.u.info = condjump(fs, op, 1, rk2, rk1);
                break;
            }
            case OPR_IS: {
                e1.u.info = condjump(fs, OP_IS, 1, rk1, rk2);
                break;
            }
            default: {
                int op = (opr - OPR_EQ) + OP_EQ;
                e1.u.info = condjump(fs, op, 1, rk1, rk2);
                break;
            }
        }
        e1.k = VJMP;
    }

    //    static void codebinexpval(FuncState *fs, OpCode op,
//                              expdesc *e1, expdesc *e2, int line) {
//        int rk2 = luaK_exp2RK(fs, e2);
//        int rk1 = luaK_exp2RK(fs, e1);  /* both operands are "RK" */
//        freeexps(fs, e1, e2);
//        e1->u.info = luaK_codeABC(fs, op, 0, rk1, rk2);  /* generate opcode */
//        e1->k = VRELOCABLE;  /* all those operations are relocatable */
//        luaK_fixline(fs, line);
//    }
    protected static void codebinexpval(FuncState fs, int op, expdesc e1, expdesc e2, int line) throws LuaError {
        int rk2 = luaK_exp2RK(fs, e2);
        int rk1 = luaK_exp2RK(fs, e1);
        freeexps(fs, e1, e2);
        e1.u.info = luaK_codeABC(fs, op, 0, rk1, rk2);
        e1.k = VRELOCABLE;
        luaK_fixline(fs, line);
    }

    //    static void freeexps(FuncState *fs, expdesc *e1, expdesc *e2) {
//        int r1 = (e1->k == VNONRELOC) ? e1->u.info : -1;
//        int r2 = (e2->k == VNONRELOC) ? e2->u.info : -1;
//        if (r1 > r2) {
//            freereg(fs, r1);
//            freereg(fs, r2);
//        } else {
//            freereg(fs, r2);
//            freereg(fs, r1);
//        }
//    }
    protected static void freeexps(FuncState fs, expdesc e1, expdesc e2) throws LuaError {
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

    //    void luaK_storevar(FuncState *fs, expdesc *var, expdesc *ex) {
//        switch (var->k) {
//            case VLOCAL: {
//                freeexp(fs, ex);
//                exp2reg(fs, ex, var->u.info);  /* compute 'ex' into proper place */
//                return;
//            }
//            case VUPVAL: {
//                int e = luaK_exp2anyreg(fs, ex);
//                luaK_codeABC(fs, OP_SETUPVAL, e, var->u.info, 0);
//                break;
//            }
//            case VINDEXED: {
//                OpCode op = (var->u.ind.vt == VLOCAL) ? OP_SETTABLE : OP_SETTABUP;
//                int e = luaK_exp2RK(fs, ex);
//                luaK_codeABC(fs, op, var->u.ind.t, var->u.ind.idx, e);
//                break;
//            }
//            default:
//                lua_assert(0);  /* invalid var kind to store */
//        }
//        freeexp(fs, ex);
//    }
    protected static void luaK_storevar(FuncState fs, expdesc var, expdesc ex) throws LuaError {
        switch (var.k) {
            case VLOCAL: {
                freeexp(fs, ex);
                exp2reg(fs, ex, var.u.info);
                return;
            }
            case VUPVAL: {
                int e = luaK_exp2anyreg(fs, ex);
                luaK_codeABC(fs, OP_SETUPVAL, e, var.u.info, 0);
                break;
            }
            case VINDEXED: {
                int op = (var.u.ind.vt == VLOCAL) ? OP_SETTABLE : OP_SETTABUP;
                int e = luaK_exp2RK(fs, ex);
                luaK_codeABC(fs, op, var.u.ind.t, var.u.ind.idx, e);
                break;
            }
            default:
                break;
        }
        freeexp(fs, ex);
    }

    //    #define luaK_jumpto(fs, t)    luaK_patchlist(fs, luaK_jump(fs), t)
    protected static void luaK_jumpto(FuncState fs, int t) throws LuaError {
        luaK_patchlist(fs, luaK_jump(fs), t);
    }
}
