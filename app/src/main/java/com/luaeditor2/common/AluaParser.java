package com.luaeditor2.common;

import android.graphics.Rect;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import static com.luaeditor2.aluasyntax.structs.*;

import static com.luaeditor2.aluasyntax.ldo.luaD_protectedparser;
import static com.luaeditor2.aluasyntax.lzio.luaZ_init;

import com.luaeditor2.aluasyntax.*;

public class AluaParser {
    // ==============================================================================
    public static ArrayList<Pair> LexState_tokens = new ArrayList<>();
    public static ArrayList<Rect> LexState_lines = new ArrayList<>();
    public static ArrayList<String> LexState_globals = new ArrayList<>();
    public static HashMap<String, ArrayList<Pair>> LexState_valueMap = new HashMap<>();
    public static String LexState_errormsg = null;
    public static int LexState_erroridx = -1;
    public static int LexState_errorline = -1;
    // ==============================================================================
    private static HashMap<String, ArrayList<Pair>> localMap = new HashMap<>();
    public static HashMap<String, ArrayList<String>> javaMethodMap = new HashMap<>();
    public static HashMap<String, ArrayList<String>> javaFieldMap = new HashMap<>();
    public static HashMap<String, ArrayList<JavaVar>> javaVar = new HashMap<>();
    private static ArrayList<Var> varList = new ArrayList<>();
    private static ArrayList<String> globalist = new ArrayList<>();
    private static HashMap<String, ArrayList<Pair>> valueMap = new HashMap<>();

    public static ArrayList<String> filterJava(String pkg, String keyword, int i) {
        //Log.i("luaj", "filterJava: " + pkg + ";" + keyword + ";" + i);
        ArrayList<String> ms = new ArrayList<>();
        ArrayList<JavaVar> js = javaVar.get(pkg);
        if (js == null)
            return ms;
        for (int i1 = js.size() - 1; i1 >= 0; i1--) {
            JavaVar j = js.get(i1);
            //Log.i("luaj", "filterJava: " + pkg + ";" + keyword + ";" + j.name);
            if (j.startidx <= i && j.endidx >= i) {
                if (j.name.toLowerCase().endsWith("." + pkg)) {
                    ArrayList<String> mm = javaFieldMap.get(j.name);
                    if (mm != null) {
                        for (String s : mm) {
                            if (s.toLowerCase().startsWith(keyword))
                                ms.add(s);
                        }
                    }
                }
                ArrayList<String> mm = javaMethodMap.get(j.name);
                if (mm == null)
                    continue;
                for (String s : mm) {
                    if (s.toLowerCase().startsWith(keyword))
                        ms.add(s);
                }
                break;
            }
        }
        return ms;
    }

    public static ArrayList<String> filterJava(String pkg, int i) {
        i = i - pkg.length();
        //Log.i("luaj", "filterJava: " + pkg + ";" + javaVar);
        ArrayList<String> ms = new ArrayList<>();
        ArrayList<JavaVar> js = javaVar.get(pkg);
        //Log.i("luaj", "filterJava: " + pkg + ";" + js);
        if (js == null)
            return ms;
        for (int i1 = js.size() - 1; i1 >= 0; i1--) {
            JavaVar j = js.get(i1);
            //Log.i("luaj", "filterJava: " + pkg + ";" + j.name);
            if (j.startidx <= i && j.endidx >= i) {
                if (j.name.toLowerCase().endsWith("." + pkg)) {
                    ArrayList<String> mm = javaFieldMap.get(j.name);
                    if (mm != null)
                        ms.addAll(mm);
                }
                ArrayList<String> mm = javaMethodMap.get(j.name);
                if (mm != null)
                    ms.addAll(mm);
                break;
            }
        }
        return ms;
    }

    public static HashMap<String, ArrayList<Pair>> getValueMap() {
        return valueMap;
    }

    public static void reset() {
        if (varList.isEmpty())
            return;
        localMap.clear();
        varList.clear();
        javaVar.clear();
        globalist = new ArrayList<>();
        valueMap = new HashMap<>();
        LexState_errormsg = null;
        LexState_globals.clear();
        LexState_lines.clear();
        LexState_valueMap.clear();
        LexState_errorline = -1;
        LexState_erroridx = -1;
    }

    public static class Var {
        public String name;
        public String type;
        public int startidx;
        public int endidx;

        public Var(String n, String t, int s, int e) {
            name = n;
            type = t;
            startidx = s;
            endidx = e;
        }

