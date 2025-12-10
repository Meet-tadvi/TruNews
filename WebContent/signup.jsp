<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
  <!DOCTYPE html>
  <html lang="en">
  <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Sign Up - News Detector</title>
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
      <style>
          body { background-color: #1a1a1a; color: #ffffff; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
          .signup-container { 
              background-color: #2c2c2c; 
              padding: 30px; 
              border-radius: 8px; 
              box-shadow: 0 0 10px rgba(255, 255, 255, 0.1); 
              width: 100%; 
              max-width: 400px;
          }
          .form-label { color: #ffffff; }
          .form-control { background-color: #444; color: #ffffff; border-color: #555; }
          .form-control:focus { background-color: #444; color: #ffffff; border-color: #4d94ff; }
          .btn-signup { background-color: #4d94ff; border: none; }
          .btn-signup:hover { background-color: #3366cc; }
      </style>
  </head>
  <body>
      <div class="signup-container">
          <h2 class="text-center">📰 Sign Up</h2>
          <% String error = (String) request.getAttribute("error");
             if (error != null) { %>
              <div class="alert alert-danger">
                  <h4><%= error %></h4>
              </div>
          <% } %>
          <form action="SignupServlet" method="post">
              <div class="mb-3">
                  <label for="username" class="form-label">Username:</label>
                  <input type="text" class="form-control" id="username" name="username" required minlength="3" maxlength="50" placeholder="Enter username">
              </div>
              <div class="mb-3">
                  <label for="email" class="form-label">Email:</label>
                  <input type="email" class="form-control" id="email" name="email" required maxlength="100" placeholder="Enter email">
              </div>
              <div class="mb-3">
                  <label for="password" class="form-label">Password:</label>
                  <input type="password" class="form-control" id="password" name="password" required minlength="8" maxlength="100" placeholder="Enter password">
              </div>
              <div class="mb-3">
                  <label for="confirmPassword" class="form-label">Confirm Password:</label>
                  <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="8" maxlength="100" placeholder="Confirm password">
              </div>
              <button type="submit" class="btn btn-signup w-100">Sign Up</button>
          </form>
          <p class="text-center mt-3">Already have an account? <a href="login.jsp" class="text-info">Login</a></p>
      </div>
      <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  </body>
  </html>