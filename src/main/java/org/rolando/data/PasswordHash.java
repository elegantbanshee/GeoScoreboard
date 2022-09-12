package org.rolando.data;

public class PasswordHash {
    public final String hash;
    public final String salt;

    public PasswordHash(String hash, String salt) {
        this.hash = hash;
        this.salt = salt;
    }
}
