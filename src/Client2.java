import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.text.DecimalFormat;

public class Client2 extends Application implements Serializable {
    // IO streams
    DataOutputStream toServer = null;
    DataInputStream fromServer = null;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        GridPane gridPane = new GridPane();
        gridPane.setStyle("-fx-border-color: green");

        TextField tfForAnnualInterestRate = new TextField();

        tfForAnnualInterestRate.setMinWidth(250);
        TextField tfForNumOfYears = new TextField();
        TextField tfForLoanAmount = new TextField();

        Label labelForAnnual = new Label("Annual Interest Rate");
        Label labelForYears = new Label("Number Of Years");
        Label labelForAmount = new Label("Loan Amount");

        Button submitButton = new Button("Submit");

        gridPane.addColumn(0, labelForAnnual, labelForYears, labelForAmount);
        gridPane.addColumn(1, tfForAnnualInterestRate, tfForNumOfYears, tfForLoanAmount);
        gridPane.add(submitButton, 2, 1);
        gridPane.setHgap(8);

        BorderPane mainPane = new BorderPane();
        // Text area to display contents
        TextArea ta = new TextArea();
        mainPane.setCenter(new ScrollPane(ta));
        mainPane.setTop(gridPane);

        // Create a scene and place it in the stage
        Scene scene = new Scene(mainPane, 450, 200);
        primaryStage.setTitle("Client"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        submitButton.setOnAction(event -> {
            try {
                // Create a socket to connect to the server
                Socket socket = new Socket("localhost", 8000);
                // Socket socket = new Socket("130.254.204.36", 8000);
                // Socket socket = new Socket("drake.Armstrong.edu", 8000);

                // Create an input stream to receive data from the server
                fromServer = new DataInputStream(socket.getInputStream());

                // Create an output stream to send data to the server
                toServer = new DataOutputStream(socket.getOutputStream());

                double annualInterestRate = Double.parseDouble(tfForAnnualInterestRate.getText().trim());
                int numberOfYears = Integer.parseInt(tfForNumOfYears.getText().trim());
                double loanAmount = Double.parseDouble(tfForLoanAmount.getText().trim());

                toServer.writeDouble(annualInterestRate);
                toServer.writeInt(numberOfYears);
                toServer.writeDouble(loanAmount);
                toServer.flush();

                double monthlyPayment = fromServer.readDouble();
                double totalPayment = fromServer.readDouble();

                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                String formattedMonthlyPayment = decimalFormat.format(monthlyPayment);
                String formattedTotalPayment = decimalFormat.format(totalPayment);

                ta.appendText("Annual Interest Rate: " + annualInterestRate + "\n" );
                ta.appendText("Number of Years: " + numberOfYears + "\n");
                ta.appendText("Loan Amount: " + loanAmount + "\n\n");

                ta.appendText("Received from server: \n");
                ta.appendText("Monthly Payment: " + formattedMonthlyPayment + "\n");
                ta.appendText("Total Payment: " + formattedTotalPayment + "\n");
                
            } catch (IOException io) {
                System.err.println(io);
                ta.appendText(io.toString() + '\n');
            }
        });
    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static void main(String[] args) {
        launch(args);
    }
}