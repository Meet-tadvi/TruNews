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
    <title>Community News - News Detector</title>
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
        .news-card { 
            background-color: #333; 
            border: none; 
            margin-bottom: 20px; 
            border-radius: 8px; 
            color: #ffffff;
        }
        .news-card .card-body { padding: 15px; }
        .news-card .card-title { color: #ff4d4d; }
        .card-text, .text-muted, .alert { color: #ffffff !important; }
        .read-more, .read-less { color: #4d94ff; cursor: pointer; }
        .read-more:hover, .read-less:hover { color: #3366cc; }
        .collapse.show + .read-more { display: none; } /* Hide Read More when expanded */
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
            <h2 class="text-center">🌐 Community News</h2>
            <div class="p-4">
                <% String error = (String) request.getAttribute("error");
                   if (error != null) { %>
                    <div class="alert alert-danger">
                        <h4><%= error %></h4>
                    </div>
                <% } %>

                <% List<String[]> communityNews = (List<String[]>) request.getAttribute("communityNews");
                   if (communityNews == null || communityNews.isEmpty()) { %>
                    <p>No community news available.</p>
                <% } else { %>
                    <% int index = 0; %>
                    <% for (String[] news : communityNews) { %>
                        <div class="card news-card">
                            <div class="card-body">
                                <h5 class="card-title"><%= news[3] %></h5>
                                <p class="card-text"><strong>Posted by:</strong> <%= news[0] %></p>
                                <p class="card-text"><strong>Topic:</strong> <%= news[1] %></p>
                                <p class="card-text"><strong>Date:</strong> <%= news[2] %></p>
                                <p class="card-text">
                                    <strong>Article:</strong> 
                                    <% if (news[4].length() > 200) { %>
                                        <span id="short-<%= index %>"><%= news[4].substring(0, 200) + "..." %></span>
                                        <a class="read-more" data-bs-toggle="collapse" href="#full-<%= index %>" aria-expanded="false" aria-controls="full-<%= index %>">Read More</a>
                                        <span id="full-<%= index %>" class="collapse"><%= news[4] %>
                                            <br><a class="read-less" data-bs-toggle="collapse" href="#full-<%= index %>">Read Less</a>
                                        </span>
                                    <% } else { %>
                                        <%= news[4] %>
                                    <% } %>
                                </p>
                                <small class="text-muted">Posted on: <%= news[5] %></small>
                            </div>
                        </div>
                        <% index++; %>
                    <% } %>
                <% } %>

                <a href="dashboard.jsp" class="btn btn-secondary mt-3">Back to Dashboard</a>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>