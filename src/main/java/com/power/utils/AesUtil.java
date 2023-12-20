package com.power.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.power.common.constant.SystemInfo.DATA_SECRET_IV;
import static com.power.common.constant.SystemInfo.OPERATOR_SECRET_KEY;

/**
 * @author cyk
 * @date 2023/12
 * description Aes加密解密
 */
@Slf4j
public class AesUtil {

    /**
     * description Aes加密
     */
    public static String encrypt(String value) {
        try {
            if (StringUtils.isBlank(value)) {
                return "";
            }
            byte[] keyBytes = OPERATOR_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = DATA_SECRET_IV.getBytes(StandardCharsets.UTF_8);
            //Aes加密
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            int valueLength = valueBytes.length;
            if (valueLength % blockSize != 0) {
                valueLength = valueLength + (blockSize-(valueLength % blockSize));
            }
            byte[] valueText = new byte[valueLength];
            System.arraycopy(valueBytes,0,valueText,0,valueBytes.length);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes,"AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);
            byte[] encrypted = cipher.doFinal(valueText);
            return Base64.getEncoder().encodeToString(encrypted).trim();
        } catch (Exception e){
            return null;
        }
    }


    /**
     * description Aes解密
     */
    public static String decrypt(String value) {
        try {
            if (StringUtils.isBlank(value)) {
                return "";
            }
            byte[] encrypted1 = Base64.getDecoder().decode(value);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(OPERATOR_SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(DATA_SECRET_IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString.trim();
        } catch (Exception e){
            throw new RuntimeException();
        }
    }

}
