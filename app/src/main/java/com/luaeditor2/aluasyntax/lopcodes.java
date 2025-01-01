package com.luaeditor2.aluasyntax;
import com.luaeditor2.aluasyntax.structs.*;
public class lopcodes {
    public lopcodes() {
    }

    //    typedef enum {
///*----------------------------------------------------------------------
//name		args	description
//------------------------------------------------------------------------*/
//        OP_MOVE,/*	A B	R(A) := R(B)					*/
//                OP_LOADK,/*	A Bx	R(A) := Kst(Bx)					*/
//                OP_LOADKX,/*	A 	R(A) := Kst(extra arg)				*/
//                OP_LOADBOOL,/*	A B C	R(A) := (Bool)B; if (C) pc++			*/
//                OP_LOADNIL,/*	A B	R(A), R(A+1), ..., R(A+B) := nil		*/
//                OP_GETUPVAL,/*	A B	R(A) := UpValue[B]				*/
//
//                OP_GETTABUP,/*	A B C	R(A) := UpValue[B][RK(C)]			*/
//                OP_GETTABLE,/*	A B C	R(A) := R(B)[RK(C)]				*/
//
//                OP_SETTABUP,/*	A B C	UpValue[A][RK(B)] := RK(C)			*/
//                OP_SETUPVAL,/*	A B	UpValue[B] := R(A)				*/
//                OP_SETTABLE,/*	A B C	R(A)[RK(B)] := RK(C)				*/
//
//                OP_NEWTABLE,/*	A B C	R(A) := {} (size = B,C)				*/
//
//                OP_SELF,/*	A B C	R(A+1) := R(B); R(A) := R(B)[RK(C)]		*/
//
//                OP_ADD,/*	A B C	R(A) := RK(B) + RK(C)				*/
//                OP_SUB,/*	A B C	R(A) := RK(B) - RK(C)				*/
//                OP_MUL,/*	A B C	R(A) := RK(B) * RK(C)				*/
//                OP_MOD,/*	A B C	R(A) := RK(B) % RK(C)				*/
//                OP_POW,/*	A B C	R(A) := RK(B) ^ RK(C)				*/
//                OP_DIV,/*	A B C	R(A) := RK(B) / RK(C)				*/
//                OP_IDIV,/*	A B C	R(A) := RK(B) // RK(C)				*/
//                OP_BAND,/*	A B C	R(A) := RK(B) & RK(C)				*/
//                OP_BOR,/*	A B C	R(A) := RK(B) | RK(C)				*/
//                OP_BXOR,/*	A B C	R(A) := RK(B) ~ RK(C)				*/
//                OP_SHL,/*	A B C	R(A) := RK(B) << RK(C)				*/
//                OP_SHR,/*	A B C	R(A) := RK(B) >> RK(C)				*/
//                OP_UNM,/*	A B	R(A) := -R(B)					*/
//                OP_BNOT,/*	A B	R(A) := ~R(B)					*/
//                OP_NOT,/*	A B	R(A) := not R(B)				*/
//                OP_LEN,/*	A B	R(A) := length of R(B)				*/
//
//                OP_CONCAT,/*	A B C	R(A) := R(B).. ... ..R(C)			*/
//
//                OP_JMP,/*	A sBx	pc+=sBx; if (A) close all upvalues >= R(A - 1)	*/
//                OP_EQ,/*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
//                OP_LT,/*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++		*/
//                OP_LE,/*	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++		*/
//
//                OP_TEST,/*	A C	if not (R(A) <=> C) then pc++			*/
//                OP_TESTSET,/*	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/
//
//                OP_CALL,/*	A B C	R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1)) */
//                OP_TAILCALL,/*	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
//                OP_RETURN,/*	A B	return R(A), ... ,R(A+B-2)	(see note)	*/
//
//                OP_FORLOOP,/*	A sBx	R(A)+=R(A+2);
//			if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }*/
//                OP_FORPREP,/*	A sBx	R(A)-=R(A+2); pc+=sBx				*/
//
//                OP_TFORCALL,/*	A C	R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));	*/
//                OP_TFORLOOP,/*	A sBx	if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx }*/
//
//                OP_SETLIST,/*	A B C	R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B	*/
//
//                OP_CLOSURE,/*	A Bx	R(A) := closure(KPROTO[Bx])			*/
//
//                OP_VARARG,/*	A B	R(A), R(A+1), ..., R(A+B-2) = vararg		*/
//
//                OP_EXTRAARG,/*	Ax	extra (larger) argument for previous opcode	*/
//                OP_TBC,
//                OP_NEWARRAY,/*	A B C	R(A) := {} (size = B,C)				*/
//                OP_TFOREACH,/*	A C	R(A), R(A+1), R(A+2), := pairs(R(A+1))	*/
//                OP_SECTION,/*	A B C    R(A) := R(A)[RK(B):RK(C)]			*/
//                OP_IS,/*	A B C    R(A) := type(RK(B))== RK(C)			*/
//                OP_TRY,/*	A B C    xpcall(R(A),R(A+1))			*/
//                OP_TRYRETURN,/* CallInfo-- & A B	Treturn R(A), ... ,R(A+B-2)	(see note)	*/
//                OP_FULLOPSIZE
//    } OpCode;
    public static final int OP_MOVE = 0;
    public static final int OP_LOADK = OP_MOVE + 1;
    public static final int OP_LOADKX = OP_LOADK + 1;
    public static final int OP_LOADBOOL = OP_LOADKX + 1;
    public static final int OP_LOADNIL = OP_LOADBOOL + 1;
    public static final int OP_GETUPVAL = OP_LOADNIL + 1;
    public static final int OP_GETTABUP = OP_GETUPVAL + 1;
    public static final int OP_GETTABLE = OP_GETTABUP + 1;
    public static final int OP_SETTABUP = OP_GETTABLE + 1;
    public static final int OP_SETUPVAL = OP_SETTABUP + 1;
    public static final int OP_SETTABLE = OP_SETUPVAL + 1;
    public static final int OP_NEWTABLE = OP_SETTABLE + 1;
    public static final int OP_SELF = OP_NEWTABLE + 1;
    public static final int OP_ADD = OP_SELF + 1;
    public static final int OP_SUB = OP_ADD + 1;
    public static final int OP_MUL = OP_SUB + 1;
    public static final int OP_MOD = OP_MUL + 1;
    public static final int OP_POW = OP_MOD + 1;
    public static final int OP_DIV = OP_POW + 1;
    public static final int OP_IDIV = OP_DIV + 1;
    public static final int OP_BAND = OP_IDIV + 1;
    public static final int OP_BOR = OP_BAND + 1;
    public static final int OP_BXOR = OP_BOR + 1;
    public static final int OP_SHL = OP_BXOR + 1;
    public static final int OP_SHR = OP_SHL + 1;
    public static final int OP_UNM = OP_SHR + 1;
    public static final int OP_BNOT = OP_UNM + 1;
    public static final int OP_NOT = OP_BNOT + 1;
    public static final int OP_LEN = OP_NOT + 1;
    public static final int OP_CONCAT = OP_LEN + 1;
    public static final int OP_JMP = OP_CONCAT + 1;
    public static final int OP_EQ = OP_JMP + 1;
    public static final int OP_LT = OP_EQ + 1;
    public static final int OP_LE = OP_LT + 1;
    public static final int OP_TEST = OP_LE + 1;
    public static final int OP_TESTSET = OP_TEST + 1;
    public static final int OP_CALL = OP_TESTSET + 1;
    public static final int OP_TAILCALL = OP_CALL + 1;
    public static final int OP_RETURN = OP_TAILCALL + 1;
    public static final int OP_FORLOOP = OP_RETURN + 1;
    public static final int OP_FORPREP = OP_FORLOOP + 1;
    public static final int OP_TFORCALL = OP_FORPREP + 1;
    public static final int OP_TFORLOOP = OP_TFORCALL + 1;
    public static final int OP_SETLIST = OP_TFORLOOP + 1;
    public static final int OP_CLOSURE = OP_SETLIST + 1;
    public static final int OP_VARARG = OP_CLOSURE + 1;
    public static final int OP_EXTRAARG = OP_VARARG + 1;
    public static final int OP_TBC = OP_EXTRAARG + 1;
    public static final int OP_NEWARRAY = OP_TBC + 1;
    public static final int OP_TFOREACH = OP_NEWARRAY + 1;
    public static final int OP_SECTION = OP_TFOREACH + 1;
    public static final int OP_IS = OP_SECTION + 1;
    public static final int OP_TRY = OP_IS + 1;
    public static final int OP_TRYRETURN = OP_TRY + 1;

