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

  @WebServlet("/CommunityNewsServlet")
  public class CommunityNewsServlet extends HttpServlet {
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

          List<String[]> communityNews = new ArrayList<>();
          try {
              Connection con = DBConnection.getConnection();
              String query = "SELECT u.email, n.topic, n.post_date, n.headline, n.article, n.submitted_at " +
                            "FROM user_news n JOIN users u ON n.user_id = u.id " +
                            "ORDER BY n.submitted_at DESC";
              PreparedStatement ps = con.prepareStatement(query);
              ResultSet rs = ps.executeQuery();
              while (rs.next()) {
                  communityNews.add(new String[]{
                      rs.getString("email"),
                      rs.getString("topic"),
                      rs.getString("post_date"),
                      rs.getString("headline"),
                      rs.getString("article"),
                      rs.getTimestamp("submitted_at").toString()
                  });
              }
              rs.close();
              ps.close();
              con.close();

              request.setAttribute("communityNews", communityNews);
              request.getRequestDispatcher("community_news.jsp").forward(request, response);
          } catch (Exception e) {
              e.printStackTrace();
              request.setAttribute("error", "Failed to load community news: " + e.getMessage());
              request.getRequestDispatcher("community_news.jsp").forward(request, response);
          }
      }
  }