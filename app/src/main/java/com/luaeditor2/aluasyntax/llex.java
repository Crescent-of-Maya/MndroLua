package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.clib.*;
import static com.luaeditor2.aluasyntax.lctype.*;
import static com.luaeditor2.aluasyntax.llimits.*;
import static com.luaeditor2.aluasyntax.lobject.*;
import static com.luaeditor2.aluasyntax.lstring.*;
import static com.luaeditor2.aluasyntax.lzio.*;
import static com.luaeditor2.aluasyntax.ldebug.*;

import com.luaeditor2.aluasyntax.structs.*;
import com.luaeditor2.common.AluaParser;

import static com.luaeditor2.aluasyntax.lua.*;

public class llex {
    protected llex() {
    }

    //    #define next(ls) (ls->current = zgetc(ls->z))
    protected static void next(LexState ls) {
        ls.current = zgetc(ls.z);
        ls.lastidx = ls.currentidx;
        ls.currentidx++;
    }

    //    #define currIsNewline(ls)    (ls->current == '\n' || ls->current == '\r')
    protected static boolean currIsNewline(LexState ls) {
        return (ls.current == '\n' || ls.current == '\r');
    }

    public static final String[] luaX_tokens = {
            "and", "break", "case", "continue", "default", "defer", "do", "else", "elseif",
            "end", "false", "for", "function", "goto", "if", "try", "catch", "finally",
//#ifdef LUA_COMPAT_ASM_INLINE
            "__asm__",
//#endif
            "in", "lambda", "local", "nil", "not", "or", "repeat",
            "return", "switch", "then", "true", "until", "when", "is", "while",
            "//", "..", "...", "==", ">=", "<=", "~=",
            "<<", ">>",
            "+=", "-=", "*=", "%=", "^=", "/=", "//=",
            "&=", "|=", "~=", ">>=", "<<=", "..=", "++",
            "->", "=>",
            "::", "<eof>", "数字", "整数", "变量或键名", "字符串",
    };

    //    #define save_and_next(ls) (save(ls, ls->current), next(ls))
    protected static void save_and_next(LexState ls) throws LuaError {
        save(ls, ls.current);
        next(ls);
    }

    //    static void save(LexState *ls, int c) {
//        Mbuffer *b = ls->buff;
//        if (luaZ_bufflen(b) + 1 > luaZ_sizebuffer(b)) {
//            size_t newsize;
//            if (luaZ_sizebuffer(b) >= MAX_SIZE / 2)
//                lexerror(ls, "lexical element too long", 0);
//            newsize = luaZ_sizebuffer(b) * 2;
//            luaZ_resizebuffer(ls->L, b, newsize);
//        }
//        b->buffer[luaZ_bufflen(b)++] = cast(char, c);
//    }
    protected static void save(LexState ls, int c) throws LuaError {
        Mbuffer b = ls.buff;
        if (luaZ_bufflen(b) + 1 > luaZ_sizebuffer(b)) {
            int newsize;
            if (luaZ_sizebuffer(b) >= Integer.MAX_VALUE / 2) {
                lexerror(ls, "词法元素太长", 0);
            }
            newsize = luaZ_sizebuffer(b) * 2;
            luaZ_resizebuffer(ls.L, b, newsize);
        }
        b.buffer[luaZ_bufflen(b, 1)] = (byte) (c & 0xff);
    }

    //    #define FIRST_RESERVED    257
    protected static final int FIRST_RESERVED = 257;

//    enum RESERVED {
//        /* terminal symbols denoted by reserved words */
//        TK_AND = FIRST_RESERVED, TK_BREAK, TK_CASE, TK_CONTINUE, TK_DEFAULT, TK_DEFER,
//        TK_DO, TK_ELSE, TK_ELSEIF, TK_END, TK_FALSE, TK_FOR, TK_FUNCTION,
//        TK_GOTO, TK_IF, TK_TRY, TK_CATCH, TK_FINALLY,
//                #ifdef LUA_COMPAT_ASM_INLINE
//        TK__ASM__,
//                #endif
//                TK_IN, TK_LAMBDA, TK_LOCAL, TK_NIL, TK_NOT, TK_OR, TK_REPEAT,
//                TK_RETURN, TK_SWITCH, TK_THEN, TK_TRUE, TK_UNTIL, TK_WHEN, TK_IS, TK_WHILE,
//        /* other terminal symbols */
//        TK_IDIV, TK_CONCAT, TK_DOTS, TK_EQ, TK_GE, TK_LE, TK_NE,
//                TK_SHL, TK_SHR,
//                TK_ADDA, TK_SUBA, TK_MULA, TK_MODA, TK_POWA, TK_DIVA, TK_IDIVA,
//                TK_BANDA, TK_BORA, TK_BXORA, TK_SHLA, TK_SHRA, TK_CONCATA,
//                TK_SELFADD,
//                TK_LET, TK_MEAN,
//                TK_DBCOLON, TK_EOS,
//                TK_FLT, TK_INT, TK_NAME, TK_STRING
//    };