    //    enum OpMode {
//        iABC, iABx, iAsBx, iAx
//    };  /* basic instruction format */
    public static final int iABC = 0;
    public static final int iABx = iABC + 1;
    public static final int iAsBx = iABx + 1;
    public static final int iAx = iAsBx + 1;

    //    #define SIZE_C        9
//            #define SIZE_B        9
//            #define SIZE_Bx        (SIZE_C + SIZE_B)
//#define SIZE_A        8
//            #define SIZE_Ax        (SIZE_C + SIZE_B + SIZE_A)
//
//#define SIZE_OP        6
//
//            #define POS_OP        0
//            #define POS_A        (POS_OP + SIZE_OP)
//#define POS_C        (POS_A + SIZE_A)
//#define POS_B        (POS_C + SIZE_C)
//#define POS_Bx        POS_C
//#define POS_Ax        POS_A
    public static final int SIZE_C = 9;
    public static final int SIZE_B = 9;
    public static final int SIZE_Bx = (SIZE_C + SIZE_B);
    public static final int SIZE_A = 8;
    public static final int SIZE_Ax = 8;
    public static final int SIZE_OP = 6;
    public static final int POS_OP = 0;
    public static final int POS_A = (POS_OP + SIZE_OP);
    public static final int POS_C = (POS_A + SIZE_A);
    public static final int POS_B = (POS_C + SIZE_C);
    public static final int POS_Bx = POS_C;
    public static final int POS_Ax = POS_A;

