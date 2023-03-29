package com.data.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author:chenxueqing
 * @Description: RSA 工具类
 * @Time:2022/7/5 9:12
 */
@Slf4j
public class RSAUtil {

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static final int RSA_SIZE_1024 = 1024;

    private static final String ALGORITHM = "SHA1WithRSA";
    /**
     * 非对称密钥算法
     */
    private static final String KEY_ALGORITHM = "RSA";

    //字符编码
    private static final String CHARSET = "UTF-8";

    /**
     * 生成 RSA 密钥对
     *
     * @param keySize
     * @return: {@link Map<String,Object> }
     * @author: Andy
     * @time: 2019/5/10 16:59
     */
    public static Map<String, Object> createKeyPair(int keySize) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            log.error("初始化密钥工具异常", e);
            return null;
        }
        keyGen.initialize(keySize, new SecureRandom());
        KeyPair key = keyGen.generateKeyPair();
        PublicKey publicKey = key.getPublic();
        PrivateKey privateKey = key.getPrivate();
        Map map = new HashMap();
        map.put("publicKey", publicKey);
        map.put("privateKey", privateKey);
        map.put("publicKeyBase64", Base64.encodeBase64String(publicKey.getEncoded()));
        map.put("privateKeyBase64", Base64.encodeBase64String(privateKey.getEncoded()));
        return map;
    }

    /**
     * 获得公钥的 Base64 字符串
     *
     * @param publicKey 公钥
     * @return: {@link String }
     * @author: Andy
     * @time: 2019/5/10 17:11
     */
    public static String getBase64PublicKeyString(PublicKey publicKey) {
        return Base64.encodeBase64URLSafeString(publicKey.getEncoded()).trim();
    }

    /**
     * 获得私钥的 Base64 字符串
     *
     * @param privateKey 公钥
     * @return: {@link String }
     * @author: Andy
     * @time: 2019/5/10 17:11
     */
    public static String getBase64PrivateKeyString(PrivateKey privateKey) {
        return Base64.encodeBase64URLSafeString(privateKey.getEncoded()).trim();
    }

    /**
     * 获取公钥
     *
     * @param publicKeyBase64 公钥的 Base64 字符串
     * @return: {@link PublicKey }
     * @author: Andy
     * @time: 2019/5/10 18:05
     */
    public static PublicKey getPublicKey(String publicKeyBase64)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyBase64));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
        return publicKey;
    }

    /**
     * 获取私钥
     *
     * @param privateKeyBase64 私钥的 Base64 字符串
     * @return: {@link PrivateKey }
     * @author: Andy
     * @time: 2019/5/10 18:05
     */
    public static PrivateKey getPrivateKey(String privateKeyBase64)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyBase64));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);
        return priKey;
    }

    /**
     * 使用私钥对数据进行数字签名
     *
     * @param data       需要签名的数据
     * @param privateKey 私钥
     * @return: {@link byte[] }
     * @author: Andy
     * @time: 2019/5/10 17:15
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * 使用私钥对数据进行数字签名
     *
     * @param data       需要签名的字符串
     * @param privateKey 私钥
     * @return: {@link String }
     * @author: Andy
     * @time: 2019/5/10 17:15
     */
    public static String sign(String data, PrivateKey privateKey)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        return Base64.encodeBase64URLSafeString(sign(data.getBytes(), privateKey)).trim();
    }

    /**
     * 签名校验
     *
     * @param data      参与签名的数据
     * @param sign      数字签名
     * @param publicKey 公钥
     * @return: {@link boolean }
     * @author: Andy
     * @time: 2019/5/10 17:22
     */
    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);
    }

    /**
     * 签名校验
     *
     * @param data      参与签名的数据
     * @param sign      数字签名
     * @param publicKey 公钥
     * @return: {@link boolean }
     * @author: Andy
     * @time: 2019/5/10 17:22
     */
    public static boolean verify(String data, String sign, PublicKey publicKey)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        return verify(data.getBytes(), Base64.decodeBase64(sign), publicKey);
    }

    /**
     * 获取参与签名的参数的字符串。参数拼接的顺序由 TreeMap 决定。
     *
     * @param paramsMap 参与签名的参数名和参数值的映射
     * @return: {@link String }
     * @author: Andy
     * @time: 2019/5/10 17:43
     */
    public static String getSourceSignData(TreeMap<String, String> paramsMap) {
        StringBuilder paramsBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> paramEntry : paramsMap.entrySet()) {
            if (!first) {
                paramsBuilder.append("&");
            } else {
                first = false;
            }

            paramsBuilder.append(paramEntry.getKey()).append("=").append(paramEntry.getValue());
        }

        return paramsBuilder.toString();
    }

    /**
     * 私钥加密
     *
     * @param data       待加密数据
     * @param privateKey 私钥字节数组
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生成私钥
        PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        //数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * 私钥加密
     *
     * @param data       待加密数据
     * @param privateKey Base64编码的私钥
     * @return String Base64编码的加密数据
     */
    public static String encryptByPrivateKey(String data, String privateKey) throws Exception {
        byte[] key = Base64.decodeBase64(privateKey);
        return Base64.encodeBase64String(encryptByPrivateKey(data.getBytes(CHARSET), key));
    }

    /**
     * 公钥解密
     *
     * @param data      待解密数据
     * @param publicKey 公钥字节数组
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //产生公钥
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        //数据解密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * 公钥解密
     *
     * @param data      Base64编码的待解密数据
     * @param publicKey Base64编码的公钥
     * @return String 解密数据
     */
    public static String decryptByPublicKey(String data, String publicKey) throws Exception {
        byte[] key = Base64.decodeBase64(publicKey);
        return new String(decryptByPublicKey(Base64.decodeBase64(data), key), CHARSET);
    }

    /**
     * 私钥解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥字节数组
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] privateKey) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生成私钥
        PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        //数据解密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * 私钥解密
     *
     * @param data       Base64编码的待解密数据
     * @param privateKey Base64编码的私钥
     * @return String 解密数据
     */
    public static String decryptByPrivateKey(String data, String privateKey) throws Exception {
        byte[] key = Base64.decodeBase64(privateKey);
        return new String(decryptByPrivateKey(Base64.decodeBase64(data), key), CHARSET);
    }

    /**
     * 公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥字节数组
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生成公钥
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        //数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * 公钥加密
     *
     * @param data      待加密数据
     * @param publicKey Base64编码的公钥
     * @return String Base64编码的加密数据
     */
    public static String encryptByPublicKey(String data, String publicKey) throws Exception {
        byte[] key = Base64.decodeBase64(publicKey);
        return Base64.encodeBase64String(encryptByPublicKey(data.getBytes(CHARSET), key));
    }

    /**
     * 设计流程：
     * <p>
     * 1、创建密钥对
     * 2、获取参与签名的数据
     * 3、获取参与签名的数据的摘要（MD5值）
     * 4、使用私钥对摘要进行数字签名
     * 5、使用公钥验证签名
     *
     * @author: Andy
     * @time: 2019/5/10 17:32
     */
    public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //1、创建密钥对
        Map<String, Object> keyPairMap = createKeyPair(RSA_SIZE_1024);
        String publicKeyBase64 = keyPairMap.get("publicKeyBase64").toString();
        String privateKeyBase64 = keyPairMap.get("privateKeyBase64").toString();
        System.out.println(String.format("publicKeyBase64: %s", publicKeyBase64));
        System.out.println(String.format("privateKeyBase64: %s", privateKeyBase64));

    }


    /**
     * RSA分段公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String publicKeyEncrypt(String str, String publicKey, String point) throws Exception {
        log.info("{}|RSA公钥加密前的数据|str:{}|publicKey:{}", point, str, publicKey);
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").
                generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        //当长度过长的时候，需要分割后加密 117个字节
        byte[] resultBytes = getMaxResultEncrypt(str, point, cipher);

        String outStr = Base64.encodeBase64String(resultBytes);
        log.info("{}|公钥加密后的数据|outStr:{}", point, outStr);
        return outStr;
    }

    private static byte[] getMaxResultEncrypt(String str, String point, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        byte[] inputArray = str.getBytes();
        int inputLength = inputArray.length;
        log.info("{}|加密字节数|inputLength:{}", point, inputLength);
        // 最大加密字节数，超出最大字节数需要分组加密
        int MAX_ENCRYPT_BLOCK = 117;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                offSet += MAX_ENCRYPT_BLOCK;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        return resultBytes;
    }


    /**
     * RSA分段私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @param point
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String privateKeyDecrypt(String str, String privateKey, String point) throws Exception {
        log.info("{}|RSA私钥解密前的数据|str:{}|privateKey:{}", point, str, privateKey);
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
//        String outStr = new String(cipher.doFinal(inputByte));
        //当长度过长的时候，需要分割后解密 128个字节
        String outStr = new String(getMaxResultDecrypt(str, point, cipher));
//        log.info("{}|RSA私钥解密后的数据|outStr:{}", point, outStr);
        return outStr;
    }

    private static byte[] getMaxResultDecrypt(String str, String point, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        byte[] inputArray = Base64.decodeBase64(str.getBytes("UTF-8"));
        int inputLength = inputArray.length;
        log.info("{}|解密字节数|inputLength:{}", point, inputLength);
        // 最大解密字节数，超出最大字节数需要分组加密
        int MAX_ENCRYPT_BLOCK = 128;
        // 标识
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                offSet += MAX_ENCRYPT_BLOCK;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        return resultBytes;
    }


}
