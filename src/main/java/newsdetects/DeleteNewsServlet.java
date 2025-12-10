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

  @WebServlet("/DeleteNewsServlet")
  public class DeleteNewsServlet extends HttpServlet {
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
              request.getRequestDispatcher("ViewUserNewsServlet").forward(request, response);
              return;
          }

          try {
              Connection con = DBConnection.getConnection();
              String query = "DELETE FROM user_news WHERE id = ? AND user_id = ?";
              PreparedStatement ps = con.prepareStatement(query);
              ps.setInt(1, Integer.parseInt(newsId));
              ps.setInt(2, (int) session.getAttribute("user_id"));
              int rows = ps.executeUpdate();
              ps.close();
              con.close();

              if (rows > 0) {
                  request.setAttribute("success", "News deleted successfully!");
              } else {
                  request.setAttribute("error", "Failed to delete news or you don't have permission.");
              }
              request.getRequestDispatcher("ViewUserNewsServlet").forward(request, response);
          } catch (Exception e) {
              e.printStackTrace();
              request.setAttribute("error", "Failed to delete news: " + e.getMessage());
              request.getRequestDispatcher("ViewUserNewsServlet").forward(request, response);
          }
      }
  }