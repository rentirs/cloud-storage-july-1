import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private String root = "client/clientFiles";
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;

    public ListView<String> listView;
    public TextField statusBar;

    public void send() throws IOException {
        String fileName = listView.getSelectionModel().getSelectedItem();
        Path filePath = Paths.get(root, fileName);
        long size = Files.size(filePath);
        os.writeUTF(fileName);
        os.writeLong(size);
        Files.copy(filePath, os);
        os.flush();
        statusBar.setText("File: " + fileName + " sent to server");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[256];
        try {
            File dir = new File(root);
            listView.getItems().clear();
            listView.getItems().addAll(dir.list());
            Socket socket = new Socket("192.168.1.100", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String status = is.readUTF();
                        Platform.runLater(() -> {
                            statusBar.setText(status);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Exception while read");
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
