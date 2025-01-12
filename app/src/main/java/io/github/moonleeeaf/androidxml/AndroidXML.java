package io.github.moonleeeaf.androidxml;
import android.content.Context;
import com.axml2xml.Decoder;
import com.xml2axml.XMLBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class AndroidXML {
    
    public static byte[] xml2axml(Context c, File f) throws Exception {
        return XMLBuilder.compileXml(c, f);
    }
    
    public static byte[] xml2axml(Context c, String data) throws Exception {
        return XMLBuilder.compileXml(c, data);
    }
    
    public static byte[] encode(Context c, String data) throws Exception {
        return xml2axml(c, data);
    }
    
    public static byte[] encode(Context c, File f) throws Exception {
        return xml2axml(c, f);
    }
    
    public static String axml2xml(Context c, File f) throws Exception {
        return Decoder.decode(c, f);
    }
    
    public static String axml2xml(Context c, byte[] data) throws Exception {
        return Decoder.decode(c, data);
    }
    
    public static String decode(Context c, File f) throws Exception {
        return axml2xml(c, f);
    }
    
    public static String decode(Context c, byte[] data) throws Exception {
        return axml2xml(c, data);
    }
     
    public static String prettyXML(String xmlString) throws JDOMException, IOException{
        Format format = Format.getPrettyFormat();
        format.setEncoding("utf-8");
        format.setIndent("  ");
        format.setTextMode(Format.TextMode.TRIM_FULL_WHITE);
        XMLOutputter out = new XMLOutputter(format);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        out.output(new SAXBuilder().build(new StringReader(xmlString)), bos);
        return bos.toString();
    }
    
}