    public static final int TK_AND = FIRST_RESERVED;
    public static final int TK_BREAK = TK_AND + 1;
    public static final int TK_CASE = TK_BREAK + 1;
    public static final int TK_CONTINUE = TK_CASE + 1;
    public static final int TK_DEFAULT = TK_CONTINUE + 1;
    public static final int TK_DEFER = TK_DEFAULT + 1;
    public static final int TK_DO = TK_DEFER + 1;
    public static final int TK_ELSE = TK_DO + 1;
    public static final int TK_ELSEIF = TK_ELSE + 1;
    public static final int TK_END = TK_ELSEIF + 1;
    public static final int TK_FALSE = TK_END + 1;
    public static final int TK_FOR = TK_FALSE + 1;
    public static final int TK_FUNCTION = TK_FOR + 1;
    public static final int TK_GOTO = TK_FUNCTION + 1;
    public static final int TK_IF = TK_GOTO + 1;
    public static final int TK_TRY = TK_IF + 1;
    public static final int TK_CATCH = TK_TRY + 1;
    public static final int TK_FINALLY = TK_CATCH + 1;
    public static final int TK__ASM__ = TK_FINALLY + 1;
    public static final int TK_IN = TK__ASM__ + 1;
    public static final int TK_LAMBDA = TK_IN + 1;
    public static final int TK_LOCAL = TK_LAMBDA + 1;
    public static final int TK_NIL = TK_LOCAL + 1;
    public static final int TK_NOT = TK_NIL + 1;
    public static final int TK_OR = TK_NOT + 1;
    public static final int TK_REPEAT = TK_OR + 1;
    public static final int TK_RETURN = TK_REPEAT + 1;
    public static final int TK_SWITCH = TK_RETURN + 1;
    public static final int TK_THEN = TK_SWITCH + 1;
    public static final int TK_TRUE = TK_THEN + 1;
    public static final int TK_UNTIL = TK_TRUE + 1;
    public static final int TK_WHEN = TK_UNTIL + 1;
    public static final int TK_IS = TK_WHEN + 1;
    public static final int TK_WHILE = TK_IS + 1;
    public static final int TK_IDIV = TK_WHILE + 1;
    public static final int TK_CONCAT = TK_IDIV + 1;
    public static final int TK_DOTS = TK_CONCAT + 1;
    public static final int TK_EQ = TK_DOTS + 1;
    public static final int TK_GE = TK_EQ + 1;
    public static final int TK_LE = TK_GE + 1;
    public static final int TK_NE = TK_LE + 1;
    public static final int TK_SHL = TK_NE + 1;
    public static final int TK_SHR = TK_SHL + 1;
    public static final int TK_ADDA = TK_SHR + 1;
    public static final int TK_SUBA = TK_ADDA + 1;
    public static final int TK_MULA = TK_SUBA + 1;
    public static final int TK_MODA = TK_MULA + 1;
    public static final int TK_POWA = TK_MODA + 1;
    public static final int TK_DIVA = TK_POWA + 1;
    public static final int TK_IDIVA = TK_DIVA + 1;
    public static final int TK_BANDA = TK_IDIVA + 1;
    public static final int TK_BORA = TK_BANDA + 1;
    public static final int TK_BXORA = TK_BORA + 1;
    public static final int TK_SHLA = TK_BXORA + 1;
    public static final int TK_SHRA = TK_SHLA + 1;
    public static final int TK_CONCATA = TK_SHRA + 1;
    public static final int TK_SELFADD = TK_CONCATA + 1;
    public static final int TK_LET = TK_SELFADD + 1;
    public static final int TK_MEAN = TK_LET + 1;
    public static final int TK_DBCOLON = TK_MEAN + 1;
    public static final int TK_EOS = TK_DBCOLON + 1;//314
    public static final int TK_FLT = TK_EOS + 1;
    public static final int TK_INT = TK_FLT + 1;
    public static final int TK_NAME = TK_INT + 1;
    public static final int TK_STRING = TK_NAME + 1;
    public static final int NUM_RESERVED = (TK_WHILE - FIRST_RESERVED + 1);

    //    const char *luaX_token2str(LexState *ls, int token) {
//        if (token < FIRST_RESERVED) {  /* single-byte symbols? */
//            lua_assert(token == cast_uchar(token));
//            return luaO_pushfstring(ls->L, "'%c'", token);
//        } else {
//        const char *s = luaX_tokens[token - FIRST_RESERVED];
//            if (token < TK_EOS)  /* fixed format (symbols and reserved words)? */
//                return luaO_pushfstring(ls->L, "'%s'", s);
//            else  /* names, strings, and numerals */
//                return s;
//        }
//    }
    protected static String luaX_token2str(LexState ls, int token) {
        if (token < FIRST_RESERVED) {
            return luaO_pushfstring(ls.L, "'%c'", token);
        } else {
            String s = luaX_tokens[token - FIRST_RESERVED];
            if (token < TK_EOS) {
                return luaO_pushfstring(ls.L, "'%s'", s);
            } else {
                return s;
            }
        }
    }

    //    static const char *txtToken(LexState *ls, int token) {
//        switch (token) {
//            case TK_NAME:
//            case TK_STRING:
//            case TK_FLT:
//            case TK_INT:
//                save(ls, '\0');
//                return luaO_pushfstring(ls->L, "'%s'", luaZ_buffer(ls->buff));
//            default:
//                return luaX_token2str(ls, token);
//        }
//    }
    public static String txtToken(LexState ls, int token) throws LuaError {
        switch (token) {
            case TK_NAME:
            case TK_STRING:
            case TK_FLT:
            case TK_INT:
                save(ls, '\0');
                return luaO_pushfstring(ls.L, "'%s'", cstring(luaZ_buffer(ls.buff)));
            case TK_EOS:
                return "当前位置（" + ls.linenumber + "行或更前的某行）";
            default:
                return luaX_token2str(ls, token);
        }
    }

    //    static l_noret lexerror(LexState *ls, const char *msg, int token) {
//        msg = luaG_addinfo(ls->L, msg, ls->source, ls->linenumber);
//        if (token)
//            luaO_pushfstring(ls->L, "%s near %s", msg, txtToken(ls, token));
//        luaD_throw(ls->L, LUA_ERRSYNTAX);
//    }
    protected static void lexerror(LexState ls, String msg, int token) throws LuaError {
        if (ls.L.inLexer) {
            if (AluaParser.LexState_erroridx < 0) {
                if (token == 0) {
                    AluaParser.LexState_errormsg = ls.linenumber + ": " + msg;
                }else{
                    AluaParser.LexState_errormsg = ls.linenumber + ": 在" + txtToken(ls, token) + "附近" + msg;
                }
                AluaParser.LexState_errorline = ls.linenumber;
                AluaParser.LexState_erroridx = ls.lastidx;
            }
        }
        msg = luaG_addinfo(ls.L, msg, ls.source, ls.linenumber);
        if (token != 0) {
            msg = luaO_pushfstring(ls.L, "在%s附近%s", txtToken(ls, token), msg);
        }
        throw new LuaError(msg, LUA_ERRSYNTAX);
    }

