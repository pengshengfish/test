package com.data.util;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * @Description: 重写XMLWriter中writeDeclaration方法，报文声明添加standalone属性
 * @Author: xuyang
 * @Date: 2022/9/9  10:57
 */
public class StandaloneWriter extends XMLWriter {

    public StandaloneWriter(Writer writer, OutputFormat format) throws UnsupportedEncodingException {
        super(writer, format);
    }


    public StandaloneWriter(OutputStream out, OutputFormat format) throws UnsupportedEncodingException {
        super(out, format);
    }

    @Override
    protected void writeDeclaration()
            throws IOException {
        OutputFormat format = getOutputFormat();
        String encoding = format.getEncoding();
        if (!format.isSuppressDeclaration()) {
            writer.write("<?xml version=\"1.0\"");
            if ("UTF8".equals(encoding)) {
                if (!format.isOmitEncoding()) {
                    writer.write(" encoding=\"UTF-8\"");
                }
            } else {
                if (!format.isOmitEncoding()) {
                    writer.write(" encoding=\"" + encoding + "\"");
                }

            }
            writer.write(" standalone=\"yes\"");
            writer.write("?>");
            if (format.isNewLineAfterDeclaration()) {
                println();
            }
        }
    }

}
