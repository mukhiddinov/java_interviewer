package com.example.telegrambot.service;

import com.example.telegrambot.database.DatabaseUtil;
import com.example.telegrambot.model.Question;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    public static Integer addQuestion(String module, String question, String answer, long chatId) {
        String sql = "INSERT INTO questions (modul_id, question, answer) VALUES ((SELECT id FROM moduls WHERE name = ?), ?, ?)";
        Integer result = 0;
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, module);
            stmt.setString(2, question);
            stmt.setString(3, answer);
            int affectedRows = stmt.executeUpdate();
            result = affectedRows;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Question> getQuestionsByModule(String moduleName) {
        List<Question> questions = new ArrayList<>();
        String selectQuestionsSQL = "SELECT q.id, q.question, q.answer FROM questions q JOIN moduls m ON q.modul_id = m.id WHERE m.name = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuestionsSQL)) {
            preparedStatement.setString(1, moduleName);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Question question = new Question();
                question.setId(resultSet.getInt("id"));
                question.setQuestion(resultSet.getString("question"));
                question.setAnswer(resultSet.getString("answer"));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public static List<String> getAllModuleNames() {
        List<String> modules = new ArrayList<>();
        String selectModulesSQL = "SELECT name FROM moduls";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectModulesSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                modules.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public static List<String> getRandomQuestions(String moduleName) {
        List<String> questions = new ArrayList<>();
        String selectQuestionsSQL = "SELECT question FROM questions WHERE modul_id = (SELECT id FROM moduls WHERE name = ?) ORDER BY RANDOM() LIMIT 10";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuestionsSQL)) {
            preparedStatement.setString(1, moduleName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    questions.add(resultSet.getString("question"));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("questions" + questions);
        return questions;
    }

    public static int getIdByQuestion(String question) {
        String sql = "SELECT id FROM questions WHERE question = ?";
        int questionId = -1;

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, question);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    questionId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questionId;
    }


    public static String getAnswerById(Integer questionId) {
        String sql = "SELECT answer FROM questions WHERE id = ?";
        String answer = null;

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, questionId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    answer = resultSet.getString("answer");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answer;
    }



}
