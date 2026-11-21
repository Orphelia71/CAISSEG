package reports;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Envoi d'e-mails par SMTP. Configuration dans src/main/resources/mail.properties.
 * Le mot de passe peut aussi etre fourni par la variable d'environnement CAISSEG_MAIL_PASSWORD
 * (preferable : ne pas ecrire de mot de passe dans le code).
 */
@ApplicationScoped
public class MailService {

    private Properties config;

    private synchronized Properties config() throws IOException {
        if (config == null) {
            Properties p = new Properties();
            try (InputStream in = getClass().getResourceAsStream("/mail.properties")) {
                if (in != null) {
                    p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            String envPassword = System.getenv("CAISSEG_MAIL_PASSWORD");
            if (envPassword != null && !envPassword.isBlank()) {
                p.setProperty("password", envPassword);
            }
            config = p;
        }
        return config;
    }

    public boolean isConfigured() {
        try {
            Properties p = config();
            return !p.getProperty("host", "").isBlank() && !p.getProperty("from", "").isBlank();
        } catch (IOException e) {
            return false;
        }
    }

    public void sendWithAttachment(String to, String subject, String text, String filename, byte[] pdf)
            throws MessagingException, IOException {
        Properties c = config();
        Properties smtp = new Properties();
        smtp.put("mail.smtp.host", c.getProperty("host"));
        smtp.put("mail.smtp.port", c.getProperty("port", "587"));
        smtp.put("mail.smtp.connectiontimeout", "10000");
        smtp.put("mail.smtp.timeout", "15000");
        smtp.put("mail.smtp.writetimeout", "15000");
        smtp.put("mail.smtp.starttls.enable", c.getProperty("starttls", "true"));
        smtp.put("mail.smtp.ssl.enable", c.getProperty("ssl", "false"));
        String user = c.getProperty("user", "");
        String password = c.getProperty("password", "");
        boolean auth = !user.isBlank();
        smtp.put("mail.smtp.auth", String.valueOf(auth));

        Session session = auth
                ? Session.getInstance(smtp, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                })
                : Session.getInstance(smtp);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(c.getProperty("from"), c.getProperty("from.name", "CAISSEG"), "UTF-8"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, true));
        msg.setSubject(subject, "UTF-8");

        MimeBodyPart body = new MimeBodyPart();
        body.setText(text, "UTF-8");
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(new jakarta.activation.DataHandler(new ByteArrayDataSource(pdf, "application/pdf")));
        attachment.setFileName(filename);

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(body);
        multipart.addBodyPart(attachment);
        msg.setContent(multipart);

        Transport.send(msg);
    }
}
