import java.io.IOException;
import java.io.PrintStream;
import java.lang.InterruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.HashMap;

public class Node {
  private int mId = 0;
  private boolean mInTheZone = false;
  private HashMap<Integer, Boolean> mIdMap;

  private String mHost;
  private int mPort, mResourcePort;

  private Socket mSocket = null;
  private Socket mResourceSocket = null;
  private Scanner mSocketScanner = null;
  private PrintStream mPrintStream = null;
  private PrintStream mResourceStream = null;

  private Read mRead = null;

  public Node(String host, int port, int resourcePort) {
    mHost = host;
    mPort = port;
    mResourcePort = resourcePort;
    mIdMap = new HashMap<>();
  }

  public void run() {
    init();

    new Thread(mRead).start();

    whoIsAlive();
    initProcess();
    initUser();
  }

  private void init() {
    try {
      mSocket = new Socket(mHost, mPort);
      mResourceSocket = new Socket(mHost, mResourcePort);
      mSocketScanner = new Scanner(mSocket.getInputStream());
      mPrintStream = new PrintStream(mSocket.getOutputStream());
      mResourceStream = new PrintStream(mResourceSocket.getOutputStream());
    } catch (UnknownHostException uh) {
      uh.printStackTrace();
      System.exit(1);
    } catch (IOException io) {
      io.printStackTrace();
      System.exit(1);
    }
    mRead = new Read(mSocketScanner);
    System.out.println("LOG: Successfully connected!");
  }

  private void whoIsAlive() {
    System.out.println("LOG: Acquiring new ID...");
    mPrintStream.println("WHOISALIVE");
    mRead.setReadable((String nextLine) -> {
      System.out.println("LOG: " + nextLine);
      if (nextLine.startsWith("ALIVE")) {
        int id = getAlive(nextLine) + 1;
        mId = id > mId ? id : mId;
      }
    });
    sleep();
    mRead.setReadable(null);
    System.out.println("LOG: New ID = " + mId);
    mPrintStream.println("ALIVE:" + mId);
  }

  private void initProcess() {
    System.out.println("LOG: Processing...");
    mRead.setReadable((String nextLine) -> {
      System.out.println("LOG: " + nextLine);
      if (nextLine.startsWith("ALIVE")) {
        int id = getAlive(nextLine);
      } else if (nextLine.startsWith("WHOISALIVE")) {
        mPrintStream.println("ALIVE:" + mId);
      } else if (nextLine.startsWith("USER")) {
        System.out.println("USER: " + getUserMsg(nextLine));
      } else if (nextLine.startsWith("EXIT")) {
        getExit(nextLine);
      } else if (nextLine.startsWith("ENTERZONE")) {
        mPrintStream.println("ZONE:" + mId + ":" + (mInTheZone ? "false" : "true"));
      } else if (nextLine.startsWith("ZONE")) {
        getZone(nextLine);
      }
    });
  }

  private void initUser() {
    System.out.println("LOG: Waiting user input...");
    Read userReader = new Read(new Scanner(System.in));
    new Thread(userReader).start();

    userReader.setReadable((String nextLine) -> {
      if (nextLine.equals("ENTERZONE") && !mInTheZone) {
        mIdMap.clear();
        mPrintStream.println("ENTERZONE:" + mId);
        sleep();
        if (canEnterTheZone()) {
          mInTheZone = true;
          System.out.println("LOG: Entering the zone...");
        } else {
          System.out.println("LOG: Zone denied!");
        }
      } else if (nextLine.equals("LEAVEZONE") && mInTheZone) {
        System.out.println("LOG: Leaving zone...");
        mInTheZone = false;
      } else if (nextLine.equals("EXIT")) {
        mPrintStream.println("EXIT:" + mId);
        System.exit(1);
      } else {
        if (mInTheZone)
          mResourceStream.println("USER-" + mId + ": " + nextLine);
        else
          mPrintStream.println("USER:" + nextLine);
      }
    });
  }

  private int getAlive(String alive) {
    int id = Integer.parseInt(alive.split(":")[1]);
    mIdMap.put(id, false);
    return id;
  }

  private int getExit(String exit) {
    int id = Integer.parseInt(exit.split(":")[1]);
    mIdMap.remove(id);
    return id;
  }

  private String getUserMsg(String userMsg) {
    return userMsg.split(":")[1];
  }

  private void getZone(String zone) {
    String[] param = zone.split(":");
    int id = Integer.parseInt(param[1]);
    boolean allow = Boolean.parseBoolean(param[2]);
    mIdMap.put(id, allow);
  }

  private void sleep() {
    try { Thread.sleep(2000); } catch (InterruptedException e) {}
  }

  private boolean canEnterTheZone() {
    boolean canEnter = true;
    for (HashMap.Entry elem : mIdMap.entrySet()) {
      canEnter &= (boolean) elem.getValue();
    }
    return canEnter;
  }
}

class Read implements Runnable {
  private Readable mReadable = null;
  private Scanner mScanner = null;

  public Read(Scanner scanner) {
    mScanner = scanner;
  }

  public void setReadable(Readable readable) {
    mReadable = readable;
  }

  public void run() {
    while (mScanner.hasNextLine())
      if (mReadable != null) mReadable.read(mScanner.nextLine());
  }
}

interface Readable { public void read(String string); }
