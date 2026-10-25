package com.bughunters.pos.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public final class TotpUtil {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int DIGITS_POWER = 1000000;

    private TotpUtil() {}

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    public static String getQrCodeUri(String secret, String accountName, String issuer) {
        String encodedAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                encodedIssuer, encodedAccount, secret, encodedIssuer);
    }

    public static boolean verifyCode(String secret, String codeStr) {
        if (secret == null || codeStr == null || !codeStr.matches("\\d{6}")) {
            return false;
        }

        try {
            int code = Integer.parseInt(codeStr);
            byte[] key = decodeBase32(secret);
            long currentInterval = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;

            // Check current window and +/- 1 window (tolerates ±30s clock drift)
            for (int i = -1; i <= 1; i++) {
                long hash = generateCode(key, currentInterval + i);
                if (hash == code) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static int generateCode(byte[] key, long timeInterval) throws GeneralSecurityException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timeInterval);
        byte[] data = buffer.array();

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "RAW"));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        return binary % DIGITS_POWER;
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt(index));
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            result.append(BASE32_CHARS.charAt(index));
        }
        return result.toString();
    }

    private static byte[] decodeBase32(String base32) {
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int numBytes = clean.length() * 5 / 8;
        byte[] result = new byte[numBytes];
        int buffer = 0;
        int bitsLeft = 0;
        int byteIndex = 0;

        for (int i = 0; i < clean.length(); i++) {
            int val = BASE32_CHARS.indexOf(clean.charAt(i));
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (byteIndex < numBytes) {
                    result[byteIndex++] = (byte) ((buffer >> bitsLeft) & 0xFF);
                }
            }
        }
        return result;
    }
}