    //    l_noret luaX_syntaxerror(LexState *ls, const char *msg) {
//        lexerror(ls, msg, ls->t.token);
//    }
    protected static void luaX_syntaxerror(LexState ls, String msg) throws LuaError {
        lexerror(ls, msg, ls.t.token);
    }

    //    TString *luaX_newstring(LexState *ls, const char *str, size_t l) {
//        lua_State *L = ls->L;
//        TValue *o;  /* entry for 'str' */
//        TString *ts = luaS_newlstr(L, str, l);  /* create new string */
//        setsvalue2s(L, L->top++, ts);  /* temporarily anchor it in stack */
//        o = luaH_set(L, ls->h, L->top - 1);
//        if (ttisnil(o)) {  /* not in use yet? */
//        /* boolean value does not need GC barrier;
//           table has no metatable, so it does not need to invalidate cache */
//            setbvalue(o, 1);  /* t[string] = true */
//            luaC_checkGC(L);
//        } else {  /* string already present */
//            ts = tsvalue(keyfromval(o));  /* re-use value previously stored */
//        }
//        L->top--;  /* remove string from stack */
//        return ts;
//    }
//    protected static String luaX_newstring(LexState ls, String str, int l) {
//        lua_State L = ls.L;
//        TValue o;
//        String ts = str;
//        o = luaH_set(L, ls.h, new TValue(ts));
//        if (ttisnil(o)) {
//            setbvalue(o, 1);
//        } else {
//            ts = tsvalue(o);
//        }
//        return ts;
//    }

    //    static void inclinenumber(LexState *ls) {
//        int old = ls->current;
//        lua_assert(currIsNewline(ls));
//        next(ls);  /* skip '\n' or '\r' */
//        if (currIsNewline(ls) && ls->current != old)
//            next(ls);  /* skip '\n\r' or '\r\n' */
//        if (++ls->linenumber >= MAX_INT)
//            lexerror(ls, "chunk has too many lines", 0);
//    }
    protected static void inclinenumber(LexState ls) throws LuaError {
        int old = ls.current;
        next(ls);
        if (currIsNewline(ls) && ls.current != old) {
            next(ls);
        }
        if ((long) ++ls.linenumber >= Integer.MAX_VALUE) {
            lexerror(ls, "已经没有足够的空间标记行号", 0);
        }
    }
//    void luaX_setinput(lua_State *L, LexState *ls, ZIO *z, TString *source,
//                       int firstchar) {
//        ls->t.token = 0;
//        ls->L = L;
//        ls->current = firstchar;
//        ls->lookahead.token = TK_EOS;  /* no look-ahead token */
//        ls->z = z;
//        ls->fs = NULL;
//        ls->linenumber = 1;
//        ls->lastline = 1;
//        ls->source = source;
//        ls->envn = luaS_newliteral(L, LUA_ENV);  /* get env name */
//        luaZ_resizebuffer(ls->L, ls->buff, LUA_MINBUFFER);  /* initialize buffer */
//    }

    //    #define LUA_ENV        "_ENV"
    protected static final String LUA_ENV = "_ENV";

    protected static void luaX_setinput(lua_State L, LexState ls, ZIO z, String source, int firstchar) {
        ls.t.token = 0;
        ls.L = L;
        ls.current = firstchar;
        ls.lookahead.token = TK_EOS;
        ls.z = z;
        ls.fs = null;
        ls.linenumber = 1;
        ls.lastline = 1;
        ls.source = source;
        ls.envn = LUA_ENV;
        luaZ_resizebuffer(L, ls.buff, LUA_MINBUFFER);
    }

    //    static int check_next1(LexState *ls, int c) {
//        if (ls->current == c) {
//            next(ls);
//            return 1;
//        } else return 0;
//    }
    protected static boolean check_next1(LexState ls, int c) {
        if (ls.current == c) {
            next(ls);
            return true;
        } else {
            return false;
        }
    }

    //    static int check_next2(LexState *ls, const char *set) {
//        lua_assert(set[2] == '\0');
//        if (ls->current == set[0] || ls->current == set[1]) {
//            save_and_next(ls);
//            return 1;
//        } else return 0;
//    }
    protected static boolean check_next2(LexState ls, byte[] set) throws LuaError {
        if (ls.current == set[0] || ls.current == set[1]) {
            save_and_next(ls);
            return true;
        } else {
            return false;
        }
    }

    protected static boolean check_next2(LexState ls, String set) throws LuaError {
        return check_next2(ls, set.getBytes());
    }

