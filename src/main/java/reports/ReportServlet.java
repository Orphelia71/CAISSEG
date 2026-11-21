package reports;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.sf.jasperreports.engine.JRException;

/**
 * Recu PDF d'une vente : /reports/recu?id=ID_VENTE.
 * L'acces est controle par les contraintes de securite de WEB-INF/web.xml.
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/reports/recu"})
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Inject
    private ReceiptService receipts;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String rawId = req.getParameter("id");
        final Integer id;
        try {
            if (rawId == null || rawId.trim().isEmpty()) {
                throw new NumberFormatException("id absent");
            }
            id = Integer.valueOf(rawId.trim());
            if (id <= 0) {
                throw new NumberFormatException("id non positif");
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Identifiant de vente invalide. Exemple : /reports/recu?id=1");
            return;
        }

        try {
            byte[] pdf = receipts.generatePdf(id);
            if (pdf == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Vente introuvable.");
                return;
            }

            resp.reset();
            resp.setContentType("application/pdf");
            resp.setContentLength(pdf.length);
            resp.setHeader("Content-Disposition", String.format("inline; filename=\"recu-%06d.pdf\"", id));
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.getOutputStream().write(pdf);
            resp.getOutputStream().flush();

        } catch (JRException e) {
            throw new ServletException(
                    "Erreur JasperReports pendant la generation du recu de la vente " + id, e);
        }
    }
}
