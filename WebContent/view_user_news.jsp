<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpSession, java.util.List" %>
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
    <title>Your News - News Detector</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { 
            background-color: #1a1a1a; 
            color: #ffffff; 
            margin: 0; 
            padding-top: 80px; /* Space for fixed navbar */
        }
        .news-container { 
            background-color: #2c2c2c; 
            padding: 30px; 
            border-radius: 8px; 
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.1); 
            width: 100%; 
            max-width: 600px; 
            margin: 20px auto;
        }
        .news-card { 
            background-color: #333; 
            border: none; 
            margin-bottom: 20px; 
            border-radius: 8px; 
            color: #ffffff; 
        }
        .news-card .card-body { padding: 15px; }
        .news-card .card-title { color: #ff4d4d; }
        .card-text, .text-muted { color: #ffffff !important; }
        .btn-edit { 
            background-color: #4d94ff; 
            border: none; 
        }
        .btn-edit:hover { 
            background-color: #3366cc; 
        }
        .btn-delete { 
            background-color: #dc3545; 
            border: none; 
        }
        .btn-delete:hover { 
            background-color: #b02a37; 
        }
        .btn-back { 
            background-color: #6c757d; 
            border: none; 
        }
        .btn-back:hover { 
            background-color: #5c636a; 
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
                <a href="ProfileServlet" class="btn btn-secondary btn-sm me-2">Profile</a>
                <a href="LogoutServlet" class="btn btn-warning btn-sm">Logout</a>
            </div>
        </div>
    </nav>

    <div class="news-container">
        <h2 class="text-center">📰 Your News</h2>
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

            <% List<String[]> userNews = (List<String[]>) request.getAttribute("userNews");
               if (userNews == null || userNews.isEmpty()) { %>
                <p>No news posted yet.</p>
            <% } else { %>
                <% for (String[] news : userNews) { %>
                    <div class="card news-card">
                        <div class="card-body">
                            <h5 class="card-title"><%= news[2].isEmpty() ? "No Headline" : news[2] %></h5>
                            <p class="card-text"><strong>Topic:</strong> <%= news[0].isEmpty() ? "No Topic" : news[0] %></p>
                            <p class="card-text"><strong>Date:</strong> <%= news[1].isEmpty() ? "No Date" : news[1] %></p>
                            <p class="card-text"><strong>Article:</strong> <%= news[3].isEmpty() ? "No Article" : news[3] %></p>
                            <small class="text-muted">Posted on: <%= news[4].isEmpty() ? "Unknown" : news[4] %></small>
                            <div class="mt-2">
                                <a href="EditNewsServlet?newsId=<%= news[5] %>" class="btn btn-edit btn-sm me-2">Edit</a>
                                <a href="DeleteNewsServlet?newsId=<%= news[5] %>" class="btn btn-delete btn-sm" 
                                   onclick="return confirm('Are you sure you want to delete this news?');">Delete</a>
                            </div>
                        </div>
                    </div>
                <% } %>
            <% } %>

            <a href="ProfileServlet" class="btn btn-back mt-3 w-100">Back to Profile</a>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>