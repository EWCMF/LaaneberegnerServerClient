
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class MultiThreadServer extends Application implements Serializable {
  // Text area for displaying contents
  private TextArea ta = new TextArea();
  
  // Number a client
  private int clientNo = 0;

  @Override // Override the start method in the Application class
  public void start(Stage primaryStage) {
    // Create a scene and place it in the stage
    Scene scene = new Scene(new ScrollPane(ta), ta.getPrefWidth(), 200);
    ta.setEditable(false);
    primaryStage.setTitle("MultiThreadServer"); // Set the stage title
    primaryStage.setScene(scene); // Place the scene in the stage
    primaryStage.show(); // Display the stage

    new Thread( () -> {
      try {
        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(8000);
        ta.appendText("MultiThreadServer started at " 
          + new Date() + '\n');
    
        while (true) {
          // Listen for a new connection request
          Socket socket = serverSocket.accept();
    
          // Increment clientNo
          clientNo++;
          
          Platform.runLater( () -> {
            // Display the client number
            ta.appendText("Starting thread for client " + clientNo +
              " at " + new Date() + '\n');

            // Find the client's host name, and IP address
            InetAddress inetAddress = socket.getInetAddress();
            ta.appendText("Client " + clientNo + "'s host name is "
              + inetAddress.getHostName() + "\n");
            ta.appendText("Client " + clientNo + "'s IP Address is "
              + inetAddress.getHostAddress() + "\n");
          });
          
          // Create and start a new thread for the connection
          new Thread(new HandleAClient(socket)).start();
        }
      }
      catch(IOException ex) {
        System.err.println(ex);
      }
    }).start();
  }
  
  // Define the thread class for handling new connection
  class HandleAClient implements Runnable {
    private Socket socket; // A connected socket

    /** Construct a thread */
    public HandleAClient(Socket socket) {
      this.socket = socket;
    }

    /** Run a thread */
    public void run() {
      try {
        // Create data input and output streams
        DataInputStream inputFromClient = new DataInputStream(
          socket.getInputStream());
        DataOutputStream outputToClient = new DataOutputStream(
          socket.getOutputStream());

        // Continuously serve the client
        while (true) {

          // Få data fra klienten.
          double annualInterestRate = inputFromClient.readDouble();
          int numberOfYears = inputFromClient.readInt();
          double loanAmount = inputFromClient.readDouble();

          // Udregninger til lån
          double monthlyInterestRate = annualInterestRate / 100 / 12;
          double monthlyPayment = loanAmount * monthlyInterestRate / (
                  1 - 1 / Math.pow(1 + monthlyInterestRate, numberOfYears * 12));
          double totalPayment = monthlyPayment * numberOfYears * 12;

          outputToClient.writeDouble(monthlyPayment);
          outputToClient.writeDouble(totalPayment);

          // Formater til 2 decimaler.
          DecimalFormat decimalFormat = new DecimalFormat("#.00");
          String formattedMonthlyPayment = decimalFormat.format(monthlyPayment);
          String formattedTotalPayment = decimalFormat.format(totalPayment);

          Platform.runLater(() -> {
            ta.appendText("Received from client: \n");
            ta.appendText("Annual Interest Rate: " + annualInterestRate + "\n" );
            ta.appendText("Number of Years: " + numberOfYears + "\n");
            ta.appendText("Loan Amount: " + loanAmount + "\n\n");
            ta.appendText("Monthly payment found: " + formattedMonthlyPayment + "\n");
            ta.appendText("Total payment found: " + formattedTotalPayment + "\n");
          });
        }
      }
      catch(IOException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * The main method is only needed for the IDE with limited
   * JavaFX support. Not needed for running from the command line.
   */
  public static void main(String[] args) {
    launch(args);
  }
}