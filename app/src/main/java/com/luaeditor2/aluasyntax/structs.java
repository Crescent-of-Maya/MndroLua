package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.llimits.*;
import static com.luaeditor2.aluasyntax.lobject.*;
import static com.luaeditor2.aluasyntax.lua.*;

import com.luaeditor2.common.Flag;

import java.util.ArrayList;
import java.util.HashMap;

public class structs {
    protected static class CClosure {
    }

    protected static class BlockCnt {
        protected BlockCnt() {
        }

        //    typedef struct BlockCnt {
//        struct BlockCnt *previous;  /* chain */
//        int firstlabel;  /* index of first label in this block */
//        int firstgoto;  /* index of first pending goto in this block */
//        lu_byte nactvar;  /* # active locals outside the block */
//        lu_byte upval;  /* true if some variable in the block is an upvalue */
//        lu_byte isloop;  /* true if 'block' is a loop */
//    } BlockCnt;
        protected BlockCnt previous = null;
        protected int firstlabel = 0;
        protected int firstgoto = 0;
        //        protected byte nactvar = 0;
        protected int nactvar = 0;
        protected byte upval = 0;
        protected byte isloop = 0;

    }

    protected static class FuncState {
        protected FuncState() {
        }

        // typedef struct FuncState {
//    Proto *f;  /* current function header */
//    struct FuncState *prev;  /* enclosing function */
//    struct LexState *ls;  /* lexical state */
//    struct BlockCnt *bl;  /* chain of current blocks */
//    int pc;  /* next position to code (equivalent to 'ncode') */
//    int lasttarget;   /* 'label' of last 'jump label' */
//    int jpc;  /* list of pending jumps to 'pc' */
//    int nk;  /* number of elements in 'k' */
//    int np;  /* number of elements in 'p' */
//    int firstlocal;  /* index of first local var (in Dyndata array) */
//    short nlocvars;  /* number of elements in 'f->locvars' */
//    lu_byte nactvar;  /* number of active local variables */
//    lu_byte nups;  /* number of upvalues */
//    lu_byte freereg;  /* first free register */
//    int needclose;
//} FuncState;
        protected Proto f = null;
        protected FuncState prev = null;
        protected LexState ls = null;
        protected BlockCnt bl = null;
        protected int pc = 0;
        protected int lasttarget = 0;
        protected int jpc = 0;
        protected int nk = 0;
        protected int np = 0;
        protected int firstlocal = 0;
        //        protected short nlocvars = 0;
//        protected byte nactvar = 0;
//        protected byte nups = 0;
//        protected byte freereg = 0;
        protected int nlocvars = 0;
        protected int nactvar = 0;
        protected int nups = 0;
        protected int freereg = 0;
        protected int needclose = 0;
        protected int trymode = 0;
    }

    protected static class Dyndata {
        protected Dyndata() {
        }

        //    typedef struct Dyndata {
//        struct {  /* list of active local variables */
//            Vardesc *arr;
//            int n;
//            int size;
//        } actvar;
//        Labellist gt;  /* list of pending gotos */
//        Labellist label;   /* list of active labels */
//    } Dyndata;
        protected Actvar actvar = new Actvar();
        protected Labellist gt = new Labellist();
        protected Labellist label = new Labellist();

        protected static class Actvar {
            protected Actvar() {
            }

            protected Vardesc[] arr = null;
            protected int n = 0;
            protected int size = 0;
        }
    }

    protected static class Instruction {
        protected Instruction() {
        }

        protected int i = 0;
    }

    protected static class Labeldesc {
        protected Labeldesc() {
        }

        //    typedef struct Labeldesc {
//        TString *name;  /* label identifier */
//        int pc;  /* position in code */
//        int line;  /* line where it appeared */
//        lu_byte nactvar;  /* local level where it appears in current block */
//    } Labeldesc;
        protected String name = null;
        protected int pc = 0;
        protected int line = 0;
        //        protected byte nactvar = 0;
        protected int nactvar = 0;
    }

    protected static class expdesc {
        protected expdesc() {
        }

        //    typedef struct expdesc {
//        expkind k;
//        union {
//            lua_Integer ival;    /* for VKINT */
//            lua_Number nval;  /* for VKFLT */
//            int info;  /* for generic use */
//            struct {  /* for indexed variables (VINDEXED) */
//                short idx;  /* index (R/K) */
//                lu_byte t;  /* table (register or upvalue) */
//                lu_byte vt;  /* whether 't' is register (VLOCAL) or upvalue (VUPVAL) */
//            } ind;
//        } u;
//        int t;  /* patch list of 'exit when true' */
//        int f;  /* patch list of 'exit when false' */
//    } expdesc;
        protected VarType type = null;
        protected int k = 0;
        protected u u = new u();
        protected int t = 0;
        protected int f = 0;

