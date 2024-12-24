package com.ktds.rcsp.message.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class EncryptionService {

   @Value("${encryption.key}")
   private String encryptionKey;

   private static final String ALGORITHM = "AES";

   public String encrypt(String data) {
       try {
           SecretKeySpec keySpec = new SecretKeySpec(
               encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
           Cipher cipher = Cipher.getInstance(ALGORITHM);
           cipher.init(Cipher.ENCRYPT_MODE, keySpec);
           
           byte[] encrypted = cipher.doFinal(data.getBytes());
           return Base64.getEncoder().encodeToString(encrypted);
       } catch (Exception e) {
           log.error("Encryption failed", e);
           throw new RuntimeException("Encryption failed", e);
       }
   }

   public String decrypt(String encryptedData) {
       try {
           SecretKeySpec keySpec = new SecretKeySpec(
               encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
           Cipher cipher = Cipher.getInstance(ALGORITHM);
           cipher.init(Cipher.DECRYPT_MODE, keySpec);
           
           byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
           return new String(decrypted);
       } catch (Exception e) {
           log.error("Decryption failed", e);
           throw new RuntimeException("Decryption failed", e);
       }
   }
}