    //#define MAXARG_Bx        ((1<<SIZE_Bx)-1)
//            #define MAXARG_sBx        (MAXARG_Bx>>1)         /* 'sBx' is signed */
    public static final int MAXARG_Bx = ((1 << SIZE_Bx) - 1);
    public static final int MAXARG_sBx = (MAXARG_Bx >> 1);

    //    #define MAXARG_Ax    ((1<<SIZE_Ax)-1)
    public static final int MAXARG_Ax = ((1 << SIZE_Ax) - 1);
    //    #define MAXARG_A        ((1<<SIZE_A)-1)
//            #define MAXARG_B        ((1<<SIZE_B)-1)
//            #define MAXARG_C        ((1<<SIZE_C)-1)
    public static final int MAXARG_A = ((1 << SIZE_A) - 1);
    public static final int MAXARG_B = ((1 << SIZE_B) - 1);
    public static final int MAXARG_C = ((1 << SIZE_C) - 1);

    //#define MASK1(n, p)    ((~((~(Instruction)0)<<(n)))<<(p))
    //    #define MASK0(n, p)    (~MASK1(n,p))
    //#define GET_OPCODE(i)    (cast(OpCode, ((i)>>POS_OP) & MASK1(SIZE_OP,0)))
//            #define SET_OPCODE(i, o)    ((i) = (((i)&MASK0(SIZE_OP,POS_OP)) | \
//            ((cast(Instruction, o)<<POS_OP)&MASK1(SIZE_OP,POS_OP))))
//            #define getarg(i, pos, size)    (cast(int, ((i)>>pos) & MASK1(size,0)))
//            #define setarg(i, v, pos, size)    ((i) = (((i)&MASK0(size,pos)) | \
//            ((cast(Instruction, v)<<pos)&MASK1(size,pos))))
//
//            #define GETARG_A(i)    getarg(i, POS_A, SIZE_A)
//            #define SETARG_A(i, v)    setarg(i, v, POS_A, SIZE_A)
//
//            #define GETARG_B(i)    getarg(i, POS_B, SIZE_B)
//            #define SETARG_B(i, v)    setarg(i, v, POS_B, SIZE_B)
//
//            #define GETARG_C(i)    getarg(i, POS_C, SIZE_C)
//            #define SETARG_C(i, v)    setarg(i, v, POS_C, SIZE_C)
//
//            #define GETARG_Bx(i)    getarg(i, POS_Bx, SIZE_Bx)
//            #define SETARG_Bx(i, v)    setarg(i, v, POS_Bx, SIZE_Bx)
//
//            #define GETARG_Ax(i)    getarg(i, POS_Ax, SIZE_Ax)
//            #define SETARG_Ax(i, v)    setarg(i, v, POS_Ax, SIZE_Ax)
//
//            #define GETARG_sBx(i)    (GETARG_Bx(i)-MAXARG_sBx)
//            #define SETARG_sBx(i, b)    SETARG_Bx((i),cast(unsigned int, (b)+MAXARG_sBx))
    public static int MASK1(int n, int p) {
        return ((~((~(int) 0) << (n))) << (p));
    }