        protected static class u {
            protected u() {
            }

            protected long ival = 0;
            protected double nval = 0;
            protected int info = 0;
            protected ind ind = new ind();

            protected static class ind {
                protected ind() {
                }

                protected short idx = 0;
                protected byte t = 0;
                protected byte vt = 0;
            }
        }
    }

    protected static class Labellist {
        protected Labellist() {
        }

        //    typedef struct Labellist {
//        Labeldesc *arr;  /* array */
//        int n;  /* number of entries in use */
//        int size;  /* array size */
//    } Labellist;
        protected Labeldesc[] arr = null;
        protected int n = 0;
        protected int size = 0;
    }

    public static class LClosure {
        protected LClosure() {
        }

        public Proto p = null;
    }

    protected static class LexState {

        protected LexState() {
        }

        //    typedef struct LexState {
//        int current;  /* current character (charint) */
//        int linenumber;  /* input line counter */
//        int lastline;  /* line of last token 'consumed' */
//        Token t;  /* current token */
//        Token lookahead;  /* look ahead token */
//        struct FuncState *fs;  /* current function (parser) */
//        struct lua_State *L;
//        ZIO *z;  /* input stream */
//        Mbuffer *buff;  /* buffer for tokens */
//        Table *h;  /* to avoid collection/reuse strings */
//        struct Dyndata *dyd;  /* dynamic structures used by the parser */
//        TString *source;  /* current source name */
//        TString *envn;  /* environment variable name */
//    } LexState;
        protected int current = 0;
        protected int linenumber = 0;
        protected int lastline = 0;
        protected Token t = new Token();
        protected Token lookahead = new Token();
        protected FuncState fs = null;
        protected lua_State L = null;
        protected ZIO z = null;
        protected Mbuffer buff = null;
        protected Table h = null;
        protected Dyndata dyd = null;
        protected String source = null;
        protected String envn = null;
        public int lastidx = 0;
        public int lastnameidx = 0;
        public int lastSymEndidx = 0;
        public int checknameidx = 0;
        public int currentidx = 0;
    }

    public static class LoadS {
        protected LoadS() {
        }

        protected LoadS(String s) {
            this.s = s.getBytes();
            this.size = this.s.length;
        }

        public LoadS(CharSequence s) {
            this(s.toString());
        }

        //        typedef struct LoadS {
        //            const char *s;
        //            size_t size;
        //        } LoadS;
        protected byte[] s;
        protected int size;
    }

    public static class LocVar {
        protected LocVar() {
        }

        //    typedef struct LocVar {
//        TString *varname;
//        int startpc;  /* first point where variable is active */
//        int endpc;    /* first point where variable is dead */
//    } LocVar;
        public String varname = null;
        protected int startpc = 0;
        protected int endpc = 0;
        public int startidx = 0;
        public int endidx = 0;
        public VarType type = null;
    }

    protected static class lua_CFunction {
    }

    public static class lua_Reader {
        public lua_Reader() {
        }

        //    typedef const char * (*lua_Reader) (lua_State *L, void *ud, size_t *sz);
        protected static byte[] getS(lua_State L, Object ud, int[] sz) {
            LoadS ls = (LoadS) ud;
            if (ls.size == 0) return null;
            sz[0] = ls.size;
            ls.size = 0;
            return ls.s;
        }
    }

    public static class lua_State {

        public lua_State() {
        }

        protected int nCcalls = 0;
        public boolean inLexer = false;
        public Flag abort = new Flag();
    }

    protected static class Mbuffer {
        protected Mbuffer() {
        }

        //    typedef struct Mbuffer {
//        char *buffer;
//        size_t n;
//        size_t buffsize;
//    } Mbuffer;
        protected byte[] buffer = null;
        protected int n = 0;
        protected int buffsize = 0;
    }

    public static class Proto {
        protected Proto() {
        }

