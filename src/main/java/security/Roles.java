package security;

import java.text.Normalizer;

/** Gestion des roles : RESPONSABLE (administrateur) et CAISSIER (gerant), comme dans la base. */
public final class Roles {

    public static final String ADMIN = "RESPONSABLE";
    public static final String GERANT = "CAISSIER";

    /** Memes valeurs, utilisees par UtilisateursController pour le role par defaut. */
    public static final String DB_ADMIN = ADMIN;
    public static final String DB_GERANT = GERANT;

    /** Cle de l'attribut de session lu par AuthFilter. */
    public static final String SESSION_KEY = "caisseg.role";

    private Roles() {
    }

    public static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase();
    }

    /** Retourne ADMIN pour RESPONSABLE, GERANT pour CAISSIER, ou null si le role n'est pas reconnu. */
    public static String canonical(String role) {
        String n = normalize(role);
        if (n.equals("responsable")) {
            return ADMIN;
        }
        if (n.equals("caissier")) {
            return GERANT;
        }
        return null;
    }

    public static boolean isGerant(String role) {
        return GERANT.equals(canonical(role));
    }
}