    public static int MASK0(int n, int p) {
        return (~MASK1(n, p));
    }

    public static int GET_OPCODE(int i) {
        return ((i >> POS_OP) & MASK1(SIZE_OP, 0));
    }

    public static int GET_OPCODE(Instruction i) {
        return GET_OPCODE(i.i);
    }

    public static int SET_OPCODE(int i, int o) {
        return ((i & MASK0(SIZE_OP, POS_OP)) | ((o << POS_OP) & MASK1(SIZE_OP, POS_OP)));
    }

    public static int SET_OPCODE(Instruction i, int o) {
        i.i = SET_OPCODE(i.i, o);
        return i.i;
    }

    public static int getarg(int i, int pos, int size) {
        return ((i >> pos) & MASK1(size, 0));
    }

    public static int getarg(Instruction i, int pos, int size) {
        return getarg(i.i, pos, size);
    }

    public static int setarg(int i, int v, int pos, int size) {
        return ((i & MASK0(size, pos)) | ((v << pos) & MASK1(size, pos)));
    }

    public static int setarg(Instruction i, int v, int pos, int size) {
        i.i = setarg(i.i, v, pos, size);
        return i.i;
    }

    public static int GETARG_A(int i) {
        return getarg(i, POS_A, SIZE_A);
    }

    public static int GETARG_A(Instruction i) {
        return getarg(i, POS_A, SIZE_A);
    }

    public static int SETARG_A(int i, int v) {
        return setarg(i, v, POS_A, SIZE_A);
    }

    public static int SETARG_A(Instruction i, int v) {
        return setarg(i, v, POS_A, SIZE_A);
    }

    public static int GETARG_B(int i) {
        return getarg(i, POS_B, SIZE_B);
    }

    public static int GETARG_B(Instruction i) {
        return getarg(i, POS_B, SIZE_B);
    }

    public static int SETARG_B(int i, int v) {
        return setarg(i, v, POS_B, SIZE_B);
    }

    public static int SETARG_B(Instruction i, int v) {
        return setarg(i, v, POS_B, SIZE_B);
    }


    public static int GETARG_C(int i) {
        return getarg(i, POS_C, SIZE_C);
    }

    public static int GETARG_C(Instruction i) {
        return getarg(i, POS_C, SIZE_C);
    }

    public static int SETARG_C(int i, int v) {
        return setarg(i, v, POS_C, SIZE_C);
    }

    public static int SETARG_C(Instruction i, int v) {
        return setarg(i, v, POS_C, SIZE_C);
    }