        //    typedef struct Proto {
//        CommonHeader;
//        lu_byte numparams;  /* number of fixed parameters */
//        lu_byte is_vararg;  /* 2: declared vararg; 1: uses vararg */
//        lu_byte maxstacksize;  /* number of registers needed by this function */
//        int sizeupvalues;  /* size of 'upvalues' */
//        int sizek;  /* size of 'k' */
//        int sizecode;
//        int sizelineinfo;
//        int sizep;  /* size of 'p' */
//        int sizelocvars;
//        int linedefined;  /* debug information  */
//        int lastlinedefined;  /* debug information  */
//        TValue *k;  /* constants used by the function */
//        Instruction *code;  /* opcodes */
//        struct Proto **p;  /* functions defined inside the function */
//        int *lineinfo;  /* map from opcodes to source lines (debug information) */
//        LocVar *locvars;  /* information about local variables (debug information) */
//        Upvaldesc *upvalues;  /* upvalue information */
//        struct LClosure *cache;  /* last-created closure with this prototype */
//        TString  *source;  /* used for debug information */
//        GCObject *gclist;
//    } Proto;
        public byte numparams = 0;
        protected byte is_vararg = 0;
        protected byte maxstacksize = 0;
        protected int sizeupvalues = 0;
        protected int sizek = 0;
        protected int sizecode = 0;
        protected int sizelineinfo = 0;
        protected int sizep = 0;
        protected int sizelocvars = 0;
        protected int linedefined = 0;
        protected int lastlinedefined = 0;
        protected TValue[] k = null;
        protected Instruction[] code = null;
        public Proto[] p = null;
        protected int[] lineinfo = null;
        public LocVar[] locvars = null;
        public Upvaldesc[] upvalues = null;
        protected String source = null;
        public int startidx = 0;
        public int endidx = 0;
    }
        /*public ArrayList<ASMBlock> asm_blocks = new ArrayList<>();
    }

    public static class ASMBlock {
        protected ASMBlock() {
        }

        protected ASMBlock(String n) {
            this.n = n;
        }

        public String n;
        public int startidx = 0;
        public int endidx = 0;

        @Override
        public String toString() {
            return "n=" + n + " startidx=" + startidx + " endidx=" + endidx;
        }
    }*/

    protected static class SemInfo {
        protected int len = 0;//方便知道长度

        protected SemInfo() {
        }

        //    typedef union {
//        lua_Number r;
//        lua_Integer i;
//        TString *ts;
//    } SemInfo;  /* semantics information */
        protected double r = 0;
        protected long i = 0;
        protected String ts = null;
    }

    protected static class SParser {
        protected SParser() {
        }

        //    struct SParser {  /* data to 'f_parser' */
//        ZIO *z;
//        Mbuffer buff;  /* dynamic structure used by the scanner */
//        Dyndata dyd;  /* dynamic structures used by the parser */
//  const char *mode;
//  const char *name;
//    };
        protected ZIO z;
        protected Mbuffer buff = new Mbuffer();
        protected Dyndata dyd = new Dyndata();
        protected String mode;
        protected String name;
    }

    protected static class Table {
        protected Table() {
        }

        protected HashMap<Object, Object> map = new HashMap<>();
    }

    protected static class Token {
        protected Token() {
        }

        //    typedef struct Token {
//        int token;
//        SemInfo seminfo;
//    } Token;
        protected int token = 0;
        protected SemInfo seminfo = new SemInfo();
    }

    public static class TValue {
        protected static final TValue luaO_nilobject = new TValue(LUA_TNIL);


        protected TValue() {
        }

        protected TValue(int tt_) {
            this.tt_ = tt_;
        }

        protected TValue(Value value, int tt_) {
            this.value_ = value;
            this.tt_ = tt_;
        }

        protected TValue(String s) {
            this.value_ = new Value();
            if (s.getBytes().length > LUAI_MAXSHORTLEN)
                this.tt_ = LUA_TLNGSTR;
            else
                this.tt_ = LUA_TSHRSTR;
            this.value_.gc = s;
        }

        protected TValue(long i) {
            this.value_ = new Value();
            this.value_.i = i;
            this.tt_ = LUA_TNUMINT;
        }

        protected TValue(double n) {
            this.value_ = new Value();
            this.value_.n = n;
            this.tt_ = LUA_TNUMFLT;
        }

        protected TValue(boolean b) {
            this.value_ = new Value();
            this.value_.b = b ? 1 : 0;
            this.tt_ = LUA_TBOOLEAN;
        }

        //#define TValuefields	Value value_; int tt_
//    typedef struct lua_TValue {
//        TValuefields;
//    } TValue;
        protected Value value_ = new Value();
        public int tt_ = 0;