    //    static int read_numeral(LexState *ls, SemInfo *seminfo) {
//        TValue obj;
//    const char *expo = "Ee";
//        int first = ls->current;
//        lua_assert(lisdigit(ls->current));
//        save_and_next(ls);
//        if (first == '0' && check_next2(ls, "xX"))  /* hexadecimal? */
//            expo = "Pp";
//        for (;;) {
//            if (check_next2(ls, expo))  /* exponent part? */
//                check_next2(ls, "-+");  /* optional exponent sign */
//            if (lisxdigit(ls->current))
//                save_and_next(ls);
//            else if (ls->current == '.')
//                save_and_next(ls);
//                //mod by nwdxlgzs
//            else if (ls->current == '_')
//                save_and_next(ls);//next(ls);也可以都一样，反正luaO_str2num里跳过_
//            else break;
//        }
//        save(ls, '\0');
//        if (luaO_str2num(luaZ_buffer(ls->buff), &obj) == 0)  /* format error? */
//        lexerror(ls, "malformed number", TK_FLT);
//        if (ttisinteger(&obj)) {
//            seminfo->i = ivalue(&obj);
//            return TK_INT;
//        } else {
//            lua_assert(ttisfloat(&obj));
//            seminfo->r = fltvalue(&obj);
//            return TK_FLT;
//        }
//    }
    protected static int read_numeral(LexState ls, SemInfo seminfo) throws LuaError {
        TValue obj = new TValue();
        String expo = "Ee";
        int first = ls.current;
        save_and_next(ls);
        if (first == '0' && check_next2(ls, "xX")) {
            expo = "Pp";
        }
        for (; ; ) {
            if (check_next2(ls, expo)) {
                check_next2(ls, "-+");
            }
            if (lisxdigit((byte) ls.current)) {
                save_and_next(ls);
            } else if (ls.current == '.') {
                save_and_next(ls);
            } else if (ls.current == '_') {
                save_and_next(ls);
            } else {
                break;
            }
        }
        int numberlen = luaZ_bufflen(ls.buff);
        save(ls, '\0');
        if (luaO_str2num(luaZ_buffer(ls.buff), obj) == 0) {
            lexerror(ls, "转换数字失败，这不是一个正确的数字表达式", TK_FLT);
        }
        if (ttisinteger(obj)) {
            seminfo.i = ivalue(obj);
            seminfo.len = numberlen;
            return TK_INT;
        } else {
            seminfo.r = fltvalue(obj);
            seminfo.len = numberlen;
            return TK_FLT;
        }
    }

    //    static int skip_sep(LexState *ls) {
//        int count = 0;
//        int s = ls->current;
//        lua_assert(s == '[' || s == ']');
//        save_and_next(ls);
//        while (ls->current == '=') {
//            save_and_next(ls);
//            count++;
//        }
//        return (ls->current == s) ? count : (-count) - 1;
//    }
    protected static int skip_sep(LexState ls) throws LuaError {
        int count = 0;
        int s = ls.current;
        save_and_next(ls);
        while (ls.current == '=') {
            save_and_next(ls);
            count++;
        }
        return (ls.current == s) ? count : (-count) - 1;
    }

    //    static void read_long_string(LexState *ls, SemInfo *seminfo, int sep) {
//        int line = ls->linenumber;  /* initial line (for error message) */
//        save_and_next(ls);  /* skip 2nd '[' */
//        if (currIsNewline(ls))  /* string starts with a newline? */
//            inclinenumber(ls);  /* skip it */
//        for (;;) {
//            switch (ls->current) {
//                case EOZ: {  /* error */
//                const char *what = (seminfo ? "string" : "comment");
//                const char *msg = luaO_pushfstring(ls->L,
//                            "unfinished long %s (starting at line %d)", what,
//                            line);
//                    lexerror(ls, msg, TK_EOS);
//                    break;  /* to avoid warnings */
//                }
//                case ']': {
//                    if (skip_sep(ls) == sep) {
//                        save_and_next(ls);  /* skip 2nd ']' */
//                    goto endloop;
//                    }
//                    break;
//                }
//                case '\n':
//                case '\r': {
//                    save(ls, '\n');
//                    inclinenumber(ls);
//                    if (!seminfo) luaZ_resetbuffer(ls->buff);  /* avoid wasting space */
//                    break;
//                }
//                default: {
//                    if (seminfo) save_and_next(ls);
//                    else
//                        next(ls);
//                }
//            }
//        }
//        endloop:
//        if (seminfo)
//            seminfo->ts = luaX_newstring(ls, luaZ_buffer(ls->buff) + (2 + sep),
//                    luaZ_bufflen(ls->buff) - 2 * (2 + sep));
//    }
    protected static void read_long_string(LexState ls, SemInfo seminfo, int sep) throws LuaError {
        int line = ls.linenumber;
        save_and_next(ls);
        if (currIsNewline(ls)) {
            inclinenumber(ls);
        }
        while (true) {
            switch (ls.current) {
                case EOZ: {
                    String what = (seminfo != null ? "字符串" : "注释");
                    String msg = luaO_pushfstring(ls.L, "未完成的长%s(从%d行开始)", what, line);
                    lexerror(ls, msg, TK_EOS);
                    break;
                }
                case ']': {
                    if (skip_sep(ls) == sep) {
                        save_and_next(ls);
                        if (seminfo != null) {
                            byte[] buff = new byte[luaZ_bufflen(ls.buff) - 2 * (2 + sep)];
                            memcpy(buff, 0, luaZ_buffer(ls.buff), 2 + sep, buff.length);
                            String ts = new String(buff);
                            ls.currentidx += ts.length() - buff.length;
                            seminfo.ts = ts;
                            seminfo.len = buff.length;
                        } else if (ls.L.inLexer) {
                            byte[] buff = new byte[luaZ_bufflen(ls.buff)];
                            memcpy(buff, 0, luaZ_buffer(ls.buff), 0, buff.length);
                            String ts = new String(buff);
                            ls.currentidx += ts.length() - buff.length;
                            luaZ_resetbuffer(ls.buff);
                        }

                        return;
                    }
                    break;
                }
                case '\n':
                case '\r': {
                    save(ls, '\n');
                    inclinenumber(ls);
                    if (seminfo == null && !ls.L.inLexer) {
                        luaZ_resetbuffer(ls.buff);
                    }
                    break;
                }
                default: {
                    if (seminfo != null || ls.L.inLexer) {
                        save_and_next(ls);
                    } else {
                        next(ls);
                    }
                }
            }
        }
    }