    public static int GETARG_Bx(int i) {
        return getarg(i, POS_Bx, SIZE_Bx);
    }

    public static int GETARG_Bx(Instruction i) {
        return getarg(i, POS_Bx, SIZE_Bx);
    }

    public static int SETARG_Bx(int i, int v) {
        return setarg(i, v, POS_Bx, SIZE_Bx);
    }

    public static int SETARG_Bx(Instruction i, int v) {
        return setarg(i, v, POS_Bx, SIZE_Bx);
    }


    public static int GETARG_sBx(int i) {
        return (GETARG_Bx(i) - MAXARG_sBx);
    }

    public static int GETARG_sBx(Instruction i) {
        return (GETARG_Bx(i) - MAXARG_sBx);
    }

    public static int SETARG_sBx(int i, int v) {
        return SETARG_Bx(i, v + MAXARG_sBx);
    }

    public static int SETARG_sBx(Instruction i, int v) {
        return SETARG_Bx(i, v + MAXARG_sBx);
    }

    public static int GETARG_Ax(int i) {
        return getarg(i, POS_Ax, SIZE_Ax);
    }

    public static int GETARG_Ax(Instruction i) {
        return getarg(i, POS_Ax, SIZE_Ax);
    }

    public static int SETARG_Ax(int i, int v) {
        return setarg(i, v, POS_Ax, SIZE_Ax);
    }

    public static int SETARG_Ax(Instruction i, int v) {
        return setarg(i, v, POS_Ax, SIZE_Ax);
    }

    public static int CREATE_ABC(int o, int a, int b, int c) {
        return (SET_OPCODE(0, o) | SETARG_A(0, a) | SETARG_B(0, b) | SETARG_C(0, c));
    }

    public static int CREATE_ABx(int o, int a, int bc) {
        return (SET_OPCODE(0, o) | SETARG_A(0, a) | SETARG_Bx(0, bc));
    }

    public static int CREATE_AsBx(int o, int a, int bc) {
        return (SET_OPCODE(0, o) | SETARG_A(0, a) | SETARG_sBx(0, bc));
    }

    public static int CREATE_Ax(int o, int a) {
        return (SET_OPCODE(0, o) | SETARG_Ax(0, a));
    }

    public static int BITRK = 1 << (SIZE_B - 1);

    public static boolean ISK(int x) {
        return (x & BITRK) != 0;
    }

    public static int INDEXK(long r) {
        return ((int) (r) & ~BITRK);
    }

    public static int MAXINDEXRK = BITRK - 1;

    public static int RKASK(int x) {
        return (x | BITRK);
    }

    public static int NO_REG = MAXARG_A;
    //    enum OpArgMask {
//        OpArgN,  /* argument is not used */
//        OpArgU,  /* argument is used */
//        OpArgR,  /* argument is a register or a jump offset */
//        OpArgK   /* argument is a constant or register/constant */
//    };
    public static final int OpArgN = 0;
    public static final int OpArgU = OpArgN + 1;
    public static final int OpArgR = OpArgU + 1;
    public static final int OpArgK = OpArgR + 1;

