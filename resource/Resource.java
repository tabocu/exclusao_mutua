import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Resource {

  private int mPort = -1;

  public Resource(int port) {
    mPort = port;
  }

  public void run() throws IOException {
    try (ServerSocket socketServer = new ServerSocket(mPort)){
      System.out.println("LOG: Server: " + mPort + " - Successfully connected!");

      Socket socket = null;
			while ((socket = socketServer.accept()) != null) {
				System.out.println("Nova conexão com o cliente " + socket.getInetAddress().getHostAddress());

        InputStream stream = socket.getInputStream();
        new Thread(() -> {
          try (Scanner scanner = new Scanner(stream)) {
			      while (scanner.hasNextLine()) {
              System.out.println(scanner.nextLine());
			      }
      		}
        }).start();
			}
		}
  }
}
