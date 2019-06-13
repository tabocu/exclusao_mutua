import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length == 3) {
      new Node(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2])).run();
    } else {
      System.out.println("Invalid number of arguments:" + args.length);
    }
  }
}
