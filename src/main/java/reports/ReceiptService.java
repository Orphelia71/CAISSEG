package reports;

import entities.Ventes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/** Service central de generation des recus PDF. */
@ApplicationScoped
public class ReceiptService {

    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf;

    private volatile JasperReport cachedReport;

    public static String numero(Integer id) {
        return String.format("REC-%06d", id);
    }

    public Ventes findVente(Integer id) {
        if (id == null) {
            return null;
        }
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Ventes.class, id);
        }
    }

    /**
     * Genere le PDF du recu correspondant a une vente.
     *
     * @param id identifiant de la vente
     * @return le contenu PDF, ou null si la vente n'existe pas
     */
    public byte[] generatePdf(Integer id) throws JRException, IOException {
        Ventes vente = findVente(id);
        if (vente == null) {
            return null;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("NUMERO", numero(id));
        params.put("GENERE_LE", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        JasperPrint print = JasperFillManager.fillReport(
                report(),
                params,
                new JRBeanCollectionDataSource(List.of(vente))
        );

        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            net.sf.jasperreports.engine.export.JRPdfExporter exporter =
                    new net.sf.jasperreports.engine.export.JRPdfExporter();
            exporter.setExporterInput(
                    new net.sf.jasperreports.export.SimpleExporterInput(print));
            exporter.setExporterOutput(
                    new net.sf.jasperreports.export.SimpleOutputStreamExporterOutput(output));
            exporter.exportReport();
            return output.toByteArray();
        }
    }

    /** Compile le modèle une seule fois par cycle de vie de l'application. */
    private JasperReport report() throws JRException, IOException {
        JasperReport result = cachedReport;
        if (result == null) {
            synchronized (this) {
                result = cachedReport;
                if (result == null) {
                    try (InputStream in = getClass().getResourceAsStream("/reports/recu.jrxml")) {
                        if (in == null) {
                            throw new IOException(
                                    "Fichier /reports/recu.jrxml introuvable dans src/main/resources/reports/");
                        }
                        result = JasperCompileManager.compileReport(in);
                        cachedReport = result;
                    }
                }
            }
        }
        return result;
    }
}
