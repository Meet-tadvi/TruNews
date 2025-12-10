package newsdetects;

  import jakarta.servlet.ServletException;
  import jakarta.servlet.annotation.WebServlet;
  import jakarta.servlet.http.HttpServlet;
  import jakarta.servlet.http.HttpServletRequest;
  import jakarta.servlet.http.HttpServletResponse;
  import java.io.IOException;
  import java.sql.Connection;
  import java.sql.PreparedStatement;
  import org.mindrot.jbcrypt.BCrypt;

  @WebServlet("/SignupServlet")
  public class SignupServlet extends HttpServlet {
      @Override
      public void init() throws ServletException {
          DBConnection.init(getServletContext());
      }

      protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          String username = request.getParameter("username");
          String email = request.getParameter("email");
          String password = request.getParameter("password");
          String confirmPassword = request.getParameter("confirmPassword");

          // Validate inputs
          if (username == null || username.trim().length() < 3 || username.length() > 50) {
              request.setAttribute("error", "Username must be 3-50 characters long.");
              request.getRequestDispatcher("signup.jsp").forward(request, response);
              return;
          }
          if (email == null || email.trim().isEmpty() || email.length() > 100) {
              request.setAttribute("error", "Invalid email address.");
              request.getRequestDispatcher("signup.jsp").forward(request, response);
              return;
          }
          if (password == null || password.length() < 8 || password.length() > 100) {
              request.setAttribute("error", "Password must be 8-100 characters long.");
              request.getRequestDispatcher("signup.jsp").forward(request, response);
              return;
          }
          if (!password.equals(confirmPassword)) {
              request.setAttribute("error", "Passwords do not match.");
              request.getRequestDispatcher("signup.jsp").forward(request, response);
              return;
          }

          try {
              Connection con = DBConnection.getConnection();
              // Insert user
              String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
              PreparedStatement ps = con.prepareStatement(query);
              ps.setString(1, username);
              ps.setString(2, email);
              ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt()));
              ps.executeUpdate();
              ps.close();
              con.close();

              response.sendRedirect("login.jsp");
          } catch (Exception e) {
              e.printStackTrace();
              request.setAttribute("error", "Signup failed: " + (e.getMessage().contains("Duplicate entry") ? "Username or email already exists." : e.getMessage()));
              request.getRequestDispatcher("signup.jsp").forward(request, response);
          }
      }
  }