<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
  <%@ page import="jakarta.servlet.http.HttpSession" %>

  <%
      HttpSession sess = request.getSession(false);
      if (sess == null || sess.getAttribute("user_id") == null) {
          response.sendRedirect("login.jsp");
          return;
      }
  %>

  <!DOCTYPE html>
  <html lang="en">
  <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Edit News - News Detector</title>
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
      <style>
          body { background-color: #1a1a1a; color: #ffffff; margin: 0; }
          .navbar { 
              background-color: #333; 
              position: fixed; 
              top: 0; 
              width: 100%; 
              z-index: 1050;
          }
          .container-box { 
              background-color: #2c2c2c; 
              padding: 20px; 
              border-radius: 8px; 
              box-shadow: 0 0 10px rgba(255, 255, 255, 0.1); 
              margin-top: 80px;
              color: #ffffff;
          }
          .form-label { color: #ffffff; }
          .form-control { background-color: #444; color: #ffffff; border-color: #555; }
          .form-control:focus { background-color: #444; color: #ffffff; border-color: #4d94ff; }
          .btn-update { background-color: #4d94ff; border: none; }
          .btn-update:hover { background-color: #3366cc; }
      </style>
  </head>
  <body>
      <nav class="navbar navbar-expand-lg navbar-dark">
          <div class="container-fluid">
              <a class="navbar-brand" href="#">📰 News Detector</a>
              <div class="ms-auto">
                  <span class="text-light me-3">Welcome, <%= sess.getAttribute("email") %>!</span>
                  <a href="dashboard.jsp" class="btn btn-secondary btn-sm me-2">Dashboard</a>
                  <a href="ProfileServlet" class="btn btn-secondary btn-sm me-2">Profile</a>
                  <a href="LogoutServlet" class="btn btn-warning btn-sm">Logout</a>
              </div>
          </div>
      </nav>

      <div class="container">
          <div class="container-box">
              <h2 class="text-center">✏️ Edit News</h2>
              <div class="p-4">
                  <% String error = (String) request.getAttribute("error");
                     if (error == null) {
                         error = (String) sess.getAttribute("error");
                         if (error != null) {
                             sess.removeAttribute("error");
                         }
                     }
                     if (error != null) { %>
                      <div class="alert alert-danger">
                          <h4><%= error %></h4>
                      </div>
                  <% } %>
                  <% String success = (String) sess.getAttribute("success");
                     if (success != null) {
                         sess.removeAttribute("success");
                  %>
                      <div class="alert alert-success">
                          <h4><%= success %></h4>
                      </div>
                  <% } %>

                  <form action="EditNewsServlet" method="post">
                      <input type="hidden" name="newsId" value="<%= request.getAttribute("newsId") != null ? request.getAttribute("newsId") : "" %>">
                      <div class="mb-3">
                          <label for="topic" class="form-label">Topic:</label>
                          <input type="text" class="form-control" id="topic" name="topic" required maxlength="100" 
                                 value="<%= request.getAttribute("topic") != null ? request.getAttribute("topic") : "" %>">
                      </div>
                      <div class="mb-3">
                          <label for="post_date" class="form-label">Date:</label>
                          <input type="date" class="form-control" id="post_date" name="post_date" required 
                                 value="<%= request.getAttribute("post_date") != null ? request.getAttribute("post_date") : "" %>">
                      </div>
                      <div class="mb-3">
                          <label for="headline" class="form-label">Headline:</label>
                          <input type="text" class="form-control" id="headline" name="headline" required maxlength="255" 
                                 value="<%= request.getAttribute("headline") != null ? request.getAttribute("headline") : "" %>">
                      </div>
                      <div class="mb-3">
                          <label for="article" class="form-label">Article:</label>
                          <textarea class="form-control" id="article" name="article" rows="5" required><%= request.getAttribute("article") != null ? request.getAttribute("article") : "" %></textarea>
                      </div>
                      <button type="submit" class="btn btn-update w-100">Update News</button>
                  </form>
                  <a href="ViewUserNewsServlet" class="btn btn-secondary mt-3">Back to Your News</a>
              </div>
          </div>
      </div>

      <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  </body>
  </html>