
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
  import java.text.SimpleDateFormat;

  @WebServlet("/AddNewsServlet")
  public class AddNewsServlet extends HttpServlet {
      @Override
      public void init() throws ServletException {
          DBConnection.init(getServletContext());
      }

      protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          HttpSession session = request.getSession(false);
          if (session == null || session.getAttribute("user_id") == null) {
              response.sendRedirect("login.jsp");
              return;
          }

          int userId = (int) session.getAttribute("user_id");
          String topic = request.getParameter("topic");
          String postDate = request.getParameter("post_date");
          String headline = request.getParameter("headline");
          String article = request.getParameter("article");

          // Basic server-side validation
          if (topic == null || topic.trim().isEmpty() || postDate == null || headline == null || headline.trim().isEmpty() || article == null || article.trim().isEmpty()) {
              request.setAttribute("error", "All fields are required.");
              request.getRequestDispatcher("profile.jsp").forward(request, response);
              return;
          }

          try {
              // Validate date format
              new SimpleDateFormat("yyyy-MM-dd").parse(postDate);

              Connection con = DBConnection.getConnection();
              String query = "INSERT INTO user_news (user_id, topic, post_date, headline, article) VALUES (?, ?, ?, ?, ?)";
              PreparedStatement ps = con.prepareStatement(query);
              ps.setInt(1, userId);
              ps.setString(2, topic);
              ps.setString(3, postDate);
              ps.setString(4, headline);
              ps.setString(5, article);
              ps.executeUpdate();
              ps.close();
              con.close();

              request.setAttribute("success", "News posted successfully!");
              request.getRequestDispatcher("profile.jsp").forward(request, response);
          } catch (Exception e) {
              e.printStackTrace();
              request.setAttribute("error", "Failed to post news: " + e.getMessage());
              request.getRequestDispatcher("profile.jsp").forward(request, response);
          }
      }
  }