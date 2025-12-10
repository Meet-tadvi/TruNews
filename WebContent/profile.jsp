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
    <title>User Profile - News Detector</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { 
            background-color: #1a1a1a; 
            color: #ffffff; 
            margin: 0; 
            padding-top: 80px; /* Space for fixed navbar */
        }
        .profile-container { 
            background-color: #2c2c2c; 
            padding: 30px; 
            border-radius: 8px; 
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.1); 
            width: 100%; 
            max-width: 900px; /* Increased to accommodate side-by-side blocks */
            margin: 20px auto;
        }
        .profile-content {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            justify-content: space-between;
        }
        .profile-block, .post-block {
            flex: 1;
            min-width: 300px;
            background-color: #333;
            padding: 20px;
            border-radius: 8px;
        }
        .form-label, .card-text, .alert, small { color: #ffffff !important; }
        .form-control { 
            background-color: #444; 
            color: #ffffff; 
            border-color: #555; 
        }
        .form-control:focus { 
            background-color: #444; 
            color: #ffffff; 
            border-color: #4d94ff; 
        }
        .btn-update { 
            background-color: #4d94ff; 
            border: none; 
        }
        .btn-update:hover { 
            background-color: #3366cc; 
        }
        .btn-add-news { 
            background-color: #28a745; 
            border: none; 
        }
        .btn-add-news:hover { 
            background-color: #1e7e34; 
        }
        .btn-check-news { 
            background-color: #ff4d4d; 
            border: none; 
        }
        .btn-check-news:hover { 
            background-color: #cc0000; 
        }
        .navbar { 
            background-color: #333; 
            position: fixed; 
            top: 0; 
            width: 100%; 
            z-index: 1050;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">📰 News Detector</a>
            <div class="ms-auto">
                <span class="text-light me-3">Welcome, <%= sess.getAttribute("email") %>!</span>
                <a href="dashboard.jsp" class="btn btn-secondary btn-sm me-2">Dashboard</a>
                <a href="LogoutServlet" class="btn btn-warning btn-sm">Logout</a>
            </div>
        </div>
    </nav>

    <div class="profile-container">
        <h2 class="text-center">👤 User Profile</h2>
        <div class="p-4">
            <% String error = (String) request.getAttribute("error");
               if (error != null) { %>
                <div class="alert alert-danger">
                    <h4><%= error %></h4>
                </div>
            <% } %>
            <% String success = (String) request.getAttribute("success");
               if (success != null) { %>
                <div class="alert alert-success">
                    <h4><%= success %></h4>
                </div>
            <% } %>

            <!-- Profile and Post News Blocks Side by Side -->
            <div class="profile-content">
                <!-- Profile Details Block -->
                <div class="profile-block">
                    <h4 class="text-center" style="color: #ff4d4d;">Profile Details</h4>
                    <p><strong>Username:</strong> <%= request.getAttribute("username") %></p>
                    <p><strong>Email:</strong> <%= request.getAttribute("userEmail") %></p>
                    <p><strong>Joined:</strong> <%= request.getAttribute("createdAt") %></p>
                    <form action="ProfileServlet" method="post" class="mt-3">
                        <div class="mb-3">
                            <label for="newUsername" class="form-label">Update Username:</label>
                            <input type="text" class="form-control" id="newUsername" name="newUsername" maxlength="50" placeholder="Enter new username">
                        </div>
                        <button type="submit" class="btn btn-update btn-sm w-100">Update Username</button>
                    </form>
                    <h5 class="mt-4 text-center">Change Password</h5>
                    <form action="ProfileServlet" method="post" class="mt-3">
                        <div class="mb-3">
                            <label for="newPassword" class="form-label">New Password:</label>
                            <input type="password" class="form-control" id="newPassword" name="newPassword" minlength="8">
                        </div>
                        <div class="mb-3">
                            <label for="confirmPassword" class="form-label">Confirm Password:</label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" minlength="8">
                        </div>
                        <button type="submit" class="btn btn-update btn-sm w-100">Update Password</button>
                    </form>
                </div>

                <!-- Post News Block -->
                <div class="post-block">
                    <h4 class="text-center" style="color: #ff4d4d;">Post News</h4>
                    <form action="AddNewsServlet" method="post" class="mt-3">
                        <div class="mb-3">
                            <label for="topic" class="form-label">Topic:</label>
                            <input type="text" class="form-control" id="topic" name="topic" required maxlength="100">
                        </div>
                        <div class="mb-3">
                            <label for="post_date" class="form-label">Date:</label>
                            <input type="date" class="form-control" id="post_date" name="post_date" required>
                        </div>
                        <div class="mb-3">
                            <label for="headline" class="form-label">Headline:</label>
                            <input type="text" class="form-control" id="headline" name="headline" required maxlength="255">
                        </div>
                        <div class="mb-3">
                            <label for="article" class="form-label">Article:</label>
                            <textarea class="form-control" id="article" name="article" rows="5" required></textarea>
                        </div>
                        <button type="submit" class="btn btn-add-news btn-sm w-100">Add News</button>
                    </form>
                </div>
            </div>

            <!-- View News Section -->
            <div class="mt-4">
                <h4 class="text-center" style="color: #ff4d4d;">View Your News</h4>
                <a href="ViewUserNewsServlet" class="btn btn-check-news btn-sm w-100">Check News</a>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>