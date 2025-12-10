<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpSession, java.util.List, java.util.ArrayList, java.sql.*, newsdetects.DBConnection" %>
<%
    HttpSession sess = request.getSession(false);
    if (sess == null || sess.getAttribute("user_id") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    int userId = (int) sess.getAttribute("user_id");
    List<String[]> history = new ArrayList<>();
    String historyError = null;
    try {
        Connection con = DBConnection.getConnection();
        String query = "SELECT news_text, result, submitted_at, model_type FROM news_history WHERE user_id = ? ORDER BY submitted_at DESC";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            history.add(new String[]{rs.getString("news_text"), rs.getString("result"), rs.getTimestamp("submitted_at").toString(), rs.getString("model_type")});
        }
        rs.close();
        ps.close();
        con.close();
    } catch (Exception e) {
        e.printStackTrace();
        historyError = "Failed to load history: " + e.getMessage();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>News Detection Dashboard</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { background-color: #1a1a1a; color: white; margin: 0; }
        .navbar { 
            background-color: #333; 
            position: fixed; 
            top: 10px; /* Margin from top */
            left: 10px; /* Margin from left */
            right: 10px; /* Margin from right */
            width: calc(100% - 20px); /* Adjusted for left and right margins */
            z-index: 1050;
            border-radius: 8px; /* Optional: slight rounding for aesthetics */
        }
        .container-box { 
            background-color: #2c2c2c; 
            padding: 20px; 
            border-radius: 8px; 
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.1); 
            margin-top: 80px; /* Space for navbar and history button */
        }
        .btn-check-news { 
            background-color: #ff4d4d; 
            border: none; 
            margin: 0 auto; /* Center horizontally */
            display: block; /* Ensure block-level for centering */
        }
        .btn-check-news:hover { background-color: #cc0000; }
        .news-list li { margin-bottom: 8px; }
        .feedback-btn { position: fixed; bottom: 20px; right: 20px; z-index: 1000; }
        .sidebar {
            height: calc(100% - 70px); /* Adjusted for navbar and history button */
            width: 0;
            position: fixed;
            z-index: 1000;
            top: 70px; /* Below navbar and history button */
            left: 0;
            background-color: #333;
            overflow-x: hidden;
            transition: width 0.5s;
        }
        .sidebar a { padding: 8px 8px 8px 32px; text-decoration: none; font-size: 18px; color: #818181; display: block; transition: 0.3s; }
        .sidebar a:hover { color: #f1f1f1; }
        .sidebar .closebtn { position: absolute; top: 10px; right: 25px; font-size: 36px; }
        .openbtn { 
            font-size: 20px; 
            cursor: pointer; 
            background-color: #333; 
            color: white; 
            padding: 10px 15px; 
            border: none; 
            position: fixed; 
            top: 70px; /* Just below navbar (10px margin + ~50px navbar height) */
            left: 10px; /* Margin from left */
            z-index: 1100; 
            border-radius: 4px;
        }
        .openbtn:hover { background-color: #444; }
        #main { transition: margin-left 0.5s; padding: 16px; margin-top: 90px; /* Space for navbar and history button */ }
        .history-item { padding: 10px; border-bottom: 1px solid #555; }
        .history-item small { color: #bbb; }
        .spinner-container { display: none; text-align: center; margin-top: 10px; }
    </style>
</head>
<body>
    <div id="mySidebar" class="sidebar">
        <a href="javascript:void(0)" class="closebtn" onclick="closeNav()">×</a>
        <h3 class="px-4">News History</h3>
        <% if (historyError != null) { %>
            <div class="alert alert-danger mx-3">
                <%= historyError %>
            </div>
        <% } else if (history.isEmpty()) { %>
            <p class="px-4">No history available.</p>
        <% } else { %>
            <% for (String[] entry : history) { %>
                <div class="history-item">
                    <p><%= entry[0].length() > 50 ? entry[0].substring(0, 50) + "..." : entry[0] %></p>
                    <small>Result: <%= entry[1] %> | Model: <%= entry[3] %> | <%= entry[2] %></small>
                </div>
            <% } %>
        <% } %>
    </div>

    <div id="main">
        <button class="openbtn" onclick="openNav()">☰ History</button>
        <nav class="navbar navbar-expand-lg navbar-dark">
            <div class="container-fluid">
                <a class="navbar-brand" href="#">📰 News Detector</a>
                <div class="ms-auto">
                    <span class="text-light me-3">Welcome, <%= sess.getAttribute("email") %>!</span>
                    <a href="CommunityNewsServlet" class="btn btn-secondary btn-sm me-2">Community News</a>
                    <a href="ProfileServlet" class="btn btn-secondary btn-sm me-2">Profile</a>
                    <a href="LogoutServlet" class="btn btn-warning btn-sm">Logout</a>
                </div>
            </div>
        </nav>

        <div class="container mt-5">
            <div class="container-box">
                <h2 class="text-center">🔍 Check Fake News</h2>
                <form id="newsForm" action="DetectServlet" method="post" class="p-4">
                    <label>Enter News Article:</label>
                    <textarea name="newsText" class="form-control" rows="3" required style="resize: vertical; max-height: 100px;"></textarea>
                    <label class="mt-3">Select Model:</label>
                    <select name="modelType" class="form-select mb-3">
                        <option value="newsapi">NewsAPI Verifier</option>
                        <option value="ml">ML Model (Logistic Regression)</option>
                    </select>
                    <button type="submit" class="btn btn-check-news btn-sm mt-3" id="submitBtn" style="width: 150px;">Check News</button>
                    <div class="spinner-container" id="spinner">
                        <div class="spinner-border text-light" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <div class="container mt-4">
            <% String error = (String) request.getAttribute("error");
               if (error != null) { %>
                <div class="alert alert-danger">
                    <h4>Error: <%= error %></h4>
                </div>
            <% } %>
            <% String prediction = (String) request.getAttribute("prediction");
               String articleLink = (String) request.getAttribute("articleLink");
               if (prediction != null) { %>
                <div class="alert <%= prediction.contains("true") ? "alert-success" : prediction.contains("Unverified") ? "alert-warning" : "alert-danger" %>">
                    <h4>Result: <%= prediction %></h4>
                    <% if (articleLink != null && !articleLink.isEmpty()) { %>
                        <p>Related Article: <a href="<%= articleLink %>" target="_blank">Read more</a></p>
                    <% } %>
                </div>
            <% } %>
            <% String feedbackSuccess = (String) request.getAttribute("feedbackSuccess");
               if (feedbackSuccess != null) { %>
                <div class="alert alert-success">
                    <h4><%= feedbackSuccess %></h4>
                </div>
            <% } %>
            <% String feedbackError = (String) request.getAttribute("feedbackError");
               if (feedbackError != null) { %>
                <div class="alert alert-danger">
                    <h4><%= feedbackError %></h4>
                </div>
            <% } %>
        </div>

        <div class="container mt-4">
            <h4>📰 Latest News from Trusted Sources:</h4>
            <ul class="news-list">
                <% List<String> news = (List<String>) request.getAttribute("latestNews");
                   if (news != null && !news.isEmpty()) {
                       for (String headline : news) { %>
                           <li>🔹 <%= headline %></li>
                <%    }
                   } else { %>
                       <li>No news available.</li>
                <% } %>
            </ul>
        </div>

        <!-- Feedback Button -->
        <button type="button" class="btn btn-primary feedback-btn" data-bs-toggle="modal" data-bs-target="#feedbackModal">
            Feedback
        </button>

        <!-- Feedback Modal -->
        <div class="modal fade" id="feedbackModal" tabindex="-1" aria-labelledby="feedbackModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content bg-dark text-light">
                    <div class="modal-header">
                        <h5 class="modal-title" id="feedbackModalLabel">Submit Feedback</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form action="FeedbackServlet" method="post">
                        <div class="modal-body">
                            <div class="mb-3">
                                <label for="userEmail" class="form-label">Your Email:</label>
                                <input type="email" class="form-control" id="userEmail" name="userEmail" value="<%= sess.getAttribute("email") %>" readonly>
                            </div>
                            <div class="mb-3">
                                <label for="feedbackText" class="form-label">Your Feedback:</label>
                                <textarea class="form-control" id="feedbackText" name="feedbackText" rows="4" required></textarea>
                            </div>
                            <div class="mb-3">
                                <label for="rating" class="form-label">Rating (1-5):</label>
                                <select class="form-select" id="rating" name="rating" required>
                                    <option value="">Select a rating</option>
                                    <option value="1">1 - Poor</option>
                                    <option value="2">2 - Fair</option>
                                    <option value="3">3 - Good</option>
                                    <option value="4">4 - Very Good</option>
                                    <option value="5">5 - Excellent</option>
                                </select>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                            <button type="submit" class="btn btn-primary">Submit</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function openNav() {
            console.log("Opening sidebar");
            document.getElementById("mySidebar").style.width = "300px";
            document.getElementById("main").style.marginLeft = "300px";
        }

        function closeNav() {
            console.log("Closing sidebar");
            document.getElementById("mySidebar").style.width = "0";
            document.getElementById("main").style.marginLeft = "0";
        }

        document.getElementById("newsForm").addEventListener("submit", function() {
            console.log("Form submitted");
            document.getElementById("submitBtn").disabled = true;
            document.getElementById("spinner").style.display = "block";
        });

        window.onload = function() {
            if (!document.getElementById("mySidebar")) {
                console.error("Sidebar element not found");
            }
            if (!document.getElementById("main")) {
                console.error("Main element not found");
            }
            if (!document.querySelector(".openbtn")) {
                console.error("Open button not found");
            }
        };
    </script>
</body>
</html>