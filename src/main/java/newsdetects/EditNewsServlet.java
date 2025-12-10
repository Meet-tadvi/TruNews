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

  @WebServlet("/EditNewsServlet")
  public class EditNewsServlet extends HttpServlet {
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

          String newsId = request.getParameter("newsId");
          if (newsId == null || newsId.trim().isEmpty()) {
              request.setAttribute("error", "Invalid news ID.");
              request.getRequestDispatcher("view_user_news.jsp").forward(request, response);
              return;
          }

          try {
              Connection con = DBConnection.getConnection();
              String query = "SELECT topic, post_date, headline, article FROM user_news WHERE id = ? AND user_id = ?";
              PreparedStatement ps = con.prepareStatement(query);
              ps.setInt(1, Integer.parseInt(newsId));
              ps.setInt(2, (int) session.getAttribute("user_id"));
              ResultSet rs = ps.executeQuery();
              if (rs.next()) {
                  request.setAttribute("newsId", newsId);
                  request.setAttribute("topic", rs.getString("topic"));
                  request.setAttribute("post_date", rs.getString("post_date"));
                  request.setAttribute("headline", rs.getString("headline"));
                  request.setAttribute("article", rs.getString("article"));
              } else {
                  request.setAttribute("error", "News not found or you don't have permission to edit.");
                  request.getRequestDispatcher("view_user_news.jsp").forward(request, response);
                  return;
              }
              rs.close();
              ps.close();
              con.close();
              request.getRequestDispatcher("edit_news.jsp").forward(request, response);
          } catch (Exception e) {
              e.printStackTrace();
              request.setAttribute("error", "Failed to load news: " + e.getMessage());
              request.getRequestDispatcher("view_user_news.jsp").forward(request, response);
          }
      }

      protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          HttpSession session = request.getSession(false);
          if (session == null || session.getAttribute("user_id") == null) {
              response.sendRedirect("login.jsp");
              return;
          }

          String newsId = request.getParameter("newsId");
          String topic = request.getParameter("topic");
          String postDate = request.getParameter("post_date");
          String headline = request.getParameter("headline");
          String article = request.getParameter("article");

          // Validate inputs
          if (newsId == null || topic == null || postDate == null || headline == null || article == null ||
              topic.trim().isEmpty() || postDate.trim().isEmpty() || headline.trim().isEmpty() || article.trim().isEmpty()) {
              request.setAttribute("error", "All fields are required.");
              request.setAttribute("newsId", newsId);
              request.setAttribute("topic", topic);
              request.setAttribute("post_date", postDate);
              request.setAttribute("headline", headline);
              request.setAttribute("article", article);
              request.getRequestDispatcher("edit_news.jsp").forward(request, response);
              return;
          }
          if (topic.length() > 100 || headline.length() > 255) {
              request.setAttribute("error", "Topic or headline too long.");
              request.setAttribute("newsId", newsId);
              request.setAttribute("topic", topic);
              request.setAttribute("post_date", postDate);
              request.setAttribute("headline", headline);
              request.setAttribute("article", article);
              request.getRequestDispatcher("edit_news.jsp").forward(request, response);
              return;
          }

          try {
              Connection con = DBConnection.getConnection();
              String query = "UPDATE user_news SET topic = ?, post_date = ?, headline = ?, article = ? WHERE id = ? AND user_id = ?";
              PreparedStatement ps = con.prepareStatement(query);
              ps.setString(1, topic);
              ps.setString(2, postDate);
              ps.setString(3, headline);
              ps.setString(4, article);
              ps.setInt(5, Integer.parseInt(newsId));
              ps.setInt(6, (int) session.getAttribute("user_id"));
              int rows = ps.executeUpdate();
              ps.close();
              con.close();

              if (rows > 0) {
                  session.setAttribute("success", "News updated successfully!");
              } else {
                  session.setAttribute("error", "Failed to update news or you don't have permission.");
              }
              response.sendRedirect("ViewUserNewsServlet");
          } catch (Exception e) {
              e.printStackTrace();
              request.setAttribute("error", "Failed to update news: " + e.getMessage());
              request.setAttribute("newsId", newsId);
              request.setAttribute("topic", topic);
              request.setAttribute("post_date", postDate);
              request.setAttribute("headline", headline);
              request.setAttribute("article", article);
              request.getRequestDispatcher("edit_news.jsp").forward(request, response);
          }
      }
  }