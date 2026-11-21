package security;

import entities.Utilisateurs;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;

/**
 * Utilisateur connecte. L'authentification est faite par WildFly (JAAS/Elytron, voir web.xml) :
 * ici on lit seulement le principal et les roles fournis par le serveur.
 */
@Named("authController")
@SessionScoped
public class AuthController implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf;

    private Utilisateurs user;

    private ExternalContext ec() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public String getLogin() {
        Principal p = ec().getUserPrincipal();
        return p == null ? null : p.getName();
    }

    public boolean isLoggedIn() {
        return getLogin() != null;
    }

    public boolean isAdmin() {
        return ec().isUserInRole(Roles.ADMIN);
    }

    public boolean isGerant() {
        return ec().isUserInRole(Roles.GERANT);
    }

    /** Entite Utilisateurs de la personne connectee (chargee une fois par session). */
    public Utilisateurs getUser() {
        String login = getLogin();
        if (login == null) {
            return null;
        }
        if (user == null || !login.equals(user.getLogin())) {
            EntityManager em = emf.createEntityManager();
            try {
                List<Utilisateurs> l = em.createQuery("SELECT u FROM Utilisateurs u WHERE u.login = :l", Utilisateurs.class)
                        .setParameter("l", login).getResultList();
                user = l.isEmpty() ? null : l.get(0);
            } finally {
                em.close();
            }
        }
        return user;
    }

    public String getFullName() {
        Utilisateurs u = getUser();
        return u == null ? "" : u.getPrenom() + " " + u.getNom();
    }

    public String getRoleLabel() {
        return isAdmin() ? "Responsable" : "Gérant";
    }

    public void redirectGerant() throws IOException {
        if (!isAdmin()) {
            ExternalContext ec = ec();
            ec.redirect(ec.getRequestContextPath() + "/views/ventes/List.xhtml");
        }
    }
    public void logout() throws IOException, jakarta.servlet.ServletException {
        ExternalContext ec = ec();
        String ctx = ec.getRequestContextPath();
        ((HttpServletRequest) ec.getRequest()).logout();
        ec.invalidateSession();
        ec.redirect(ctx + "/login.xhtml");
    }
}