        protected void set(TValue o) {
            this.value_ = o.value_;
            this.tt_ = o.tt_;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TValue) {
                TValue o = (TValue) obj;
                if (this.tt_ == o.tt_) {
                    int t = ttype(this);
                    if (t == LUA_TNIL) {
                        return true;
                    }
                    if (t == LUA_TNUMFLT) {
                        return this.value_.n_equals(o.value_.n);
                    }
                    if (t == LUA_TNUMINT) {
                        return this.value_.i_equals(o.value_.i);
                    }
                    if (t == LUA_TSHRSTR || t == LUA_TLNGSTR) {
                        return this.value_.gc_equals(o.value_.gc);
                    }
                    if (t == LUA_TBOOLEAN) {
                        return this.value_.b_equals(o.value_.b);
                    }
                    if (t == LUA_TLIGHTUSERDATA) {
                        return this.value_.p_equals(o.value_.p);
                    }
                    if (t == LUA_TLCF) {
                        return this.value_.f_equals(o.value_.f);
                    }
                    return this.value_.equals(o.value_);
                }
                return false;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int t = ttype(this);
            if (t == LUA_TNIL) {
                return 0;
            }
            if (t == LUA_TNUMFLT) {
                return (int) (Double.doubleToLongBits(value_.n));
            }
            if (t == LUA_TNUMINT) {
                return (int) value_.i;
            }
            if (t == LUA_TSHRSTR || t == LUA_TLNGSTR) {
                return value_.gc.hashCode();
            }
            if (t == LUA_TBOOLEAN) {
                return value_.b == 0 ? 0 : 1;
            }
            if (t == LUA_TLIGHTUSERDATA) {
                return value_.p.hashCode();
            }
            if (t == LUA_TLCF) {
                return value_.f.hashCode();
            }
            return tt_;
        }

        public String getTValueTypeName() {
            int t = ttnov(this);
            if (t < VarType.TYPE_NAMES.length && t > 0) {
                return VarType.TYPE_NAMES[t];
            }
            return null;
        }

    }

    public static class Upvaldesc {
        protected Upvaldesc() {
        }

        //    typedef struct Upvaldesc {
//        TString *name;  /* upvalue name (for debug information) */
//        lu_byte instack;  /* whether it is in stack (register) */
//        lu_byte idx;  /* index of upvalue (in stack or in outer function's list) */
//    } Upvaldesc;
        public String name = null;
        protected boolean instack = false;
        protected byte idx = 0;
        public VarType type = null;
    }

    protected static class Value {
        protected Value() {
        }

        //    typedef union Value {
//        GCObject *gc;    /* collectable objects */
//        void *p;         /* light userdata */
//        int b;           /* booleans */
//        lua_CFunction f; /* light C functions */
//        lua_Integer i;   /* integer numbers */
//        lua_Number n;    /* float numbers */
//    } Value;
        protected Object gc = null;
        protected Object p = null;
        protected int b = 0;
        protected lua_CFunction f = null;
        protected long i = 0;
        protected double n = 0;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                Value o = (Value) obj;
                return this.gc.equals(o.gc) && this.p.equals(o.p) && this.b == o.b && this.f.equals(o.f) && this.i == o.i && this.n == o.n;
            }
            return false;
        }

        protected boolean gc_equals(Object gc) {
            return this.gc.equals(gc);
        }

        protected boolean p_equals(Object p) {
            return this.p.equals(p);
        }

        protected boolean b_equals(int b) {
            boolean b1 = this.b == 1;
            boolean b2 = b == 1;
            return b1 == b2;
        }

        protected boolean f_equals(lua_CFunction f) {
            return this.f.equals(f);
        }

        protected boolean i_equals(long i) {
            return this.i == i;
        }

        protected boolean n_equals(double n) {
            return this.n == n;
        }
    }

    protected static class Vardesc {
        protected Vardesc() {
        }

        //    typedef struct Vardesc {
//        short idx;  /* variable index in stack */
//    } Vardesc;
        protected short idx = 0;
    }

    public static class ZIO {
        public ZIO() {

        }

        //    struct Zio {
//        size_t n;			/* bytes still unread */
//        const char *p;		/* current position in buffer */
//        lua_Reader reader;		/* reader function */
//        void *data;			/* additional data */
//        lua_State *L;			/* Lua state (for reader) */
//    };
        protected int n = 0;
        protected byte[] p = null;
        protected int POS_p = 0;
        protected lua_Reader reader = null;
        protected Object data = null;
        protected lua_State L = null;
    }
}
