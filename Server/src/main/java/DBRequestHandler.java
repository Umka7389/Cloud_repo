import java.sql.*;

public class DBRequestHandler {
    private static Connection connection;
    private static PreparedStatement preparedStatement;

    public static void getConnectionWithDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/users_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC", "root", "2easy4umka");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectDB() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfUserExistsForAuthorization(String login) {
        String dbQuery = "SELECT login FROM users_tbl";
        try {
            preparedStatement = connection.prepareStatement(dbQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("login").equals(login)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static boolean checkIfPasswordIsRight(String login, String password) {
        String dbQuery = "SELECT password FROM users_tbl WHERE login = ?";
        try {
            preparedStatement = connection.prepareStatement(dbQuery);
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String passwordFromDB = resultSet.getString("password");
                if (passwordFromDB.equals(password)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void registerNewUser(String login, String password) {
        String dbQuery = "INSERT INTO users_tbl (login,password) VALUES (?,?)";
        try {
            preparedStatement = connection.prepareStatement(dbQuery);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
