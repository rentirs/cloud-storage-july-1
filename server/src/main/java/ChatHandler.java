import java.io.*;
import java.net.Socket;

public class ChatHandler implements Runnable {

    private String root = "server/ServerFiles";
    private Socket socket;
    private byte[] buffer;
    private DataInputStream is;
    private DataOutputStream os;

    public ChatHandler(Socket socket) {
        this.socket = socket;
        buffer = new byte[256];
    }

    @Override
    public void run() {
        try {
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            while (true) {
                processFileMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processFileMessage() throws IOException {
        String fileName = is.readUTF();
        System.out.print("Received file: " + fileName);
        long size = is.readLong();
        System.out.println(" size: " + size + "B");
        try (FileOutputStream fos = new FileOutputStream(root + "/" + fileName)) {
            for (int i = 0; i < (size + 256) / 256; i++) {
                int read = is.read(buffer);
                fos.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        os.writeUTF("File: " + fileName + " received");
    }
}