        @Override
        public String toString() {
            return String.format("Var (%s %s %s-%s)", name, type, startidx, endidx);
        }
    }

    public static class JavaVar {
        public String name;
        public ArrayList<String> method;
        public int startidx;
        public int endidx;

        public JavaVar(String n, int s, int e) {
            name = n;
            startidx = s;
            endidx = e;
        }
    }

    public static class CharInputSteam extends InputStream {

        private final CharSequence mSrc;
        private final int mLen;
        private int idx = 0;

        public CharInputSteam(CharSequence src) {
            mSrc = src;
            mLen = src.length();
        }

        @Override
        public int read() throws IOException {
            idx++;
            if (idx > mLen)
                return -1;
            return mSrc.charAt(idx - 1);
        }
    }

    // 标记: 语法错误
    public static boolean lexer(CharSequence src, Flag _abort) {
        //Log.i("luaj", "lexer: start");
        try {
            //Prototype lex = LuaC.lexer(new CharInputSteam(src), "luaj");
            lua_State L = new lua_State();
            L.inLexer = true;
            L.abort = _abort;
            ZIO z = new ZIO();
            LoadS ls = new LoadS(src);
            luaZ_init(L, z, new lua_Reader(), ls);
            LClosure lc = luaD_protectedparser(L, z, "SyntaxSupport", "bt");
            Proto lex = lc.p;
//            Prototype lex = LuaC.lexer(src, "luaj", _abort);
            localMap.clear();
            varList.clear();
            javaVar.clear();
            lexer(lex);
            if (LexState_erroridx < 0)
                globalist = new ArrayList<>(LexState_globals);
            valueMap = new HashMap<>(LexState_valueMap);
            //Log.i("luaj", "lexer: "+valueMap);
            return true;
        } catch (Exception e) {
            // 标记: 消除错误检测
            // AluaParser.LexState_errormsg = null;
            // e.printStackTrace();
        }
        return false;
    }

    public static HashMap<String, ArrayList<Pair>> getLocalMap() {
        return localMap;
    }

    public static String typename(String n, LocVar l) {
        VarType type = l.type;
        if (type == null)
            return "";
        //Log.i("luaj", "typename: " + n + ';' + type.typename);
        int idx = type.typename.lastIndexOf(".");
        if (idx < 1)
            return type.typename;
        String p = type.typename.substring(0, idx);
        String c = type.typename.substring(idx + 1);
        getJavaMethods(c, type.typename);
        ArrayList<JavaVar> jv = javaVar.get(n);
        if (jv == null) {
            jv = new ArrayList<>();
            javaVar.put(n.toLowerCase(), jv);
        }
        jv.add(new JavaVar(type.typename, l.startidx, l.endidx));
        if (c.equals(n))
            return p;
        return c;
    }

    public static String typename(String n, Upvaldesc l) {
        VarType type = l.type;
        if (type == null)
            return "";
        //Log.i("luaj", "typename: " + n + ';' + type.typename);
        int idx = type.typename.lastIndexOf(".");
        if (idx < 1)
            return type.typename;
        String p = type.typename.substring(0, idx);
        String c = type.typename.substring(idx + 1);
        if (c.equals(n))
            return p;
        return c;
    }

