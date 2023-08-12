package grabber;



import grabber.utils.HabrCareerDateTimeParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password"));
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream("src/main/resources/rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PsqlStore psqlStore = new PsqlStore(properties)) {
            Post post1 = new Post("Java-разработчик",
                    "https://career.habr.com/vacancies/1000126192",
                    "СберКорус — цифровая платформа для электронного документооборота.",
                    new HabrCareerDateTimeParser().parse("2023-08-11T14:27:31+03:00"));
            Post post2 = new Post("Java developer",
                    "https://career.habr.com/vacancies/1000121931",
                    "Мы ищем Java-разработчика в команду разработки",
                    new HabrCareerDateTimeParser().parse("2023-08-11T14:27:31+03:00"));
            Post post3 = new Post("Руководитель направления по JAVA-разработке",
                    "https://career.habr.com/vacancies/1000126192",
                    "СберКорус — цифровая платформа для электронного документооборота.",
                    new HabrCareerDateTimeParser().parse("2023-08-11T14:38:04+03:00"));
            psqlStore.save(post1);
            psqlStore.save(post2);
            psqlStore.save(post3);
            List<Post> list = psqlStore.getAll();
            Post post = psqlStore.findById(2);
        }  catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(
                "INSERT INTO post(name, text, link, created) values (?, ?, ?, ?)"
                        + "ON CONFLICT (link) DO NOTHING",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    post.setId(resultSet.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM Post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Post post = new Post();
                    executePost(post, resultSet);
                    list.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post post = new Post();
        try (PreparedStatement preparedStatement = cnn.prepareStatement(
                "SELECT * FROM Post WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    executePost(post, resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    private void executePost(Post post, ResultSet resultSet) throws SQLException {
        post.setId(resultSet.getInt("id"));
        post.setTitle(resultSet.getString("name"));
        post.setDescription(resultSet.getString("text"));
        post.setLink(resultSet.getString("link"));
        Timestamp timestamp = resultSet.getTimestamp("created");
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        post.setCreated(localDateTime);
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }
}
