package sessions;

import entities.Ventes;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import reports.MailService;
import reports.ReceiptService;

/** Page views/ventes/Mail.xhtml : envoi du reçu PDF d'une vente par e-mail. */
@Named("receiptMailController")
@ViewScoped
public class ReceiptMailController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ReceiptMailController.class.getName());

    @Inject
    private ReceiptService receipts;
    @Inject
    private MailService mail;

    private Integer id;
    private String email;
    private Ventes vente;

    /** Récupère l'identifiant transmis dans l'URL puis charge la vente. */
    public void init() {
        loadVente();
    }

    /**
     * Chargement différé : garantit que la vente est chargée même si l'action
     * f:viewAction du template n'a pas été exécutée au premier affichage.
     */
    private void loadVente() {
        if (id == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
                String idParam = params.get("id");
                try {
                    if (idParam != null && !idParam.trim().isEmpty()) {
                        id = Integer.valueOf(idParam.trim());
                    }
                } catch (NumberFormatException e) {
                    LOG.log(Level.WARNING, "Identifiant de vente invalide : " + idParam, e);
                }
            }
        }

        if (id != null && vente == null && receipts != null) {
            try {
                vente = receipts.findVente(id);
                if (vente == null) {
                    LOG.warning("Aucune vente trouvée pour l'identifiant " + id);
                }
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Erreur lors du chargement de la vente " + id, e);
                throw e;
            }
        }
    }

    public String send() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Ventes venteCourante = getVente();
        if (venteCourante == null) {
            msg(fc, FacesMessage.SEVERITY_ERROR,
                    id == null ? "Identifiant de vente absent ou invalide." : "Vente introuvable (numéro " + id + ").");
            return null;
        }
        if (email == null || email.trim().isEmpty()) {
            msg(fc, FacesMessage.SEVERITY_ERROR, "Veuillez saisir l'adresse e-mail du client.");
            return null;
        }
        if (!mail.isConfigured()) {
            msg(fc, FacesMessage.SEVERITY_ERROR,
                    "L'envoi d'e-mails n'est pas configuré. Vérifiez mail.properties et la variable CAISSEG_MAIL_PASSWORD.");
            return null;
        }

        try {
            byte[] pdf = receipts.generatePdf(id);
            if (pdf == null || pdf.length == 0) {
                msg(fc, FacesMessage.SEVERITY_ERROR, "Le reçu PDF n'a pas pu être généré pour cette vente.");
                return null;
            }
            String numero = ReceiptService.numero(id);
            String montant = new DecimalFormat("#,##0.##").format(venteCourante.getMontant());
            String text = "Bonjour,\n\nVeuillez trouver ci-joint le reçu " + numero
                    + " de votre achat d'un montant de " + montant
                    + " FCFA.\n\nMerci de votre confiance.\n\nCAISSEG";
            mail.sendWithAttachment(email.trim(), "Votre reçu " + numero, text,
                    "recu-" + numero + ".pdf", pdf);
            msg(fc, FacesMessage.SEVERITY_INFO,
                    "Reçu " + numero + " envoyé à " + email.trim() + ".");
            email = null;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Échec d'envoi du reçu " + id, e);
            msg(fc, FacesMessage.SEVERITY_ERROR, "Échec de l'envoi : " + e.getMessage());
        }
        return null;
    }

    private static void msg(FacesContext fc, FacesMessage.Severity severity, String text) {
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, text, text));
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (this.id == null || !this.id.equals(id)) {
            this.vente = null;
        }
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Ventes getVente() {
        loadVente();
        return vente;
    }
}
