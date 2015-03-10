package com.mobike.mobike.utils;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Andrea-PC on 10/03/2015.
 */

public class Crypter {

    private byte[] key = "749b67ba749b67ba".getBytes();
    private byte[] iv = "7ebae6fa7ebae6fa".getBytes();
    private SecretKeySpec secretKey = new SecretKeySpec(this.key, "AES");
    private IvParameterSpec ivSpec = new IvParameterSpec(this.iv);

    public Crypter() {}

    public String encrypt(String plainText){
        try {
            Cipher cipher;
            byte[] plainTextByte = plainText.getBytes();
            byte[] encryptedByte = null;
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, this.ivSpec);
            encryptedByte = cipher.doFinal(plainTextByte);
            String encryptedText = Base64.encodeToString(encryptedByte, 0, encryptedByte.length, Base64.DEFAULT);
            return encryptedText;
        } catch (Exception e) {}
        return "";
    }

    public String decrypt(String encryptedText){
        try {
            Cipher cipher;
            byte[] encryptedTextByte = Base64.decode(encryptedText, Base64.DEFAULT);
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, this.secretKey, this.ivSpec);
            byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
            String decryptedText = new String(decryptedByte);
            return decryptedText;
        } catch (Exception e) {}
        return "";
    }



}
