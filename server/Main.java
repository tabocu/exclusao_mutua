import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length == 1) {
      new Server(Integer.parseInt(args[0])).run();
    } else {
      System.out.println("Invalid argument number: " + args.length);
    }
  }
}
