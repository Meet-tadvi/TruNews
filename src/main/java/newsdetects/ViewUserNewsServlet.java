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
  import java.util.ArrayList;
  import java.util.List;

  @WebServlet("/ViewUserNewsServlet")
  public class ViewUserNewsServlet extends HttpServlet {
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

          List<String[]> userNews = new ArrayList<>();
          try {
              Connection con = DBConnection.getConnection();
              String query = "SELECT topic, post_date, headline, article, submitted_at, id " +
                            "FROM user_news WHERE user_id = ? ORDER BY submitted_at DESC";
              PreparedStatement ps = con.prepareStatement(query);
              ps.setInt(1, (int) session.getAttribute("user_id"));
              ResultSet rs = ps.executeQuery();
              while (rs.next()) {
                  userNews.add(new String[]{
                      rs.getString("topic") != null ? rs.getString("topic") : "",
                      rs.getString("post_date") != null ? rs.getString("post_date") : "",
                      rs.getString("headline") != null ? rs.getString("headline") : "",
                      rs.getString("article") != null ? rs.getString("article") : "",
                      rs.getTimestamp("submitted_at") != null ? rs.getTimestamp("submitted_at").toString() : "",
                      String.valueOf(rs.getInt("id"))
                  });
              }
              rs.close();
              ps.close();
              con.close();

              request.setAttribute("userNews", userNews);
              request.getRequestDispatcher("view_user_news.jsp").forward(request, response);
          } catch (Exception e) {
              e.printStackTrace();
              session.setAttribute("error", "Failed to load your news: " + e.getMessage());
              request.getRequestDispatcher("view_user_news.jsp").forward(request, response);
          }
      }
  }