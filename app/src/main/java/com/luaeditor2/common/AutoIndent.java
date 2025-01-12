package com.luaeditor2.common;

import android.util.Log;
import com.luaeditor2.LuaLexer;
import com.luaeditor2.LuaTokenTypes;
import java.io.IOException;

public class AutoIndent {
    
    /*
    public static CharSequence repeatText(CharSequence cs, int ts) {
        StringBuilder sb = new StringBuilder(text.length())
        for (int i = 0; i < cs.length(); i++)
            sb.append(text)
        return sb.toString();
    }
    
    public static CharSequence format(CharSequence text, int width) {
        String code = new StringBuilder(text.length()).append(text).toString();
        
        StringBuilder sb = new StringBuilder();
        int deep = 0;
        
        Iterator<String> i = code.lines().iterator()
        while (i.hasNext()) {
            String line = i.next().trim();
            sb.append(line);
            switch (line.substring(0, line.indexOf(" ") - 1)) {
                case "switch":
                case "for":
                case "do":
                case "while":
                case "if":
                    deep += 1;
                    break;
            }
        }
        
        return null;
    }*/
    
    
    public static int createAutoIndent(CharSequence text) {
        LuaLexer lexer = new LuaLexer(text);
        int idt = 0;
        try {
            while (true) {
                LuaTokenTypes type = lexer.advance();
                if (type == null) {
                    break;
                }
                if (lexer.yytext().equals("switch"))
                    idt += 1;
                else
                    idt += indent(type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return idt;
    }
    
    // md今天被androidide坑了
    // byd这他妈打开的是影子文件吗
    // 修改没效果的

    private static int indent(LuaTokenTypes t) {
        switch (t) {
            case FOR:
            case WHILE:
            case FUNCTION:
            case IF:
            case REPEAT:
            case LCURLY:
            case SWITCH:
            // case DO:
                return 1;
            case UNTIL:
            case END:
            case RCURLY:
                return -1;
            default:
                return 0;
        }
    }

    public static CharSequence format(CharSequence text, int width) {
        StringBuilder builder = new StringBuilder();
        boolean isNewLine = true;
        LuaLexer lexer = new LuaLexer(text);
        try {
            int idt = 0;

            while (true) {
                LuaTokenTypes type = lexer.advance();
                if (type == null)
                    break;

                if (type == LuaTokenTypes.NEW_LINE) {
                    if(builder.length()>0&&builder.charAt(builder.length()-1)==' ')
                        builder.deleteCharAt(builder.length()-1);
                    isNewLine = true;
                    builder.append('\n');
                    idt = Math.max(0, idt);
                } else if (isNewLine) {
                    // Log.e("lua", lexer.yytext());
                    if ("do".equals(lexer.yytext())) {
                        // idt++;
                        builder.append(createIndent(idt * width));
                        builder.append(lexer.yytext());
                        // idt--;
                        isNewLine = true;
                        idt++;
                        continue;
                    }
                    switch (type) {
                        case WHITE_SPACE:
                            break;
                        case ELSE:
                        case ELSEIF:
                        case CASE:
                        case DEFAULT:
                            //idt--;
                            builder.append(createIndent(idt * width - width / 2));
                            builder.append(lexer.yytext());
                            //idt++;
                            isNewLine = false;
                            break;
                        case DOUBLE_COLON:
                        case AT:
                            builder.append(lexer.yytext());
                            isNewLine = false;
                            break;
                        case END:
                        case UNTIL:
                        case RCURLY:
                            idt--;
                            builder.append(createIndent(idt * width));
                            builder.append(lexer.yytext());
                            isNewLine = false;
                            break;
                        default:
                            builder.append(createIndent(idt * width));
                            builder.append(lexer.yytext());
                            idt += indent(type);
                            isNewLine = false;
                    }
                } else if (type == LuaTokenTypes.WHITE_SPACE) {
                    builder.append(' ');
                } else {
                    builder.append(lexer.yytext());
                    idt += indent(type);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder;
    }

    private static char[] createIndent(int n) {
        if (n < 0)
            return new char[0];
        char[] idts = new char[n];
        for (int i = 0; i < n; i++)
            idts[i] = ' ';
        return idts;
    }

}