    //    #define opmode(t, a, b, c, m) (((t)<<7) | ((a)<<6) | ((b)<<4) | ((c)<<2) | (m))
    public static int opmode(int t, int a, int b, int c, int m) {
        return (((t) << 7) | ((a) << 6) | ((b) << 4) | ((c) << 2) | (m));
    }

//    LUAI_DDEF const lu_byte luaP_opmodes[NUM_OPCODES] = {
//        /*       T  A    B       C     mode		   opcode	*/
//        opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_MOVE */
//                , opmode(0, 1, OpArgK, OpArgN, iABx)        /* OP_LOADK */
//                , opmode(0, 1, OpArgN, OpArgN, iABx)        /* OP_LOADKX */
//                , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_LOADBOOL */
//                , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_LOADNIL */
//                , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_GETUPVAL */
//                , opmode(0, 1, OpArgU, OpArgK, iABC)        /* OP_GETTABUP */
//                , opmode(0, 1, OpArgR, OpArgK, iABC)        /* OP_GETTABLE */
//                , opmode(0, 0, OpArgK, OpArgK, iABC)        /* OP_SETTABUP */
//                , opmode(0, 0, OpArgU, OpArgN, iABC)        /* OP_SETUPVAL */
//                , opmode(0, 0, OpArgK, OpArgK, iABC)        /* OP_SETTABLE */
//                , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_NEWTABLE */
//                , opmode(0, 1, OpArgR, OpArgK, iABC)        /* OP_SELF */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_ADD */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SUB */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_MUL */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_MOD */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_POW */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_DIV */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_IDIV */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_BAND */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_BOR */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_BXOR */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SHL */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SHR */
//                , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_UNM */
//                , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_BNOT */
//                , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_NOT */
//                , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_LEN */
//                , opmode(0, 1, OpArgR, OpArgR, iABC)        /* OP_CONCAT */
//                , opmode(0, 0, OpArgR, OpArgN, iAsBx)        /* OP_JMP */
//                , opmode(1, 0, OpArgK, OpArgK, iABC)        /* OP_EQ */
//                , opmode(1, 0, OpArgK, OpArgK, iABC)        /* OP_LT */
//                , opmode(1, 0, OpArgK, OpArgK, iABC)        /* OP_LE */
//                , opmode(1, 0, OpArgN, OpArgU, iABC)        /* OP_TEST */
//                , opmode(1, 1, OpArgR, OpArgU, iABC)        /* OP_TESTSET */
//                , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_CALL */
//                , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_TAILCALL */
//                , opmode(0, 0, OpArgU, OpArgN, iABC)        /* OP_RETURN */
//                , opmode(0, 1, OpArgR, OpArgN, iAsBx)        /* OP_FORLOOP */
//                , opmode(0, 1, OpArgR, OpArgN, iAsBx)        /* OP_FORPREP */
//                , opmode(0, 0, OpArgN, OpArgU, iABC)        /* OP_TFORCALL */
//                , opmode(0, 1, OpArgR, OpArgN, iAsBx)        /* OP_TFORLOOP */
//                , opmode(0, 0, OpArgU, OpArgU, iABC)        /* OP_SETLIST */
//                , opmode(0, 1, OpArgU, OpArgN, iABx)        /* OP_CLOSURE */
//                , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_VARARG */
//                , opmode(0, 0, OpArgU, OpArgU, iAx)            /* OP_EXTRAARG */
//                , opmode(0, 0, OpArgN, OpArgN, iABC)        /* OP_TBC */
//                , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_NEWARRAY */
//                , opmode(0, 0, OpArgN, OpArgU, iABC)        /* OP_TFOREACH */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SECTION */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_IS */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_TRY */
//                , opmode(0, 0, OpArgU, OpArgN, iABC)        /* OP_TRYRETURN */
//                , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_FULLOPSIZE */
//    };

