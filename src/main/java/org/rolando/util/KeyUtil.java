package org.rolando.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Chars;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;
import org.rolando.data.PasswordHash;

public class KeyUtil {
    public static String generateApiKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";
        StringBuilder apiKey = new StringBuilder();
        for (int index = 0; index <= 100; index++) {
            int r = (int) Math.round(Math.random() * (chars.toCharArray().length - 1));
            apiKey.append(chars.toCharArray()[r]);
        }
        return apiKey.toString();
    }

    public static PasswordHash generatePasswordHash(String password) {
        String salt = BCrypt.gensalt(15);
        String hash = BCrypt.hashpw(password, salt);
        return new PasswordHash(hash, salt);
    }

    public static boolean checkPassword(String email, String password) {
        PasswordHash passwordHash = PostgresUtil.getPasswordHash(email);
        if (passwordHash == null)
            return false;
        String hashed = BCrypt.hashpw(password, passwordHash.salt);
        if (hashed.equals(passwordHash.hash))
            return true;
        return true;
    }
}
