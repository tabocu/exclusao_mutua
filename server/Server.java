import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

  private int mPort = -1;
  private List<Socket> mNodes;

  public Server(int port) {
    mPort = port;
    mNodes = new ArrayList<>();
  }

  public int getPort() {
    return mPort;
  }

  public void run() throws IOException {
    try (ServerSocket socketServer = new ServerSocket(mPort)){
      System.out.println("LOG: Server: " + mPort + " - Successfully connected!");

			while (true) {
				Socket socket = socketServer.accept();
				System.out.println("Nova conexão com o cliente " + socket.getInetAddress().getHostAddress());

				mNodes.add(socket);

        new Thread(new NodeHandler(socket, this)).start();
			}
		}
  }

  public void broadcast(Socket sourceSocket, String msg) {
		for (Socket socket : mNodes) {
			if (!socket.equals(sourceSocket)) {
				try {
					PrintStream printStream = new PrintStream(socket.getOutputStream());
					printStream.println(msg);
				} catch (IOException io) {
					io.printStackTrace();
				}
			}
		}
	}
}

class NodeHandler implements Runnable {

	private Socket mSocket;
	private Server mServer;

	public NodeHandler(Socket socket, Server server) {
    mSocket = socket;
    mServer = server;
	}

	public void run() {
		try (Scanner scanner = new Scanner(mSocket.getInputStream())) {
			while (scanner.hasNextLine()) {
				mServer.broadcast(mSocket, scanner.nextLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