    //    static void esccheck(LexState *ls, int c, const char *msg) {
//        if (!c) {
//            if (ls->current != EOZ)
//                save_and_next(ls);  /* add current to buffer for error message */
//            lexerror(ls, msg, TK_STRING);
//        }
//    }
    protected static void esccheck(LexState ls, int c, String msg) throws LuaError {
        if (c == 0) {
            if (ls.current != EOZ) {
                save_and_next(ls);
            }
            lexerror(ls, msg, TK_STRING);
        }
    }

    protected static void esccheck(LexState ls, boolean c, String msg) throws LuaError {
        esccheck(ls, c ? 1 : 0, msg);
    }


    //    static int gethexa(LexState *ls) {
//        save_and_next(ls);
//        esccheck(ls, lisxdigit(ls->current), "hexadecimal digit expected");
//        return luaO_hexavalue(ls->current);
//    }
    protected static int gethexa(LexState ls) throws LuaError {
        save_and_next(ls);
        esccheck(ls, lisxdigit((byte) ls.current), "hexadecimal digit expected");
        return luaO_hexavalue((byte) ls.current);
    }

    //    static int gethexa2(LexState *ls) {
//        esccheck(ls, lisxdigit(ls->current), "hexadecimal digit expected");
//        return luaO_hexavalue(ls->current);
//    }
    protected static int gethexa2(LexState ls) throws LuaError {
        esccheck(ls, lisxdigit((byte) ls.current), "hexadecimal digit expected");
        return luaO_hexavalue((byte) ls.current);
    }


    //    static int readhexaesc(LexState *ls) {
//        int r = gethexa(ls);
//        r = (r << 4) + gethexa(ls);
//        luaZ_buffremove(ls->buff, 2);  /* remove saved chars from buffer */
//        return r;
//    }
    protected static int readhexaesc(LexState ls) throws LuaError {
        int r = gethexa(ls);
        r = (r << 4) + gethexa(ls);
        luaZ_buffremove(ls.buff, 2);
        return r;
    }


    //    static unsigned long readutf8esc(LexState *ls) {
//        unsigned long r;
//        int i = 4;  /* chars to be removed: '\', 'u', '{', and first digit */
//        save_and_next(ls);  /* skip 'u' */
//        //esccheck(ls, ls->current == '{', "missing '{'");
//        //r = gethexa(ls);  /* must have at least one digit */
//        int m = ls->current == '{';
//        if (!m)
//            i = 3;
//        if (m)
//            r = gethexa(ls);  /* must have at least one digit */
//        else
//            r = gethexa2(ls);  /* must have at least one digit */
////---
//        while ((save_and_next(ls), lisxdigit(ls->current))) {
//            i++;
//            esccheck(ls, r <= (0x7FFFFFFFu >> 4), "UTF-8 value too large");
//            r = (r << 4) + luaO_hexavalue(ls->current);
////mod by nirenr
//            if (!m && i == 8) {
//                save_and_next(ls);
//                break;
//            }
////---
//        }
//        //esccheck(ls, ls->current == '}', "missing '}'");
//        //next(ls);  /* skip '}' */
//        //mod by nirenr
//        if (m) {
//            esccheck(ls, ls->current == '}', "missing '}'");
//            next(ls);  /* skip '}' */
//        }
//        luaZ_buffremove(ls->buff, i);  /* remove saved chars from buffer */
//        return r;
//    }
    protected static long readutf8esc(LexState ls) throws LuaError {
        long r;
        int i = 4;
        save_and_next(ls);
        boolean m = ls.current == '{';
        if (!m)
            i = 3;
        if (m)
            r = gethexa(ls);
        else
            r = gethexa2(ls);
        while (lisxdigit((byte) ls.current)) {
            i++;
            r = (r << 4) + luaO_hexavalue((byte) ls.current);
            if (!m && i == 8) {
                save_and_next(ls);
                break;
            }
        }
        if (m) {
            esccheck(ls, ls.current == '}', "missing '}'");
            next(ls);
        }
        luaZ_buffremove(ls.buff, i);
        return r;
    }

    //    static void utf8esc(LexState *ls) {
//        char buff[UTF8BUFFSZ];
//        int n = luaO_utf8esc(buff, readutf8esc(ls));
//        for (; n > 0; n--)  /* add 'buff' to string */
//            save(ls, buff[UTF8BUFFSZ - n]);
//    }
    protected static void utf8esc(LexState ls) throws LuaError {
        byte[] buff = new byte[UTF8BUFFSZ];
        int n = luaO_utf8esc(buff, readutf8esc(ls));
        for (; n > 0; n--) {
            save(ls, buff[UTF8BUFFSZ - n]);
        }
    }

    //    static int readdecesc(LexState *ls) {
//        int i;
//        int r = 0;  /* result accumulator */
//        for (i = 0; i < 3 && lisdigit(ls->current); i++) {  /* read up to 3 digits */
//            r = 10 * r + ls->current - '0';
//            save_and_next(ls);
//        }
//        esccheck(ls, r <= UCHAR_MAX, "decimal escape too large");
//        luaZ_buffremove(ls->buff, i);  /* remove read digits from buffer */
//        return r;
//    }
    protected static int readdecesc(LexState ls) throws LuaError {
        int i;
        int r = 0;
        for (i = 0; i < 3 && lisdigit((byte) ls.current); i++) {
            r = 10 * r + ls.current - '0';
            save_and_next(ls);
        }
        esccheck(ls, r <= 255, "decimal escape too large");
        luaZ_buffremove(ls.buff, i);
        return r;
    }

