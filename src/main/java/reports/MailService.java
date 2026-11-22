
package reports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@ApplicationScoped
public class MailService {

    private Properties config;

    private synchronized Properties config() throws IOException {
        if (config == null) {
            Properties p = new Properties();

            try (InputStream in = getClass().getResourceAsStream("/mail.properties")) {
                if (in == null) {
                    throw new IOException("Fichier mail.properties introuvable.");
                }

                p.load(new java.io.InputStreamReader(
                        in, StandardCharsets.UTF_8));
            }

            String apiKey = System.getenv("BREVO_API_KEY");

            if (apiKey != null && !apiKey.isBlank()) {
                p.setProperty("api.key", apiKey.trim());
            }

            config = p;
        }

        return config;
    }

    public boolean isConfigured() {
        try {
            Properties p = config();

            return !p.getProperty("api.key", "").isBlank()
                    && !p.getProperty("from", "").isBlank();

        } catch (IOException e) {
            return false;
        }
    }

    public void sendWithAttachment(
            String to,
            String subject,
            String text,
            String filename,
            byte[] pdf
    ) throws MessagingException, IOException {

        Properties c = config();

        String apiKey = c.getProperty("api.key", "").trim();
        String from = c.getProperty("from", "").trim();
        String fromName = c.getProperty("from.name", "CAISSEG");

        if (apiKey.isBlank() || from.isBlank()) {
            throw new MessagingException(
                    "Configuration Brevo incomplète.");
        }

        if (pdf == null || pdf.length == 0) {
            throw new MessagingException("Le fichier PDF est vide.");
        }

        InternetAddress[] recipients =
                InternetAddress.parse(to, true);

        StringBuilder toJson = new StringBuilder("[");

        for (int i = 0; i < recipients.length; i++) {
            if (i > 0) {
                toJson.append(",");
            }

            toJson.append("{\"email\":\"")
                    .append(jsonEscape(recipients[i].getAddress()))
                    .append("\"}");
        }

        toJson.append("]");

        String pdfBase64 = Base64.getEncoder()
                .encodeToString(pdf);

        String json = "{"
                + "\"sender\":{"
                + "\"name\":\"" + jsonEscape(fromName) + "\","
                + "\"email\":\"" + jsonEscape(from) + "\""
                + "},"
                + "\"to\":" + toJson + ","
                + "\"subject\":\"" + jsonEscape(subject) + "\","
                + "\"textContent\":\"" + jsonEscape(text) + "\","
                + "\"attachment\":[{"
                + "\"name\":\"" + jsonEscape(filename) + "\","
                + "\"content\":\"" + pdfBase64 + "\""
                + "}]"
                + "}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.brevo.com/v3/smtp/email"))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));

            if (response.statusCode() != 201) {
                throw new MessagingException(
                        "Erreur Brevo HTTP "
                        + response.statusCode()
                        + " : " + response.body());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new MessagingException(
                    "Envoi interrompu : " + e.getMessage());
        }
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
