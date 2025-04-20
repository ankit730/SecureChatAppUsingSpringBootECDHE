package com.ankitPersonalProject.chatAppECDHE.utils;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;

import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDHE {

    private static final String EC_ALGORITHM = "EC"; // Elliptic Curve algorithm
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int GCM_IV_LENGTH = 12; // 12 bytes is recommended for GCM

    private KeyPair keyPair;              // our private/public ephemeral key pair
    private SecretKey aesKey;             // derived AES key from ECDHE
    private final SecureRandom secureRandom = new SecureRandom(); // for IV generation

    // Generate a fresh ECDHE key pair
    public void generateEphemeralKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(EC_ALGORITHM);
        keyGen.initialize(256); // using 256-bit curve
        this.keyPair = keyGen.generateKeyPair();
    }

    // Get our encoded public key to send to peer (Base64 for transmission)
    public String getEncodedPublicKey() {
        byte[] encoded = keyPair.getPublic().getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    // Receive peer’s Base64 public key, decode, derive shared secret and AES key
    public void generateSharedSecret(String peerEncodedPublicKey) throws Exception {
        byte[] peerBytes = Base64.getDecoder().decode(peerEncodedPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
        PublicKey peerPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(peerBytes));

        KeyAgreement keyAgree = KeyAgreement.getInstance("ECDH");
        keyAgree.init(keyPair.getPrivate());
        keyAgree.doPhase(peerPublicKey, true);

        byte[] sharedSecret = keyAgree.generateSecret();

        // Derive AES key using SHA-256 (get first 16 bytes for AES-128)
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hashedSecret = sha256.digest(sharedSecret);
        byte[] aesKeyBytes = new byte[16];
        System.arraycopy(hashedSecret, 0, aesKeyBytes, 0, 16);
        this.aesKey = new SecretKeySpec(aesKeyBytes, "AES");
    }

    // Encrypt a message using AES-GCM
    public String encrypt(String message) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv); // generate unique IV

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

        byte[] ciphertext = cipher.doFinal(message.getBytes());

        // Combine IV + ciphertext into a single message
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        buffer.put(iv);
        buffer.put(ciphertext);
        return Base64.getEncoder().encodeToString(buffer.array());
    }

    // Decrypt a message using AES-GCM
    public String decrypt(String base64Message) throws Exception {
        byte[] messageBytes = Base64.getDecoder().decode(base64Message);

        ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext);
    }
}

