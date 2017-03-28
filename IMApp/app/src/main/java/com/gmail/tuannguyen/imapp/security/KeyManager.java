package com.gmail.tuannguyen.imapp.security;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import com.gmail.tuannguyen.imapp.util.CommonUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

/**
 * Created by tuannguyen on 4/17/16.
 */
public class KeyManager {
    KeyStore keyStore;
    ArrayList<String> keyAliases;
    Context context;

    public KeyManager(Context context) {
        this.context = context;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshKeyAliases();
    }

    private void refreshKeyAliases() {
        keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public void createNewKey(String alias) {
        try {
            if (!keyStore.containsAlias(alias)) {
                Date startDate = Calendar.getInstance().getTime();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                Date endDate = end.getTime();
                KeyPairGeneratorSpec keySpec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=IMAPP, O=IMAPP"))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build();
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                keyPairGenerator.initialize(keySpec);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        refreshKeyAliases();
    }

    public String encryptString(String alias, String plaintext) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            cipherOutputStream.write(plaintext.getBytes());
            cipherOutputStream.close();

            String encryptedString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            return encryptedString;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptString(String alias, String encryptedText) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);


            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encryptedText, Base64.DEFAULT)), cipher);
            /*
            ArrayList<Byte> byteArr = new ArrayList<>();
            int nextByte;

            while ( (nextByte = cipherInputStream.read()) >0 ) {
                byteArr.add((byte)nextByte);
            }
            byte[]  bytes = new byte[byteArr.size()];
            for (int i=0; i < byteArr.size() ; i++ ){
                bytes[i] = byteArr.get(i).byteValue();
            }
            */
            byte[] bytes = CommonUtil.readAllBytesFromInputStream(cipherInputStream);
            String decryptedString = new String(bytes, 0, bytes.length, "UTF-8");
            return decryptedString;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
