import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ChatClient <server address> <port number>");
            return;
        }

        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {

            // Prompt the user to enter a nickname
            System.out.print("Enter your nickname: ");
            String nickname = consoleIn.readLine();
            out.println(nickname);

            // Start a separate thread to handle incoming messages from the server
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Server error: " + e.getMessage());
                }
            });

            // Main thread handles user input and sends messages to the server
            String userInput;
            while ((userInput = consoleIn.readLine()) != null) {
                out.println(userInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