    //    static void read_string(LexState *ls, int del, SemInfo *seminfo) {
//        save_and_next(ls);  /* keep delimiter (for error messages) */
//        while (ls->current != del) {
//            switch (ls->current) {
//                case EOZ:
//                    lexerror(ls, "unfinished string", TK_EOS);
//                    break;  /* to avoid warnings */
//                case '\n':
//                case '\r':
//                    lexerror(ls, "unfinished string", TK_STRING);
//                    break;  /* to avoid warnings */
//                case '\\': {  /* escape sequences */
//                    int c;  /* final character to be saved */
//                    save_and_next(ls);  /* keep '\\' for error messages */
//                    switch (ls->current) {
//                        case 'a':
//                            c = '\a';
//                        goto read_save;
//                        case 'b':
//                            c = '\b';
//                        goto read_save;
//                        case 'f':
//                            c = '\f';
//                        goto read_save;
//                        case 'n':
//                            c = '\n';
//                        goto read_save;
//                        case 'r':
//                            c = '\r';
//                        goto read_save;
//                        case 't':
//                            c = '\t';
//                        goto read_save;
//                        case 'v':
//                            c = '\v';
//                        goto read_save;
//                        case 'x':
//                            c = readhexaesc(ls);
//                        goto read_save;
//                        case 'u':
//                            utf8esc(ls);
//                        goto no_save;
//                        case '\n':
//                        case '\r':
//                            inclinenumber(ls);
//                            c = '\n';
//                        goto only_save;
//                        case '\\':
//                        case '\"':
//                        case '\'':
//                            c = ls->current;
//                        goto read_save;
//                        case EOZ:
//                        goto no_save;  /* will raise an error next loop */
//                        case 'z': {  /* zap following span of spaces */
//                            luaZ_buffremove(ls->buff, 1);  /* remove '\\' */
//                            next(ls);  /* skip the 'z' */
//                            while (lisspace(ls->current)) {
//                                if (currIsNewline(ls)) inclinenumber(ls);
//                                else
//                                    next(ls);
//                            }
//                        goto no_save;
//                        }
//                        default: {
//                            esccheck(ls, lisdigit(ls->current), "invalid escape sequence");
//                            c = readdecesc(ls);  /* digital escape '\ddd' */
//                        goto only_save;
//                        }
//                    }
//                    read_save:
//                    next(ls);
//                    /* go through */
//                    only_save:
//                    luaZ_buffremove(ls->buff, 1);  /* remove '\\' */
//                    save(ls, c);
//                    /* go through */
//                    no_save:
//                    break;
//                }
//                default:
//                    save_and_next(ls);
//            }
//        }
//        save_and_next(ls);  /* skip delimiter */
//        seminfo->ts = luaX_newstring(ls, luaZ_buffer(ls->buff) + 1,
//                luaZ_bufflen(ls->buff) - 2);
//    }
    protected static void read_string_only_save(LexState ls, int c) throws LuaError {
        luaZ_buffremove(ls.buff, 1);  /* remove '\\' */
        save(ls, c);
    }

    protected static void read_string_read_save(LexState ls, int c) throws LuaError {
        next(ls);
        read_string_only_save(ls, c);
    }

