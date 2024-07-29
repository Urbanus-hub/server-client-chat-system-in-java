import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345; // Change to an appropriate port if needed
    private static Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String nickname = null;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientWriters.add(out);

                nickname = in.readLine();
                broadcastMessage(nickname + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(nickname + ": " + message);
                }
            } catch (IOException e) {
                System.out.println("Client error: " + e.getMessage());
            } finally {
                try {
                    if (nickname != null) {
                        broadcastMessage(nickname + " has left the chat.");
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientWriters.remove(out);
            }
        }

        private void broadcastMessage(String message) {
            System.out.println("Broadcasting: " + message);
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }
}
