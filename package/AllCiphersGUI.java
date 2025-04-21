import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;

public class AllCiphersGUI extends Application {

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";  // Adjust with your DB connection details
    private static final String DB_USER = "yuva";
    private static final String DB_PASSWORD = "yuva";

    private TextField txtInput;
    private TextField txtKey;
    private TextArea txtEncrypted;
    private TextArea txtDecrypted;
    private ChoiceBox<String> cipherChoice;

    @Override
    public void start(Stage primaryStage) {
        // UI Elements
        Label lblInput = new Label("Enter Text:");
        txtInput = new TextField();

        Label lblKey = new Label("Enter Key (Number for Caesar, Text for XOR, blank for ROT13):");
        txtKey = new TextField();

        Label lblMethod = new Label("Select Cipher Method:");
        cipherChoice = new ChoiceBox<>();
        cipherChoice.getItems().addAll("Caesar", "XOR", "ROT13");

        Button btnEncrypt = new Button("Encrypt & Decrypt");
        btnEncrypt.setOnAction(e -> handleEncryption());

        Label lblEncrypted = new Label("Encrypted Text:");
        txtEncrypted = new TextArea();
        txtEncrypted.setEditable(false);

        Label lblDecrypted = new Label("Decrypted Text:");
        txtDecrypted = new TextArea();
        txtDecrypted.setEditable(false);

        // Layout
        VBox root = new VBox(10, lblInput, txtInput, lblKey, txtKey, lblMethod, cipherChoice, btnEncrypt, lblEncrypted, txtEncrypted, lblDecrypted, txtDecrypted);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");

        // Animation effect
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            root.setStyle("-fx-background-color: #f0f0f0;");
        });

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cipher GUI - Caesar, XOR, ROT13");
        primaryStage.show();

        // Initialize database
        initializeDatabase();
    }

    private void handleEncryption() {
        String input = txtInput.getText();
        String key = txtKey.getText();
        String method = cipherChoice.getValue();

        if (input.isEmpty()) {
            txtEncrypted.setText("Enter text to encrypt.");
            txtDecrypted.setText("");
            return;
        }

        String encrypted = "", decrypted = "";

        try {
            switch (method) {
                case "Caesar":
                    int shift = Integer.parseInt(key);
                    encrypted = caesarEncrypt(input, shift);
                    decrypted = caesarDecrypt(encrypted, shift);
                    break;
                case "XOR":
                    if (key.isEmpty()) throw new Exception("Key required for XOR");
                    encrypted = xorCipher(input, key);
                    decrypted = xorCipher(encrypted, key);
                    break;
                case "ROT13":
                    encrypted = rot13(input);
                    decrypted = rot13(encrypted);
                    break;
            }
        } catch (Exception ex) {
            encrypted = "Error: " + ex.getMessage();
            decrypted = "";
        }

        txtEncrypted.setText(encrypted);
        txtDecrypted.setText(decrypted);

        // Store the data in database
        storeDataInDatabase(input, method, decrypted);
    }

    private String caesarEncrypt(String text, int shift) {
        text = text.toLowerCase();
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            int pos = "abcdefghijklmnopqrstuvwxyz".indexOf(ch);
            if (pos != -1) {
                int newPos = (pos + shift) % 26;
                result.append("abcdefghijklmnopqrstuvwxyz".charAt(newPos));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private String caesarDecrypt(String text, int shift) {
        return caesarEncrypt(text, 26 - (shift % 26));
    }

    private String xorCipher(String text, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append((char) (text.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    private String rot13(String input) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (ch >= 'a' && ch <= 'z') {
                result.append((char) ('a' + (ch - 'a' + 13) % 26));
            } else if (ch >= 'A' && ch <= 'Z') {
                result.append((char) ('A' + (ch - 'A' + 13) % 26));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    // Initialize Database
    private void initializeDatabase() {
        Connection connection = null;
        Statement statement = null;

        try {
            // Connect to the Oracle database
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            statement = connection.createStatement();

            String createTableSQL = "BEGIN "
                    + "EXECUTE IMMEDIATE 'CREATE TABLE cipher_data ("
                    + "id NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "input_text VARCHAR2(255), "
                    + "method VARCHAR2(50), "
                    + "decrypted_text VARCHAR2(255))';"
                    + "EXCEPTION "
                    + "WHEN OTHERS THEN "
                    + "IF SQLCODE != -955 THEN "
                    + "RAISE; "
                    + "END IF; "
                    + "END;";
            statement.executeUpdate(createTableSQL);

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Store Data in Database
    private void storeDataInDatabase(String inputText, String method, String decryptedText) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Connect to the Oracle database
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String insertSQL = "INSERT INTO cipher_data (input_text, method, decrypted_text) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, inputText);
            preparedStatement.setString(2, method);
            preparedStatement.setString(3, decryptedText);

            preparedStatement.executeUpdate();
            System.out.println("Data stored successfully in the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