    protected static void read_string(LexState ls, int del, SemInfo seminfo) throws LuaError {
        save_and_next(ls);
        while (ls.current != del) {
            switch (ls.current) {
                case EOZ:
                    lexerror(ls, "未完成的字符串", TK_EOS);
                    break;
                case '\n':
                case '\r':
                    lexerror(ls, "未完成的字符串", TK_STRING);
                    break;
                case '\\': {
                    int c;
                    save_and_next(ls);
                    switch (ls.current) {
                        case 'a':
                            c = '\u0007';
                            read_string_read_save(ls, c);
                            break;
                        case 'b':
                            c = '\b';
                            read_string_read_save(ls, c);
                            break;
                        case 'f':
                            c = '\f';
                            read_string_read_save(ls, c);
                            break;
                        case 'n':
                            c = '\n';
                            read_string_read_save(ls, c);
                            break;
                        case 'r':
                            c = '\r';
                            read_string_read_save(ls, c);
                            break;
                        case 't':
                            c = '\t';
                            read_string_read_save(ls, c);
                            break;
                        case 'v':
                            c = '\u000B';
                            read_string_read_save(ls, c);
                            break;
                        case 'x':
                            c = readhexaesc(ls);
                            read_string_read_save(ls, c);
                            break;
                        case 'u':
                            utf8esc(ls);
                            break;
                        case '\n':
                        case '\r':
                            inclinenumber(ls);
                            c = '\n';
                            read_string_only_save(ls, c);
                            break;
                        case '\\':
                        case '\"':
                        case '\'':
                            c = ls.current;
                            read_string_read_save(ls, c);
                            break;
                        case EOZ:
                            break;
                        case 'z': {  /* zap following span of spaces */
                            luaZ_buffremove(ls.buff, 1);  /* remove '\\' */
                            next(ls);  /* skip the 'z' */
                            while (lisspace(ls.current)) {
                                if (currIsNewline(ls)) inclinenumber(ls);
                                else
                                    next(ls);
                            }
                            break;
                        }
                        default: {
                            esccheck(ls, lisdigit(ls.current), "invalid escape sequence");
                            c = readdecesc(ls);  /* digital escape '\ddd' */
                            read_string_only_save(ls, c);
                        }
                    }
                    break;
                }
                default:
                    save_and_next(ls);
            }
        }
        save_and_next(ls);
        byte[] buff = new byte[luaZ_bufflen(ls.buff) - 2];
        memcpy(buff, 0, luaZ_buffer(ls.buff), 1, buff.length);
        String ts = new String(buff);
        ls.currentidx += ts.length() - buff.length;
        seminfo.ts = ts;
        seminfo.len = buff.length;
    }


//    static int llex(LexState *ls, SemInfo *seminfo) {
//        luaZ_resetbuffer(ls->buff);
//        for (;;) {
//            switch (ls->current) {
//                case '\n':
//                case '\r': {  /* line breaks */
//                    inclinenumber(ls);
//                    break;
//                }
//                case ' ':
//                case '\f':
//                case '\t':
//                case '\v': {  /* spaces */
//                    next(ls);
//                    break;
//                }
//                case '+': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_ADDA;
//                    if (check_next1(ls, '+')) return TK_SELFADD;
//                    return '+';
//                    //break;
//                }
//                case '*': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_MULA;
//                    return '*';
//                    //break;
//                }
//                case '%': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_MODA;
//                    return '%';
//                    //break;
//                }
//                case '^': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_POWA;
//                    return '^';
//                    //break;
//                }
//                case '-': {  /* '-' or '--' (comment) */
//                    next(ls);
//                    if (check_next1(ls, '>')) return TK_LET;//mod by nirenr
//                    if (check_next1(ls, '=')) return TK_SUBA;
//                    if (ls->current != '-') return '-';
//                    /* else is a comment */
//                    next(ls);
//                    if (ls->current == '[') {  /* long comment? */
//                        int sep = skip_sep(ls);
//                        luaZ_resetbuffer(ls->buff);  /* 'skip_sep' may dirty the buffer */
//                        if (sep >= 0) {
//                            read_long_string(ls, NULL, sep);  /* skip long comment */
//                            luaZ_resetbuffer(ls->buff);  /* previous call may dirty the buff. */
//                            break;
//                        }
//                    }
//                    /* else short comment */
//                    while (!currIsNewline(ls) && ls->current != EOZ)
//                        next(ls);  /* skip until end of line (or end of file) */
//                    break;
//                }
//                case '[': {  /* long string or simply '[' */
//                    int sep = skip_sep(ls);
//                    if (sep >= 0) {
//                        read_long_string(ls, seminfo, sep);
//                        return TK_STRING;
//                    } else if (sep != -1)  /* '[=...' missing second bracket */
//                        lexerror(ls, "invalid long string delimiter", TK_STRING);
//                    return '[';
//                }
//                case '=': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_EQ;
//                    if (check_next1(ls, '>')) return TK_MEAN;//mod by nirenr
//                    else return '=';
//                }
//                case '<': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_LE;
//                    else if (check_next1(ls, '<')) {
//                        if (check_next1(ls, '=')) return TK_SHLA;
//                        return TK_SHL;
//                    } else return '<';
//                }
//                case '>': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_GE;
//                    else if (check_next1(ls, '>')) {
//                        if (check_next1(ls, '=')) return TK_SHRA;
//                        return TK_SHR;
//                    } else return '>';
//                }
//                case '/': {
//                    next(ls);
//                    if (check_next1(ls, '/')) {
//                        if (check_next1(ls, '=')) return TK_IDIVA;
//                        return TK_IDIV;
//                    }
//                    if (check_next1(ls, '=')) return TK_DIVA;
//                    else return '/';
//                }
////mod by nirenr
//                case '!': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_NE;
//                    else return TK_NOT;
//                }
//                case '&': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_BANDA;
//                    if (check_next1(ls, '&')) return TK_AND;
//                    return '&';
//                }
//                case '|': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_BORA;
//                    if (check_next1(ls, '|')) return TK_OR;
//                    return '|';
//                }
////---
//                case '~': {
//                    next(ls);
//                    if (check_next1(ls, '=')) return TK_BXORA;//TK_NE
//                    else return '~';
//                }
//                case ':': {
//                    next(ls);
//                    if (check_next1(ls, ':')) return TK_DBCOLON;
//                    else return ':';
//                }
//                case '"':
//                case '\'': {  /* short literal strings */
//                    read_string(ls, ls->current, seminfo);
//                    return TK_STRING;
//                }
//                case '.': {  /* '.', '..', '...', or number */
//                    save_and_next(ls);
//                    if (check_next1(ls, '.')) {
//                        if (check_next1(ls, '.'))
//                            return TK_DOTS;   /* '...' */
//                        else if (check_next1(ls, '='))
//                            return TK_CONCATA;  /* '..=' */
//                        else
//                            return TK_CONCAT;   /* '..' */
//                    } else if (!lisdigit(ls->current)) return '.';
//                    else return read_numeral(ls, seminfo);
//                }
//                case '0':
//                case '1':
//                case '2':
//                case '3':
//                case '4':
//                case '5':
//                case '6':
//                case '7':
//                case '8':
//                case '9': {
//                    return read_numeral(ls, seminfo);
//                }
//                case EOZ: {
//                    return TK_EOS;
//                }
//                default: {
//                    handle_default:
//                    if (lislalpha(ls->current)) {  /* identifier or reserved word? */
//                        TString *ts;
//                        do {
//                            save_and_next(ls);
//                        } while (lislalnum(ls->current));
//                        ts = luaX_newstring(ls, luaZ_buffer(ls->buff),
//                                luaZ_bufflen(ls->buff));
//                        seminfo->ts = ts;
//                        if (isreserved(ts))  /* reserved word? */
//                            return ts->extra - 1 + FIRST_RESERVED;
//                        else {
//                            return TK_NAME;
//                        }
//                    } else {  /* single-char tokens (+ - / ...) */
//                        int c = ls->current;
//                        next(ls);
//                        return c;
//                    }
//                }
//            }
//        }
//    }


