package com.data.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author: ps
 * @Description:
 * @Date: Created in 2021/11/18 21:23
 * @params:
 * @return:
 */
public class AESUtil {

    /**
     * 对需要加密的内容进行GZIP压缩后再进行 AES 加密操作
     *
     * @param content
     * @param password
     * @return
     * @throws Exception
     */
    public static String gzipEncrypt(String content, String password) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        gzipOutputStream.write(content.getBytes("UTF-8"));
        gzipOutputStream.close();
        outputStream.flush();
        byte[] bytes = outputStream.toByteArray();
        outputStream.close();
        String gzipContent = DatatypeConverter.printBase64Binary(bytes);
        return encrypt(gzipContent, password);
    }

    /**
     * AES 加密操作
     *
     * @param content  待加密内容
     * @param password 密码
     * @return 返回Base64转码后的加密数据
     * @throws Exception
     */
    public static String encrypt(String content, String password) throws Exception {
        return manageContent(content, password, Cipher.ENCRYPT_MODE);
    }

    /**
     * AES 解密操作 对解密结果进行GZIP解压缩
     *
     * @param content
     * @param password
     * @return
     * @throws Exception
     */
    public static String gzipDecrypt(String content, String password) throws Exception {
        String decrypt = decrypt(content, password);
        byte[] gzipBytes = DatatypeConverter.parseBase64Binary(decrypt);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(gzipBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        int count;
        byte data[] = new byte[1024];
        while ((count = gzipInputStream.read(data, 0, 1024)) != -1) {
            outputStream.write(data, 0, count);
        }
        gzipInputStream.close();
        inputStream.close();
        outputStream.flush();
        byte[] bytes = outputStream.toByteArray();
        outputStream.close();
        return new String(bytes, "UTF-8");
    }

    /**
     * AES 解密操作
     *
     * @param content  待解密内容
     * @param password 密码
     * @return 返回解密后的数据
     * @throws Exception
     */
    public static String decrypt(String content, String password) throws Exception {
        return manageContent(content, password, Cipher.DECRYPT_MODE);
    }

    /**
     * 加密解密公共方法
     *
     * @param content    内容
     * @param cipherType 加密 or 解密(支持的值: Cipher.ENCRYPT_MODE、Cipher.DECRYPT_MODE)
     * @return
     * @throws Exception
     */
    private static String manageContent(String content, String password, int cipherType) throws Exception {
        byte[] contBytes;
        if (cipherType == Cipher.DECRYPT_MODE) {
            contBytes = DatatypeConverter.parseBase64Binary(content);
        } else {
            contBytes = content.getBytes("UTF-8");
        }
        //实例化
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        //使用密钥初始化，设置模式
        cipher.init(cipherType, getSecretKey(password.getBytes("UTF-8")));
        //操作
        byte[] cipherBytes = cipher.doFinal(contBytes);
        if (cipherType == Cipher.DECRYPT_MODE) {
            return new String(cipherBytes, "UTF-8");
        } else {
            return DatatypeConverter.printBase64Binary(cipherBytes);
        }
    }


    /**
     * 生成加密秘钥
     *
     * @return 秘钥对象
     */
    public static SecretKeySpec getSecretKey(byte[] pwd) throws Exception {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(pwd);
        //AES 要求密钥长度为 128
        kg.init(128, random);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        // 转换为AES专用密钥
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    private static byte[] base64Decode(String base64Code) {
        return base64Code.isEmpty() ? null : Base64.decodeBase64(base64Code.getBytes(Charset.forName("UTF-8")));
    }


    /**
     * 测试入口
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        testEncrypt();
//        testGzipEncrypt();
//        String data = "你好好好好好";
//        String ss = AESUtil.decrypt(data,"DcitsMFT");
//        System.out.println(ss);
    }

    /**
     * 测试加密内容和解密被加密的内容
     */
    private static void testEncrypt() throws Exception {
        //String srcStr = "hello, 这是测试";
        //会员反馈
        //String reqBody = "{\"plat_user\": \"单一窗口用户编号\",\"org_code\": \"企业组织机构代码\",\"union_org_code\": \"统一社会信用代码\",\"result\": \"结果状态\",\"resp_memo\": \"备注\"}";

        //订单状态更新
        //String reqBody = "{\"order_no\": \"订单编号\", \"bank_biz_no\": \"银行系统业务号\", \"ret_date\": \"反馈信息时间\", \"ret_code\": \"反馈码\", \"resp_memo\": \"备注\"}";
        //新增会员
        String reqBody = "9$9999999999";

        String password = "bcbfc2d2260f4b7eaf7fb51a1f0abbb6";
        System.out.println("rspBody加密前:" + reqBody);
        String reqStr = encrypt(reqBody, password);
        System.out.println("rspBody加密后:" + reqStr);
        System.out.println("rspBody解密后:" + decrypt(reqStr, password));

    }

    /**
     * 测试加密内容和解密被加密的内容
     */
    private static void testGzipEncrypt() throws Exception {
        //String srcStr = "hello, 这是测试";
        //会员反馈
        //String reqBody = "{\"plat_user\": \"单一窗口用户编号\",\"org_code\": \"企业组织机构代码\",\"union_org_code\": \"统一社会信用代码\",\"result\": \"结果状态\",\"resp_memo\": \"备注\"}";

        //订单状态更新
        String reqBody = "{\"order_no\": \"订单编号\", \"bank_biz_no\": \"银行系统业务号\", \"ret_date\": \"反馈信息时间\", \"ret_code\": \"反馈码\", \"resp_memo\": \"备注\"}";

        String rspBody = "{\"resp_code\": \"001\",\"resp_memo\": \"测试\"}";
        String password = "bcbfc2d2260f4b7eaf7fb51a1f0abbb6";
        System.out.println("rspBody加密前gzip:" + reqBody);
        String reqStr = gzipEncrypt(reqBody, password);
        System.out.println("rspBody加密后gzip:" + reqStr);
        System.out.println("rspBody解密后gzip:" + gzipDecrypt(reqStr, password));
        System.out.println("*****************************************************************************");
        System.out.println("rspBody加密前gzip:" + rspBody);
        String rspStr = gzipEncrypt(rspBody, password);
        System.out.println("rspBody加密后gzip:" + rspStr);
        System.out.println("rspBody解密后gzip:" + gzipDecrypt(rspStr, password));


    }
}
