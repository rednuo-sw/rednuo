package com.rednuo.core.utils;

import com.rednuo.core.config.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rednuo 2021/4/28
 */
@Slf4j
public class Encryptor {
//
//    public static String encode(String input){
//        try{
//            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//            return passwordEncoder.encode(input);
//        }catch (Exception e){
//            log.error("加密出错:"+input, e);
//            return input;
//        }
//    }
//    public static boolean matches(String input, String hashPass){
//        try{
//            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//            return passwordEncoder.matches(input, hashPass);
//        }catch (Exception e){
//            log.error("解密出错:"+input, e);
//            return false;
//        }
//    }

    /**
     * 算法
     */
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5PADDING";

    private static final String KEY_FILL = "abcdefghijklmnop";

    /**
     * 加密Cipher缓存
     */
    private static Map<String, Cipher> encryptorMap = new ConcurrentHashMap<>();
    /**
     * 解密Cipher缓存
     */
    private static Map<String, Cipher> decryptorMap = new ConcurrentHashMap<>();

    /**
     * 加密字符串（可指定加密密钥）
     * @param input 待加密文本
     * @param key 密钥（可选）
     * @return 结果
     */
    public static String encrypt(String input, String... key){
        String seedKey = V.notEmpty(key)? key[0] : getDefaultKey();
        try{
            Cipher cipher = getEncryptor(seedKey);
            byte[] enBytes = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(enBytes);
        }
        catch(Exception e){
            log.error("加密出错:"+input, e);
            return input;
        }
    }

    /**
     * 解密字符串
     * @param input 待解密文本
     * @param key 加密key（可选）
     * @return 结果
     */
    public static String decrypt(String input, String... key){
        if(V.isEmpty(input)){
            return input;
        }
        String seedKey = V.notEmpty(key)? key[0] : getDefaultKey();
        try{
            Cipher cipher = getDecryptor(seedKey);
            byte[] deBytes = Base64.getDecoder().decode(input.getBytes());
            return new String(cipher.doFinal(deBytes));
        }
        catch(Exception e){
            log.error("解密出错:"+input, e);
            return input;
        }
    }

    /***
     * 获取指定key的加密器
     * @param key 加密密钥
     * @return 结果
     * @throws Exception e
     */
    private static Cipher getEncryptor(String key) throws Exception{
        byte[] keyBytes = getKey(key);
        Cipher encryptor = encryptorMap.get(new String(keyBytes));
        if(encryptor == null){
            SecretKeySpec skeyspec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            encryptor = Cipher.getInstance(CIPHER_ALGORITHM);
            encryptor.init(Cipher.ENCRYPT_MODE, skeyspec);
            // 放入缓存
            encryptorMap.put(key, encryptor);
        }
        return encryptor;
    }

    /***
     * 获取指定key的解密器
     * @param key 解密密钥
     * @return 结果
     * @throws Exception e
     */
    private static Cipher getDecryptor(String key) throws Exception{
        byte[] keyBytes = getKey(key);
        Cipher decryptor = encryptorMap.get(new String(keyBytes));
        if(decryptor == null){
            SecretKeySpec skeyspec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
            decryptor = Cipher.getInstance(CIPHER_ALGORITHM);
            decryptor.init(Cipher.DECRYPT_MODE, skeyspec);
            // 放入缓存
            decryptorMap.put(key, decryptor);
        }
        return decryptor;
    }

    /***
     * 获取key，如非16位则调整为16位
     * @param seed
     * @return 结果
     */
    private static byte[] getKey(String seed){
        if(V.isEmpty(seed)){
            seed = getDefaultKey();
        }
        if(seed.length() < 16){
            seed = seed + S.cut(KEY_FILL, 16-seed.length());
        }
        else if(seed.length() > 16){
            seed = S.cut(seed, 16);
        }
        return seed.getBytes();
    }

    /**
     * 默认加密seed（可通过配置文件）
     */
    private static String getDefaultKey(){
        String defaultKey = BaseConfig.getProperty("rednuo.encryptor.seed");
        return V.notEmpty(defaultKey)? defaultKey : "RednuoV1";
    }
}
