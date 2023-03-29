package com.data.util;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class ZipUtils {

    /**
     * 使用gzip压缩字符串
     *
     * @param str 要压缩的字符串
     * @return
     */
    public static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new sun.misc.BASE64Encoder().encode(out.toByteArray());
    }


    /**
     * 使用gzip解压缩
     *
     * @param compressedStr 压缩字符串
     * @return
     */
    public static String uncompress(String compressedStr) throws IOException {
        if (compressedStr == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = new BASE64Decoder().decodeBuffer(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        log.info("解密报文：{}", decompressed);
        return decompressed;
    }

    public static void main(String[] args) {
        String s1 = compress("<MFTMessage>\n" +
                "  <MFTHead>\n" +
                "    <PlatId>0</PlatId>\n" +
                "    <SignInfo>213V3Gquui3xi3VmqV5V2K2uKuVu13q4VK513u43OSVq04VxxiGG5xOVqi24K41i</SignInfo>\n" +
                "    <CopCode>MA22300W0</CopCode>\n" +
                "    <Note1>1</Note1>\n" +
                "    <Note2 />\n" +
                "  </MFTHead>\n" +
                "  <MFTData>\n" +
                "    <Manifest>\n" +
                "      <Head>\n" +
                "        <MessageID>2327MA22300W0_20230310155601807</MessageID>\n" +
                "        <FunctionCode>2</FunctionCode>\n" +
                "        <MessageType>MT3101</MessageType>\n" +
                "        <SenderID>2327MA22300W0_NJ0010002</SenderID>\n" +
                "        <ReceiverID>2327</ReceiverID>\n" +
                "        <SendTime>20230310155601668</SendTime>\n" +
                "        <Version>1.0</Version>\n" +
                "      </Head>\n" +
                "      <Declaration>\n" +
                "        <DeclarationOfficeID>2327</DeclarationOfficeID>\n" +
                "        <BorderTransportMeans>\n" +
                "          <JourneyID>1438W</JourneyID>\n" +
                "          <TypeCode>1</TypeCode>\n" +
                "          <ID>UN9346524</ID>\n" +
                "          <Name>SHABGOUN</Name>\n" +
                "        </BorderTransportMeans>\n" +
                "        <UnloadingLocation>\n" +
                "          <ID>CNTAC230030/2327</ID>\n" +
                "          <ArrivalDate>20230310</ArrivalDate>\n" +
                "        </UnloadingLocation>\n" +
                "        <TransportEquipment>\n" +
                "          <EquipmentIdentification>\n" +
                "            <ID>CICU3789922</ID>\n" +
                "          </EquipmentIdentification>\n" +
                "          <CharacteristicCode>20GP</CharacteristicCode>\n" +
                "          <FullnessCode>5</FullnessCode>\n" +
                "        </TransportEquipment>\n" +
                "      </Declaration>\n" +
                "    </Manifest>\n" +
                "  </MFTData>\n" +
                "</MFTMessage>");
        System.out.println(s1);
        try {
            System.out.println(uncompress("H4sIAAAAAAAAALOxr8jNUShLLSrOzM+zVTLUM1BSSM1Lzk/JzEu3VQoNcdO1ULK347LxdQvxTS0u\n" +
                    "TkxPteNSUABxPVITU0BsIC8gJ7HEM8UuxNk1wD8oxEYfyodIOucXOOenpNr5OhoZmQUGuRrZ6MOE\n" +
                    "QCbpIxkFMtYlsSQRovP9/r2Z+VVAUgmIYUoh0mAmzD0AtEdigsMAAAA="));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
