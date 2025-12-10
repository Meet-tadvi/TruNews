package newsdetects;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        DBConnection.init(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("user_id");
        try {
            Connection con = DBConnection.getConnection();
            String query = "SELECT email, username, created_at FROM users WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                request.setAttribute("userEmail", rs.getString("email"));
                request.setAttribute("username", rs.getString("username"));
                request.setAttribute("createdAt", rs.getTimestamp("created_at").toString());
            }
            rs.close();
            ps.close();
            con.close();
            request.getRequestDispatcher("profile.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to load profile: " + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("user_id");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        String newUsername = request.getParameter("newUsername");

        // Handle username update
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            if (newUsername.length() < 3 || newUsername.length() > 50) {
                request.setAttribute("error", "Username must be 3-50 characters long.");
                doGet(request, response);
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String query = "UPDATE users SET username = ? WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, newUsername);
                ps.setInt(2, userId);
                ps.executeUpdate();
                ps.close();
                con.close();
                session.setAttribute("username", newUsername); // Update session
                request.setAttribute("success", "Username updated successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "Failed to update username: " + e.getMessage());
                doGet(request, response);
                return;
            }
        }

        // Handle password update
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("error", "Passwords do not match.");
                doGet(request, response);
                return;
            }
            if (newPassword.length() < 8) {
                request.setAttribute("error", "Password must be at least 8 characters long.");
                doGet(request, response);
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                String query = "UPDATE users SET password = ? WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, hashedPassword);
                ps.setInt(2, userId);
                ps.executeUpdate();
                ps.close();
                con.close();
                request.setAttribute("success", "Password updated successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "Failed to update password: " + e.getMessage());
                doGet(request, response);
                return;
            }
        }

        doGet(request, response); // Reload profile page
    }
}