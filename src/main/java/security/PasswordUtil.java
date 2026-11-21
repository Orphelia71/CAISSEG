package security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashIfNeeded(String value) {

        if (value == null || value.isEmpty()) {
            return value;
        }

        // Évite de hacher une deuxième fois un mot de passe
        // déjà enregistré sous forme SHA-256 hexadécimale.
        if (value.matches("(?i)[0-9a-f]{64}")) {
            return value.toLowerCase();
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "Impossible de calculer le SHA-256", e
            );
        }
    }
}