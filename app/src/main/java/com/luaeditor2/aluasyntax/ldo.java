package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.lzio.*;
import static com.luaeditor2.aluasyntax.lparser.*;
import static com.luaeditor2.aluasyntax.lua.*;
import com.luaeditor2.aluasyntax.structs.*;
public class ldo {
    protected ldo() {
    }

    //    static void f_parser (lua_State *L, void *ud) {
//        LClosure *cl;
//        struct SParser *p = cast(struct SParser *, ud);
//        int c = zgetc(p->z);  /* read first character */
//        if (c == LUA_SIGNATURE[0]) {
//            checkmode(L, p->mode, "binary");
//            cl = luaU_undump(L, p->z, p->name);
//        }
//        else if (c == 0x1c||c==0x1a) {
//            checkmode(L, p->mode, "binary");
//            cl = luaU_undumps(L, p->z, p->name);
//        }
//        else {
//            checkmode(L, p->mode, "text");
//            cl = luaY_parser(L, p->z, &p->buff, &p->dyd, p->name, c);
//        }
//        lua_assert(cl->nupvalues == cl->p->sizeupvalues);
//        luaF_initupvals(L, cl);
//    }
    protected static LClosure f_parser(lua_State L, SParser p) throws LuaError {
        LClosure cl;
        byte c = zgetc(p.z);  /* read first character */
        cl = luaY_parser(L, p.z, p.buff, p.dyd, p.name, c);
//        luaF_initupvals(L, cl);
        return cl;
    }

    protected static LexState D_f_parser(lua_State L, SParser p) {
        LClosure cl;
        byte c = zgetc(p.z);  /* read first character */
        return D_luaY_parser(L, p.z, p.buff, p.dyd, p.name, c);
//        luaF_initupvals(L, cl);
    }


    //    int luaD_protectedparser (lua_State *L, ZIO *z, const char *name,
//                                        const char *mode) {
//        struct SParser p;
//        int status;
//        L->nny++;  /* cannot yield during parsing */
//        p.z = z; p.name = name; p.mode = mode;
//        p.dyd.actvar.arr = NULL; p.dyd.actvar.size = 0;
//        p.dyd.gt.arr = NULL; p.dyd.gt.size = 0;
//        p.dyd.label.arr = NULL; p.dyd.label.size = 0;
//        luaZ_initbuffer(L, &p.buff);
//        status = luaD_pcall(L, f_parser, &p, savestack(L, L->top), L->errfunc);
//        luaZ_freebuffer(L, &p.buff);
//        luaM_freearray(L, p.dyd.actvar.arr, p.dyd.actvar.size);
//        luaM_freearray(L, p.dyd.gt.arr, p.dyd.gt.size);
//        luaM_freearray(L, p.dyd.label.arr, p.dyd.label.size);
//        L->nny--;
//        return status;
//    }
    public static LClosure luaD_protectedparser(lua_State L, ZIO z, String name, String mode) throws Exception {
        SParser p = new SParser();
        int status = LUA_OK;
        p.z = z;
        p.name = name;
        p.mode = mode;
        p.dyd.actvar.arr = null;
        p.dyd.actvar.size = 0;
        p.dyd.gt.arr = null;
        p.dyd.gt.size = 0;
        p.dyd.label.arr = null;
        p.dyd.label.size = 0;
        luaZ_initbuffer(L, p.buff);
        return f_parser(L, p);
    }

    public static LexState D_luaD_protectedparser(lua_State L, ZIO z, String name, String mode) throws Exception {
        try {
            SParser p = new SParser();
            int status = LUA_OK;
            p.z = z;
            p.name = name;
            p.mode = mode;
            p.dyd.actvar.arr = null;
            p.dyd.actvar.size = 0;
            p.dyd.gt.arr = null;
            p.dyd.gt.size = 0;
            p.dyd.label.arr = null;
            p.dyd.label.size = 0;
            luaZ_initbuffer(L, p.buff);
            return D_f_parser(L, p);
//            return status;
        } catch (Throwable e) {
            StringBuilder sb = new StringBuilder();
            sb.append("luaD_protectedparser error: ");
            sb.append(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append("\n");
                sb.append(ste.toString());
            }
            throw new Exception(sb.toString());
        }
    }


}
