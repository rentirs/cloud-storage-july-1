package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Server {

    private ServerSocketChannel sc;
    private Selector selector;

    public Server() throws IOException {
        sc = ServerSocketChannel.open();
        selector = Selector.open();
        sc.bind(new InetSocketAddress(8189));
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_ACCEPT);
        while (sc.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        SocketChannel channel = (SocketChannel) key.channel();
        int read;
        StringBuilder sb = new StringBuilder();
        while (true) {
            read = channel.read(buffer);
            buffer.flip();
            if (read == -1) {
                channel.close();
                break;
            }
            if (read > 0) {
                while (buffer.hasRemaining()) {
                    sb.append((char) buffer.get());
                }
                buffer.clear();
            } else {
                break;
            }
        }
        System.out.println("Received: " + sb);
        for (SelectionKey selectionKey : selector.keys()) {
            if (selectionKey.isValid() && selectionKey.channel() instanceof SocketChannel) {
                SocketChannel ch = (SocketChannel) selectionKey.channel();
                //отбросим 2 ненужных для сравнения символа: 13 - возврат каретки и 10 - перевод строки
                sb.setLength(sb.length() - 2);
                if (sb.toString().equals("ls")) {
                    System.out.println("Send list of directories and files");
                    Path root = Paths.get("server", "serverFiles");
                    List<Path> paths = Files.walk(root)
                            .collect(Collectors.toList());
                    ch.write(ByteBuffer.wrap(("\033c" + "List of directories and files on the server:\n").getBytes(StandardCharsets.UTF_8)));
                    for (Path path : paths) {
                        ch.write(ByteBuffer.wrap((path + "\n").getBytes(StandardCharsets.UTF_8)));
                    }
                } else if (sb.toString().startsWith("cat")) {
                    String fileName = sb.toString().replace("cat ", "");
                    try {
                        ch.write(ByteBuffer.wrap(("\033c" + "File " + fileName + " contents:\n").getBytes(StandardCharsets.UTF_8)));
                        String pathFile = "server/serverFiles/" + fileName;
                        String content = new String(Files.readAllBytes(Paths.get(pathFile)));
                        ch.write(ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8)));
                    } catch (NoSuchFileException e) {
                        ch.write(ByteBuffer.wrap(("Error! File not found." + "\n").getBytes(StandardCharsets.UTF_8)));
                    }
                } else
                    ch.write(ByteBuffer.wrap(("From server: " + sb + "\n").getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = sc.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);

    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
