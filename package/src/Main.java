import java.awt.*;
import java.awt.event.*;

class AllCiphersGUI extends Frame implements ActionListener {
    Label lblInput, lblKey, lblMethod, lblEncrypted, lblDecrypted;
    TextField txtInput, txtKey;
    TextArea txtEncrypted, txtDecrypted;
    Choice cipherChoice;
    Button btnEncrypt;

    public AllCiphersGUI() {
        setTitle("Cipher GUI - Caesar, XOR, ROT13");
        setSize(450, 450);
        setLayout(new FlowLayout());

        lblInput = new Label("Enter Text:");
        txtInput = new TextField(30);

        lblKey = new Label("Enter Key (Number for Caesar, Text for XOR, blank for ROT13):");
        txtKey = new TextField(20);

        lblMethod = new Label("Select Cipher Method:");
        cipherChoice = new Choice();
        cipherChoice.add("Caesar");
        cipherChoice.add("XOR");
        cipherChoice.add("ROT13");

        btnEncrypt = new Button("Encrypt & Decrypt");
        btnEncrypt.addActionListener(this);

        lblEncrypted = new Label("Encrypted Text:");
        txtEncrypted = new TextArea(2, 35);
        txtEncrypted.setEditable(false);

        lblDecrypted = new Label("Decrypted Text:");
        txtDecrypted = new TextArea(2, 35);
        txtDecrypted.setEditable(false);

        add(lblInput);
        add(txtInput);
        add(lblKey);
        add(txtKey);
        add(lblMethod);
        add(cipherChoice);
        add(btnEncrypt);
        add(lblEncrypted);
        add(txtEncrypted);
        add(lblDecrypted);
        add(txtDecrypted);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });
    }

    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    // Caesar Cipher
    public String caesarEncrypt(String text, int shift) {
        text = text.toLowerCase();
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            int pos = ALPHABET.indexOf(ch);
            if (pos != -1) {
                int newPos = (pos + shift) % 26;
                result.append(ALPHABET.charAt(newPos));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public String caesarDecrypt(String text, int shift) {
        return caesarEncrypt(text, 26 - (shift % 26));
    }

    // XOR Cipher
    public String xorCipher(String text, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append((char) (text.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }

    // ROT13 Cipher
    public String rot13(String input) {
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

    public void actionPerformed(ActionEvent e) {
        String input = txtInput.getText();
        String key = txtKey.getText();
        String method = cipherChoice.getSelectedItem();

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
    }

    public static void main(String[] args) {
        new AllCiphersGUI();
    }
}
