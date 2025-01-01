package com.luaeditor2.aluasyntax;

import static com.luaeditor2.aluasyntax.lmem.*;
import com.luaeditor2.aluasyntax.structs.*;
public class lzio {
    protected lzio() {

    }
    //    #define zgetc(z)  (((z)->n--)>0 ?  cast_uchar(*(z)->p++) : luaZ_fill(z))
    protected static byte zgetc(ZIO z) {
        if ((z.n--) > 0) {
            return z.p[z.POS_p++];
        } else {
            return luaZ_fill(z);
        }
    }

//    void luaZ_init (lua_State *L, ZIO *z, lua_Reader reader, void *data) {
//        z->L = L;
//        z->reader = reader;
//        z->data = data;
//        z->n = 0;
//        z->p = NULL;
//    }
    public static void luaZ_init(lua_State L, ZIO z, lua_Reader reader, Object data) {
        z.L = L;
        z.reader = reader;
        z.data = data;
        z.n = 0;
        z.p = null;
    }


//    /* --------------------------------------------------------------- read --- */
//    size_t luaZ_read (ZIO *z, void *b, size_t n) {
//        while (n) {
//            size_t m;
//            if (z->n == 0) {  /* no bytes in buffer? */
//                if (luaZ_fill(z) == EOZ)  /* try to read more */
//                    return n;  /* no more input; return number of missing bytes */
//                else {
//                    z->n++;  /* luaZ_fill consumed first byte; put it back */
//                    z->p--;
//                }
//            }
//            m = (n <= z->n) ? n : z->n;  /* min. between n and z->n */
//            memcpy(b, z->p, m);
//            z->n -= m;
//            z->p += m;
//            b = (char *)b + m;
//            n -= m;
//        }
//        return 0;
//    }
    protected static int luaZ_read(ZIO z, byte[] b, int n) {
        while (n > 0) {
            int m;
            if (z.n == 0) {
                if (luaZ_fill(z) == EOZ) {
                    return n;
                } else {
                    z.n++;
                    z.POS_p--;
                }
            }
            m = (n <= z.n) ? n : z.n;
            System.arraycopy(z.p, z.POS_p, b, 0, m);
            z.n -= m;
            z.POS_p += m;
            n -= m;
        }
        return 0;
    }


    //    int luaZ_fill (ZIO *z) {
//        size_t size;
//        lua_State *L = z->L;
//        const char *buff;
//        lua_unlock(L);
//        buff = z->reader(L, z->data, &size);
//        lua_lock(L);
//        if (buff == NULL || size == 0)
//            return EOZ;
//        z->n = size - 1;  /* discount char being returned */
//        z->p = buff;
//        return cast_uchar(*(z->p++));
//    }
    protected static byte luaZ_fill(ZIO z) {
        int[] size = new int[]{0};
        lua_State L = z.L;
        byte[] buff;
        buff = z.reader.getS(L, z.data, size);
        if (buff == null || size[0] == 0) {
            return EOZ;
        }
        z.n = size[0] - 1;
        z.p = buff;
        return z.p[z.POS_p++];
    }

    //    #define EOZ	(-1)			/* end of stream */
    protected static final byte EOZ = -1;

    //    #define luaZ_bufflen(buff)	((buff)->n)
    protected static int luaZ_bufflen(Mbuffer buff) {
        return buff.n;
    }

    protected static int luaZ_bufflen(Mbuffer buff, int plus) {
        int n = buff.n;
        buff.n = n + plus;
        return n;
    }

    //    #define luaZ_sizebuffer(buff)	((buff)->buffsize)
    protected static int luaZ_sizebuffer(Mbuffer buff) {
        return buff.buffsize;
    }

    //    #define luaZ_resizebuffer(L, buff, size) \
//            ((buff)->buffer = luaM_reallocvchar(L, (buff)->buffer, \
//            (buff)->buffsize, size), \
//            (buff)->buffsize = size)
    protected static void luaZ_resizebuffer(lua_State L, Mbuffer buff, int size) {
        buff.buffer = luaM_reallocvchar(L, buff.buffer, buff.buffsize, size);
        buff.buffsize = size;
    }

    //    #define luaZ_buffer(buff)	((buff)->buffer)
    protected static byte[] luaZ_buffer(Mbuffer buff) {
        return buff.buffer;
    }

    //    #define luaZ_resizebuffer(L, buff, size) \
//            ((buff)->buffer = luaM_reallocvchar(L, (buff)->buffer, \
//            (buff)->buffsize, size), \
//            (buff)->buffsize = size)
    protected static void luaZ_resizebuffer(lua_State L, ZIO z, int size) {
        z.p = luaM_reallocvchar(L, z.p, z.n, size);
        z.n = size;
    }

    //    #define luaZ_resetbuffer(buff) ((buff)->n = 0)
    protected static void luaZ_resetbuffer(Mbuffer buff) {
        buff.n = 0;
    }
//    #define luaZ_buffremove(buff,i)	((buff)->n -= (i))
    protected static void luaZ_buffremove(Mbuffer buff, int i) {
        buff.n -= i;
    }

//    #define luaZ_initbuffer(L, buff) ((buff)->buffer = NULL, (buff)->buffsize = 0)
    protected static void luaZ_initbuffer(lua_State L, Mbuffer buff) {
        buff.buffer = null;
        buff.buffsize = 0;
    }
}
