<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login - TruNews</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { 
            background-color: #1a1a1a; 
            color: #ffffff; 
            margin: 0; 
            padding-top: 80px; /* Space for fixed navbar */
        }
        .login-container { 
            background-color: #2c2c2c; 
            padding: 30px; 
            border-radius: 8px; 
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.1); 
            width: 100%; 
            max-width: 400px; 
            margin: 20px auto;
        }
        .form-label { color: #ffffff; }
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
        .btn-login { 
            background-color: #4d94ff; 
            border: none; 
        }
        .btn-login:hover { 
            background-color: #3366cc; 
        }
        .navbar { 
            background-color: #333; 
            position: fixed; 
            top: 0; 
            width: 100%; 
            z-index: 1050;
        }
    </style>
    <script>
        function validateForm() {
            let email = document.getElementById("email").value;
            let password = document.getElementById("password").value;
            if (email === "" || password === "") {
                alert("Email and Password cannot be empty!");
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">📰 News Detector</a>
            <div class="ms-auto">
                <a href="signup.jsp" class="btn btn-success btn-sm">Sign Up</a>
            </div>
        </div>
    </nav>

    <div class="login-container">
        <h2 class="text-center">📰 User Login</h2>
        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-danger text-center">Invalid Email or Password!</div>
        <% } %>
        <form action="LoginServlet" method="post" onsubmit="return validateForm()">
            <div class="mb-3">
                <label for="email" class="form-label">Email:</label>
                <input type="email" id="email" name="email" class="form-control" required>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Password:</label>
                <input type="password" id="password" name="password" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-login w-100">Login</button>
        </form>
        <p class="text-center mt-3">
            Don't have an account? <a href="signup.jsp" class="text-info">Sign Up</a>
        </p>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>