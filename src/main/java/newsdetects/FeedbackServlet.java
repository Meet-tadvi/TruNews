package newsdetects;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.mail.*;
import jakarta.mail.internet.*;

@WebServlet("/FeedbackServlet")
public class FeedbackServlet extends HttpServlet {
    private static String EMAIL_USER;
    private static String EMAIL_PASSWORD;

    @Override
    public void init() throws ServletException {
        DBConnection.init(getServletContext());
        try {
            Properties props = new Properties();
            props.load(getServletContext().getResourceAsStream("/WEB-INF/config.properties"));
            EMAIL_USER = props.getProperty("email.user").trim(); // Trim to remove whitespace
            EMAIL_PASSWORD = props.getProperty("email.password").trim(); // Trim password too
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("user_id");
        String userEmail = request.getParameter("userEmail");
        String feedbackText = request.getParameter("feedbackText");
        int rating = Integer.parseInt(request.getParameter("rating"));

        try {
            // Store feedback in database
            Connection con = DBConnection.getConnection();
            String query = "INSERT INTO feedback (user_id, user_email, feedback_text, rating) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setString(2, userEmail);
            ps.setString(3, feedbackText);
            ps.setInt(4, rating);
            ps.executeUpdate();

            // Send email notification
            try {
                sendEmail(userEmail, feedbackText, rating);
                request.setAttribute("feedbackSuccess", "Thank you for your feedback!");
            } catch (MessagingException e) {
                e.printStackTrace();
                request.setAttribute("feedbackError", "Feedback saved, but email notification failed: " + e.getMessage());
            }

            // Redirect back to dashboard
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
    }

    private void sendEmail(String userEmail, String feedbackText, int rating) throws MessagingException {
        String to = EMAIL_USER;
        String from = EMAIL_USER;
        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USER, EMAIL_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("New Feedback from News Detector User");
        message.setText("User Email: " + userEmail + "\nFeedback: " + feedbackText + "\nRating: " + rating + "/5");

        Transport.send(message);
        System.out.println("Feedback email sent successfully!");
    }
}