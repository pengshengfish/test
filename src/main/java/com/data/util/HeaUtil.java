package com.data.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HeaUtil {

    /**
     * md5加密
     *
     * @param text 内容
     * @return digest 摘要
     * @throws NoSuchAlgorithmException e
     */
    public static String md5(String text) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] bytes = messageDigest.digest(text.getBytes());
        return Hex.encodeHexString(bytes);
    }

    /**
     * sha1加密
     *
     * @param text 内容
     * @return digest 摘要
     * @throws NoSuchAlgorithmException e
     */
    public static String sha1(String text) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = messageDigest.digest(text.getBytes());
        return Hex.encodeHexString(bytes);
    }

    /**
     * sha256加密
     *
     * @param text 内容
     * @return digest 摘要
     * @throws NoSuchAlgorithmException e
     */
    public static String sha256(String text) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = messageDigest.digest(text.getBytes());
        return Hex.encodeHexString(bytes);
    }

    /**
     * hmac-sha1加密
     *
     * @param text 内容
     * @param key  密钥
     * @return 密文
     * @throws Exception e
     */
    public static String hmacSha1(String text, String key) throws Exception {
        SecretKeySpec sk = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        return hmacSha1(text, sk);
    }

    /**
     * hmac-sha1加密
     *
     * @param text 内容
     * @param sk   密钥
     * @return 密文
     * @throws Exception e
     */
    public static String hmacSha1(String text, SecretKeySpec sk) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(sk);
        byte[] rawHmac = mac.doFinal(text.getBytes());
        return new String(Base64.encodeBase64(rawHmac));
    }

    /**
     * 生成 HmacSha1 密钥
     *
     * @param key 密钥字符串
     * @return SecretKeySpec
     */
    public static SecretKeySpec createHmacSha1Key(String key) {
        return new SecretKeySpec(key.getBytes(), "HmacSHA1");
    }

    /**
     * hmac-sha256加密
     *
     * @param text 内容
     * @param key  密钥
     * @return 密文
     * @throws Exception e
     */
    public static String hmacSha256(String text, String key) throws Exception {
        SecretKeySpec sk = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        return hmacSha1(text, sk);
    }

    /**
     * hmac-sha256加密
     *
     * @param text 内容
     * @param sk   密钥
     * @return 密文
     * @throws Exception e
     */
    public static String hmacSha256(String text, SecretKeySpec sk) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(sk);
        byte[] rawHmac = mac.doFinal(text.getBytes());
        return new String(Base64.encodeBase64(rawHmac));
    }

    /**
     * 生成 HmacSha256 密钥
     *
     * @param key 密钥字符串
     * @return SecretKeySpec
     */
    public static SecretKeySpec createHmacSha256Key(String key) {
        return new SecretKeySpec(key.getBytes(), "HmacSHA256");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String ss = "jdbc\\:oracle\\:thin\\:@//172.16.1.250\\:1521/pdbdxzbqfb.dxzb.com";
        System.out.println(HeaUtil.sha256(ss));

    }

}
