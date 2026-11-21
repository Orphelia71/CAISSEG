package sessions;

import entities.Ventes;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import security.Roles;

/** Chiffres du tableau de bord administrateur. */
@Named("dashboardController")
@RequestScoped
public class DashboardController {

    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf;

    private long nbVentes;
    private long nbVentesAujourdhui;
    private BigDecimal totalVentes = BigDecimal.ZERO;
    private BigDecimal totalAujourdhui = BigDecimal.ZERO;
    private int disponibles;
    private int occupees;
    private int maintenance;
    private int totalCaisses;
    private int nbGerants;
    private List<Ventes> dernieresVentes;

    @PostConstruct
    public void load() {
        EntityManager em = emf.createEntityManager();
        try {
            Date debut = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

            nbVentes = em.createQuery("SELECT COUNT(v) FROM Ventes v", Long.class).getSingleResult();
            totalVentes = em.createQuery("SELECT SUM(v.montant) FROM Ventes v", BigDecimal.class).getSingleResult();
            nbVentesAujourdhui = em.createQuery("SELECT COUNT(v) FROM Ventes v WHERE v.dateVente >= :d", Long.class)
                    .setParameter("d", debut).getSingleResult();
            totalAujourdhui = em.createQuery("SELECT SUM(v.montant) FROM Ventes v WHERE v.dateVente >= :d", BigDecimal.class)
                    .setParameter("d", debut).getSingleResult();
            if (totalVentes == null) {
                totalVentes = BigDecimal.ZERO;
            }
            if (totalAujourdhui == null) {
                totalAujourdhui = BigDecimal.ZERO;
            }

            for (String etat : em.createQuery("SELECT c.etat FROM Caisses c", String.class).getResultList()) {
                totalCaisses++;
                String e = Roles.normalize(etat);
                if (e.startsWith("dispo")) {
                    disponibles++;
                } else if (e.startsWith("occup")) {
                    occupees++;
                } else if (e.startsWith("maint") || e.startsWith("inactiv")) {
                    maintenance++;
                }
            }

            for (String role : em.createQuery("SELECT u.role FROM Utilisateurs u", String.class).getResultList()) {
                if (Roles.isGerant(role)) {
                    nbGerants++;
                }
            }

            dernieresVentes = em.createQuery("SELECT v FROM Ventes v ORDER BY v.dateVente DESC", Ventes.class)
                    .setMaxResults(5).getResultList();
        } finally {
            em.close();
        }
    }

    public long getNbVentes() { return nbVentes; }
    public long getNbVentesAujourdhui() { return nbVentesAujourdhui; }
    public BigDecimal getTotalVentes() { return totalVentes; }
    public BigDecimal getTotalAujourdhui() { return totalAujourdhui; }
    public int getDisponibles() { return disponibles; }
    public int getOccupees() { return occupees; }
    public int getMaintenance() { return maintenance; }
    public int getTotalCaisses() { return totalCaisses; }
    public int getNbGerants() { return nbGerants; }
    public List<Ventes> getDernieresVentes() { return dernieresVentes; }
}