    public static final int luaP_opmodes[] = {
            /*       T  A    B       C     mode		   opcode	*/
            opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_MOVE */
            , opmode(0, 1, OpArgK, OpArgN, iABx)        /* OP_LOADK */
            , opmode(0, 1, OpArgN, OpArgN, iABx)        /* OP_LOADKX */
            , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_LOADBOOL */
            , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_LOADNIL */
            , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_GETUPVAL */
            , opmode(0, 1, OpArgU, OpArgK, iABC)        /* OP_GETTABUP */
            , opmode(0, 1, OpArgR, OpArgK, iABC)        /* OP_GETTABLE */
            , opmode(0, 0, OpArgK, OpArgK, iABC)        /* OP_SETTABUP */
            , opmode(0, 0, OpArgU, OpArgN, iABC)        /* OP_SETUPVAL */
            , opmode(0, 0, OpArgK, OpArgK, iABC)        /* OP_SETTABLE */
            , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_NEWTABLE */
            , opmode(0, 1, OpArgR, OpArgK, iABC)        /* OP_SELF */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_ADD */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SUB */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_MUL */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_MOD */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_POW */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_DIV */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_IDIV */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_BAND */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_BOR */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_BXOR */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SHL */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SHR */
            , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_UNM */
            , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_BNOT */
            , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_NOT */
            , opmode(0, 1, OpArgR, OpArgN, iABC)        /* OP_LEN */
            , opmode(0, 1, OpArgR, OpArgR, iABC)        /* OP_CONCAT */
            , opmode(0, 0, OpArgR, OpArgN, iAsBx)        /* OP_JMP */
            , opmode(1, 0, OpArgK, OpArgK, iABC)        /* OP_EQ */
            , opmode(1, 0, OpArgK, OpArgK, iABC)        /* OP_LT */
            , opmode(1, 0, OpArgK, OpArgK, iABC)        /* OP_LE */
            , opmode(1, 0, OpArgN, OpArgU, iABC)        /* OP_TEST */
            , opmode(1, 1, OpArgR, OpArgU, iABC)        /* OP_TESTSET */
            , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_CALL */
            , opmode(0, 1, OpArgU, OpArgU, iABC)        /* OP_TAILCALL */
            , opmode(0, 0, OpArgU, OpArgN, iABC)        /* OP_RETURN */
            , opmode(0, 1, OpArgR, OpArgN, iAsBx)        /* OP_FORLOOP */
            , opmode(0, 1, OpArgR, OpArgN, iAsBx)        /* OP_FORPREP */
            , opmode(0, 0, OpArgN, OpArgU, iABC)        /* OP_TFORCALL */
            , opmode(0, 1, OpArgR, OpArgN, iAsBx)        /* OP_TFORLOOP */
            , opmode(0, 0, OpArgU, OpArgU, iABC)        /* OP_SETLIST */
            , opmode(0, 1, OpArgU, OpArgN, iABx)        /* OP_CLOSURE */
            , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_VARARG */
            , opmode(0, 0, OpArgU, OpArgU, iAx)            /* OP_EXTRAARG */
            , opmode(0, 0, OpArgN, OpArgN, iABC)        /* OP_TBC */
            , opmode(0, 1, OpArgU, OpArgN, iABC)        /* OP_NEWARRAY */
            , opmode(0, 0, OpArgN, OpArgU, iABC)        /* OP_TFOREACH */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_SECTION */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_IS */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_TRY */
            , opmode(0, 0, OpArgU, OpArgN, iABC)        /* OP_TRYRETURN */
            , opmode(0, 1, OpArgK, OpArgK, iABC)        /* OP_FULLOPSIZE */
    };

    //#define getOpMode(m)    (cast(enum OpMode, luaP_opmodes[m] & 3))
//            #define getBMode(m)    (cast(enum OpArgMask, (luaP_opmodes[m] >> 4) & 3))
//            #define getCMode(m)    (cast(enum OpArgMask, (luaP_opmodes[m] >> 2) & 3))
//            #define testAMode(m)    (luaP_opmodes[m] & (1 << 6))
//            #define testTMode(m)    (luaP_opmodes[m] & (1 << 7))
    public static int getOpMode(int m) {
        return (luaP_opmodes[m] & 3);
    }

    public static int getBMode(int m) {
        return ((luaP_opmodes[m] >> 4) & 3);
    }

    public static int getCMode(int m) {
        return ((luaP_opmodes[m] >> 2) & 3);
    }

    public static boolean testAMode(int m) {
        return (luaP_opmodes[m] & (1 << 6)) != 0;
    }

    public static boolean testTMode(int m) {
        return (luaP_opmodes[m] & (1 << 7)) != 0;
    }

    //    #define LFIELDS_PER_FLUSH    50
    public static final int LFIELDS_PER_FLUSH = 50;
}