    private static void getJavaMethods(String c, String typename) {
        ArrayList<String> ms = javaMethodMap.get(typename);
        if (ms != null)
            return;
        ms = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName(typename);
            Method[] mm = clazz.getMethods();
            for (Method method : mm) {
                String name = method.getName();
                if (ms.contains(name))
                    continue;
                ms.add(name);
            }
            javaMethodMap.put(typename, ms);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        ArrayList<String> fs = javaFieldMap.get(typename);
        if (fs != null)
            return;
        fs = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName(typename);
            Field[] mm = clazz.getFields();
            for (Field method : mm) {
                String name = method.getName();
                if (fs.contains(name))
                    continue;
                fs.add(name);
            }
            javaFieldMap.put(typename, fs);
        } catch (Exception e) {
            // e.printStackTrace();
        }


    }

    private static void lexer(Proto p) {
        if (p == null)
            return;
        LocVar[] ls = p.locvars;
        int np = p.numparams & 0xff;
        Upvaldesc[] us = p.upvalues;
        for (Upvaldesc l : us) {
            String n = l.name;
            typename(n, l);
            varList.add(new Var(n, " :upval", p.startidx, p.endidx));
        }
        for (int i = 0; i < ls.length; i++) {
            LocVar l = ls[i];
            String n = l.varname;
//            System.out.println("varList SE " + n + " " + l.startidx + " " + l.endidx);
            if (i < np) {
                varList.add(new Var(n, " :arg", l.startidx, l.endidx));
            } else {
                typename(n, l);
                varList.add(new Var(n, " :local", l.startidx, l.endidx));
            }
//            ArrayList<Pair> a = localMap.get(n);
//            if (a == null) {
//                a = new ArrayList<>();
//                localMap.put(n, a);
//            }
//            a.add(new Pair(l.startidx, l.endidx));
        }
        // ArrayList<ASMBlock> asmBlocks = p.asm_blocks;
        // for (ASMBlock asmBlock : asmBlocks) {
            // varList.add(new Var(asmBlock.n, " :asm", asmBlock.startidx, asmBlock.endidx));
        // }

        Proto[] ps = p.p;
        for (Proto l : ps) {
            lexer(l);
        }
    }

    private static ArrayList<String> userWord = new ArrayList<>();

    public static void clearUserWord() {
        userWord.clear();
    }

    public static ArrayList<String> getUserWord() {
        return userWord;
    }

    public static void addUserWord(String s) {
        userWord.add(s);
    }

    public static ArrayList<CharSequence> filterLocal(String name, int idx, ColorScheme colorScheme) {
        ArrayList<CharSequence> ret = new ArrayList<>();
        ArrayList<CharSequence> ca = new ArrayList<>();

        for (int i = varList.size() - 1; i >= 0; i--) {
            Var var = varList.get(i);
            if (var.startidx <= idx && var.endidx >= idx) {
                String n = var.name;
                if (ca.contains(n))
                    continue;
                ca.add(n);
                if (n.toLowerCase().startsWith(name))
                    ret.add(getColorText(n + var.type, colorScheme.getTokenColor(getType(var.type))));
                String p = getSpells(n);
                if (TextUtils.isEmpty(p))
                    continue;
                if (p.startsWith(name))
                    ret.add(getColorText(n + var.type, colorScheme.getTokenColor(getType(var.type))));
            }
        }
        ArrayList<String> ks = globalist;
        for (String k : ks) {
            if (ca.contains(k))
                continue;
            ca.add(k);
            if (k.toLowerCase().startsWith(name))
                ret.add(getColorText(k + " :global", colorScheme.getTokenColor(Lexer.GLOBAL)));
            String p = getSpells(k);
            if (TextUtils.isEmpty(p))
                continue;
            if (p.startsWith(name))
                ret.add(getColorText(k + " :global", colorScheme.getTokenColor(Lexer.GLOBAL)));
        }
        return ret;
    }

    private static int getType(String type) {
        switch (type) {
            case " :upval":
                return Lexer.UPVAL;
            case " :arg":
            case " :local":
                return Lexer.LOCAL;
            case " :global":
                return Lexer.GLOBAL;
            case " :asm":
                return Lexer.KEYWORD;
        }
        return 0;
    }

    private static CharSequence getColorText(String text, int color) {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);
        return ss;
    }

    private static final int GB_SP_DIFF = 160;
    private static final int[] secPosValueList = {1601, 1637, 1833, 2078, 2274, 2302,
            2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027,
            4086, 4390, 4558, 4684, 4925, 5249, 5600};
    private static final char[] firstLetter = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w', 'x',
            'y', 'z'};

    private static String getSpells(String characters) {
        try {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < characters.length(); i++) {
                char ch = characters.charAt(i);
                if (i == 0 && ch < 128) {
                    return null;
                }
                if ((ch >> 7) == 0) {
                    buffer.append(ch);
                } else {
                    char spell = getFirstLetter(ch);
                    if (spell == 0) {
                        continue;
                    }
                    buffer.append(String.valueOf(spell));
                }
            }
            return buffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static char getFirstLetter(char ch) {
        byte[] uniCode = null;
        try {
            uniCode = String.valueOf(ch).getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
            return 0;
        }
        if (uniCode[0] < 128 && uniCode[0] > 0) {
            return 0;
        } else {
            return convert(uniCode);
        }
    }

    public static char convert(byte[] bytes) {
        char result = 0;
        int secPosValue = 0;
        int i;
        for (i = 0; i < bytes.length; i++) {
            bytes[i] -= GB_SP_DIFF;
        }
        secPosValue = bytes[0] * 100 + bytes[1];
        for (i = 0; i < 23; i++) {
            if (secPosValue >= secPosValueList[i]
                    && secPosValue < secPosValueList[i + 1]) {
                result = firstLetter[i];
                break;
            }
        }
        return result;
    }


}
