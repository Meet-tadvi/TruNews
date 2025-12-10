package newsdetects;

import java.io.*;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        DBConnection.init(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println("🔹 Email entered: " + email);
        System.out.println("🔹 Checking database for email...");

        try {
            Connection con = DBConnection.getConnection();
            String query = "SELECT id, password FROM users WHERE email = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("id");
                String hashedPassword = rs.getString("password");

                System.out.println("🔹 User found with ID: " + userID);
                System.out.println("🔹 Hashed Password from DB: " + hashedPassword);

                if (BCrypt.checkpw(password, hashedPassword)) {
                    System.out.println("✅ Password match! Logging in...");
                    HttpSession session = request.getSession();
                    session.setAttribute("user_id", userID);
                    session.setAttribute("email", email);
                    response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
                } else {
                    System.out.println("❌ Password does NOT match!");
                    response.sendRedirect("login.jsp?error=1");
                }
            } else {
                System.out.println("❌ No user found with this email!");
                response.sendRedirect("login.jsp?error=1");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
    }
}