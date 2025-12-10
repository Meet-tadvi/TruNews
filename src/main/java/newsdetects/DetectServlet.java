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
import java.util.List;

@WebServlet("/DetectServlet")
public class DetectServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        try {
            DBConnection.init(getServletContext());
            NewsScraper.init(getServletContext());
            MLNewsVerifier.init(getServletContext());
        } catch (Exception e) {
            System.err.println("Initialization failed: " + e.getMessage());
            getServletContext().setAttribute("newsScraperInitError", "Failed to initialize components: " + e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int userId = (int) session.getAttribute("user_id");
        String newsText = request.getParameter("newsText");
        String modelType = request.getParameter("modelType");

        if (newsText == null || newsText.trim().isEmpty()) {
            request.setAttribute("error", "Please enter news text.");
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            return;
        }

        if (!NewsScraper.isInitialized() || !MLNewsVerifier.isInitialized()) {
            String initError = (String) getServletContext().getAttribute("newsScraperInitError");
            request.setAttribute("error", initError != null ? initError : "Components not initialized");
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            return;
        }

        List<NewsScraper.Article> toiNews;
        List<String> newsTitles;
        try {
            toiNews = NewsScraper.fetchTimesOfIndiaNews(newsText);
            newsTitles = toiNews.stream().map(article -> article.title).toList();
        } catch (IOException e) {
            request.setAttribute("error", "Failed to fetch news articles: " + e.getMessage());
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            return;
        }

        String resultMessage;
        String result;
        String articleLink;
        String selectedModel;

        try {
            if ("ml".equalsIgnoreCase(modelType)) {
                MLNewsVerifier.VerificationResult mlResult = MLNewsVerifier.verifyNews(newsText, getServletContext());
                resultMessage = mlResult.prediction.equals("REAL")
                        ? "✅ This news appears to be true."
                        : "❌ This news is likely false.";
                result = mlResult.prediction.equals("REAL") ? "True" : "False";
                articleLink = "";
                selectedModel = "MLModel";
            } else {
                NewsVerifier verifier = new NewsVerifier();
                NewsVerifier.VerificationResult verificationResult = verifier.verifyNews(newsText);
                if (verificationResult.message != null) {
                    resultMessage = verificationResult.message;
                    result = "Unverified";
                } else if (verificationResult.isReal) {
                    resultMessage = "✅ This news appears to be true.";
                    result = "True";
                } else {
                    resultMessage = "❌ This news is likely false.";
                    result = "False";
                }
                articleLink = verificationResult.articleLink;
                selectedModel = "NewsAPIVerifier";
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to verify news: " + e.getMessage());
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            String query = "INSERT INTO news_history (user_id, news_text, result, model_type) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setString(2, newsText);
            ps.setString(3, result);
            ps.setString(4, selectedModel);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Failed to save news history: " + e.getMessage());
        }

        request.setAttribute("prediction", resultMessage);
        request.setAttribute("articleLink", articleLink);
        request.setAttribute("latestNews", newsTitles);
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }
}
