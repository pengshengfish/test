package com.data.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * @Author:chenxueqing
 * @Description:
 * @Time:2022/9/26 11:18
 */
public class Sm4Util {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ENCODING = "UTF-8";
    public static final String ALGORITHM_NAME = "SM4";
    // 加密算法/分组加密模式/分组填充方式
    // PKCS5Padding-以8个字节为一组进行分组加密
    // 定义分组加密模式使用：PKCS5Padding
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    // 128-32位16进制；256-64位16进制
    public static final int DEFAULT_KEY_SIZE = 128;

    /**
     * 自动生成密钥
     *
     * @return
     * @explain
     */
    /*public static String generateKey() throws Exception {
        return new String(Hex.encodeHex(generateKey(DEFAULT_KEY_SIZE),false));
    }*/

    /**
     * @param keySize
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] generateKey(int keySize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    /**
     * 生成ECB暗号
     *
     * @param algorithmName 算法名称
     * @param mode          模式
     * @param key
     * @return
     * @throws Exception
     * @explain ECB模式（电子密码本模式：Electronic codebook）
     */
    private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }

    /**
     * sm4加密
     *
     * @param hexKey   16进制密钥（忽略大小写）
     * @param paramStr 待加密字符串
     * @return 返回16进制的加密字符串
     * @explain 加密模式：ECB
     * 密文长度不固定，会随着被加密字符串长度的变化而变化
     */
    public static String encryptEcb(String hexKey, String paramStr) {
        try {
            String cipherText = "";
            // 16进制字符串--&gt;byte[]
            byte[] keyData = ByteUtils.fromHexString(hexKey);
            // String--&gt;byte[]
            byte[] srcData = paramStr.getBytes(ENCODING);
            // 加密后的数组
            byte[] cipherArray = encrypt_Ecb_Padding(keyData, srcData);
            // byte[]--&gt;hexString
            cipherText = ByteUtils.toHexString(cipherArray);
            return cipherText;
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * 加密模式之Ecb
     *
     * @param key
     * @param data
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] encrypt_Ecb_Padding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * sm4解密
     *
     * @param hexKey     16进制密钥
     * @param cipherText 16进制的加密字符串（忽略大小写）
     * @return 解密后的字符串
     * @throws Exception
     * @explain 解密模式：采用ECB
     */
    public static String decryptEcb(String hexKey, String cipherText) {
        // 用于接收解密后的字符串
        String decryptStr = "";
        // hexString--&gt;byte[]
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        // hexString--&gt;byte[]
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        // 解密
        byte[] srcData = new byte[0];
        try {
            srcData = decrypt_Ecb_Padding(keyData, cipherData);
            // byte[]--&gt;String
            decryptStr = new String(srcData, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return decryptStr;
    }

    /**
     * 解密
     *
     * @param key
     * @param cipherText
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] decrypt_Ecb_Padding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    /**
     * 校验加密前后的字符串是否为同一数据
     *
     * @param hexKey     16进制密钥（忽略大小写）
     * @param cipherText 16进制加密后的字符串
     * @param paramStr   加密前的字符串
     * @return 是否为同一数据
     * @throws Exception
     * @explain
     */
    public static boolean verifyEcb(String hexKey, String cipherText, String paramStr) throws Exception {
        // 用于接收校验结果
        boolean flag = false;
        // hexString--&gt;byte[]
        byte[] keyData = ByteUtils.fromHexString(hexKey);
        // 将16进制字符串转换成数组
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        // 解密
        byte[] decryptData = decrypt_Ecb_Padding(keyData, cipherData);
        // 将原字符串转换成byte[]
        byte[] srcData = paramStr.getBytes(ENCODING);
        // 判断2个数组是否一致
        flag = Arrays.equals(decryptData, srcData);
        return flag;
    }

    public static void main(String[] args) {
        String key = "3A8494AA66C508E9EE8C147E7693EAD1";
        String ss = "7b37139829ee213a088f4efed23d716357d915b8e572eec2e37dc6775277e40b762b0385907251efaaa9bb3689b75a628ee7c5db5f635236940648ae332b1b7ef0ac64a5d839badc0a27ff754eef1faef48c5431230ff400e0d4b2e226c7905545fc48584434c0a9a5689700a99a69481bfe84c5c20596a28cc46378a4d2a2589429bfaff060e81412a44d634764edaf589d6a279dc364f0beae232afe8bf2dce678a99b92e8247cb0a70433d934e87bc495710f39d6c6c730a6c6d6097afbfcd398ac6563d4165bdbcdaeca349cde036f561ae92399330493e5b3a6dbe3d763f48c5431230ff400e0d4b2e226c79055185a2b4d8a3d61c27a7c782a5f64afa4f850a38eec4eaf990d6c89c7e12e753e4b599184120fb91b6f8793d6109e7d87d9f09bf0b6cd1501ea54a5dffd7eebc85225c76b7cb24a13113ee152cc4740f063cb4776e8303f42bc1f56877e6494783971614c83910551deea1765a5e2dc0455aa92890ee9bc4cb49171190b80a87a99cb3e5606d903f751772377ea2ddf3a6fa5f16b64e721230c51023189f750d123927cbb3a1e987eda0e26d9d5d16ec97031c8bc244e590d9c1e3687a5fb6b6be042dd9de47cccabb51843b8e79b34bdbd650fc5e1c3b8a9644852255cf0d440e6b77cc6d1e127317fa6c987d1c54c5efb57f677ba5099ef2e7fcab388f3130d09edd35a51e1d567af7590cdafe15af2cbd4b8b9a92dd461ea71b3356c8b0788b1b63157252957856a25b25037941638bfa6fa80e32a3de78935cb96a4eac496a30c8206ce51749eeeb70a740ad740636607b10b1ba6b498dbf139df69fed95ef6ee7b4e2b2cd8bd044bcabf9030c7b2a5fa0b8fb7339300c7028cad811c75feb966182bbc3dff657174a8f48a5cbeb16072e24d6adc112c18d2a65684730d83ce00cbe49b0ffd01b858e8a0227f7f9cb83beae5e89c05a6408cd46462ac4c989786e6984c267e6fa662692a8ba613bb";
        try {
            /*String enStr = Sm4Util.encryptEcb(key, cipher);
            System.out.println("加密原始数据:"+cipher);
            System.out.println("SM4加密key:"+key);
            System.out.println("SM4加密后:"+enStr);
            System.out.println("============================");*/
            String json = Sm4Util.decryptEcb(key, ss);
            /*System.out.println("解密数据:"+enStr);
            System.out.println("SM4加密key:"+key);*/
            System.out.println("SM4解密后:" + json);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