    protected static int method_llex(LexState ls, SemInfo seminfo) throws LuaError {
        luaZ_resetbuffer(ls.buff);
        for (; ; ) {
            switch (ls.current) {
                case '\n':
                case '\r': {
                    inclinenumber(ls);
                    break;
                }
                case ' ':
                case '\f':
                case '\t':
                case '\u000B': {
                    next(ls);
                    break;
                }
                case '+': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_ADDA;
                    if (check_next1(ls, '+')) return TK_SELFADD;
                    return '+';
                }
                case '*': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_MULA;
                    return '*';
                }
                case '%': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_MODA;
                    return '%';
                }
                case '^': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_POWA;
                    return '^';
                }
                case '-': {
                    next(ls);
                    if (check_next1(ls, '>')) return TK_LET;
                    if (check_next1(ls, '=')) return TK_SUBA;
                    if (ls.current != '-') return '-';
                    next(ls);
                    if (ls.current == '[') {
                        int sep = skip_sep(ls);
                        luaZ_resetbuffer(ls.buff);
                        if (sep >= 0) {
                            read_long_string(ls, null, sep);
                            luaZ_resetbuffer(ls.buff);
                            break;
                        }
                    }
                    if (ls.L.inLexer) {
                        luaZ_resetbuffer(ls.buff);
                        while (!currIsNewline(ls) && ls.current != EOZ)
                            save_and_next(ls);
                        byte[] buff = new byte[luaZ_bufflen(ls.buff)];
                        memcpy(buff, 0, luaZ_buffer(ls.buff), 0, buff.length);
                        String ts = new String(buff);
                        ls.currentidx += ts.length() - buff.length;
                        luaZ_resetbuffer(ls.buff);
                    } else {
                        while (!currIsNewline(ls) && ls.current != EOZ)
                            next(ls);
                    }
                    break;
                }
                case '[': {
                    int sep = skip_sep(ls);
                    if (sep >= 0) {
                        read_long_string(ls, seminfo, sep);
                        return TK_STRING;
                    } else if (sep != -1)
                        lexerror(ls, "无效的长字符串分隔符", TK_STRING);
                    return '[';
                }
                case '=': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_EQ;
                    if (check_next1(ls, '>')) return TK_MEAN;
                    else return '=';
                }
                case '<': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_LE;
                    else if (check_next1(ls, '<')) {
                        if (check_next1(ls, '=')) return TK_SHLA;
                        return TK_SHL;
                    } else return '<';
                }
                case '>': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_GE;
                    else if (check_next1(ls, '>')) {
                        if (check_next1(ls, '=')) return TK_SHRA;
                        return TK_SHR;
                    } else return '>';
                }
                case '/': {
                    next(ls);
                    if (check_next1(ls, '/')) {
                        if (check_next1(ls, '=')) return TK_IDIVA;
                        return TK_IDIV;
                    }
                    if (check_next1(ls, '=')) return TK_DIVA;
                    else return '/';
                }
                case '!': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_NE;
                    else return TK_NOT;
                }
                case '&': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_BANDA;
                    if (check_next1(ls, '&')) return TK_AND;
                    return '&';
                }
                case '|': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_BORA;
                    if (check_next1(ls, '|')) return TK_OR;
                    return '|';
                }
                case '~': {
                    next(ls);
                    if (check_next1(ls, '=')) return TK_BXORA;
                    else return '~';
                }
                case ':': {
                    next(ls);
                    if (check_next1(ls, ':')) return TK_DBCOLON;
                    else return ':';
                }
                case '"':
                case '\'': {
                    read_string(ls, ls.current, seminfo);
                    return TK_STRING;
                }
                case '.': {
                    save_and_next(ls);
                    if (check_next1(ls, '.')) {
                        if (check_next1(ls, '.'))
                            return TK_DOTS;
                        else if (check_next1(ls, '='))
                            return TK_CONCATA;
                        else
                            return TK_CONCAT;
                    } else if (!lisdigit(ls.current)) return '.';
                    else return read_numeral(ls, seminfo);
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    return read_numeral(ls, seminfo);
                }
                case EOZ: {
                    return TK_EOS;
                }
                default: {
                    if (lislalpha(ls.current)) {
                        String ts;
                        do {
                            save_and_next(ls);
                        } while (lislalnum(ls.current));
                        byte[] buff = new byte[luaZ_bufflen(ls.buff)];
                        memcpy(buff, luaZ_buffer(ls.buff), buff.length);
                        ts = new String(buff);
                        ls.currentidx += ts.length() - buff.length;
                        ls.lastnameidx = ls.currentidx;
                        seminfo.ts = ts;
                        seminfo.len = buff.length;
                        int extra = isreserved(ts);
                        if (extra >= 0)
                            return extra - 1 + FIRST_RESERVED;
                        else {
                            return TK_NAME;
                        }
                    } else {
                        int c = ls.current;
                        if (c == '}' || c == ';' || c == ',')
                            ls.lastSymEndidx = ls.currentidx - 1;
                        next(ls);
                        return c;
                    }
                }
            }
        }
    }

    //    void luaX_next(LexState *ls) {
//        ls->lastline = ls->linenumber;
//        if (ls->lookahead.token != TK_EOS) {  /* is there a look-ahead token? */
//            ls->t = ls->lookahead;  /* use this one */
//            ls->lookahead.token = TK_EOS;  /* and discharge it */
//        } else
//            ls->t.token = llex(ls, &ls->t.seminfo);  /* read next token */
//    }
    private static void tokencopy(Token t, Token lookahead) {
        t.token = lookahead.token;
        t.seminfo.ts = lookahead.seminfo.ts;
        t.seminfo.r = lookahead.seminfo.r;
        t.seminfo.i = lookahead.seminfo.i;
        t.seminfo.len = lookahead.seminfo.len;
    }

    protected static void luaX_next(LexState ls) throws LuaError {
        ls.lastline = ls.linenumber;
        if (ls.lookahead.token != TK_EOS) {
//            ls.t = ls.lookahead;
            tokencopy(ls.t, ls.lookahead);
            ls.lookahead.token = TK_EOS;
        } else {
            ls.t.token = method_llex(ls, ls.t.seminfo);
        }
    }


    //    int luaX_lookahead(LexState *ls) {
//        lua_assert(ls->lookahead.token == TK_EOS);
//        ls->lookahead.token = llex(ls, &ls->lookahead.seminfo);
//        return ls->lookahead.token;
//    }
    protected static int luaX_lookahead(LexState ls) throws LuaError {
        ls.lookahead.token = method_llex(ls, ls.lookahead.seminfo);
        return ls.lookahead.token;
    }


}
