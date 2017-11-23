import java.io.IOException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class DataSenderRunnable implements Runnable {

    private Socket socket;
    private Scanner userIn;

    public DataSenderRunnable(Socket socket){
        this.socket = socket;
        this.userIn = new Scanner(System.in);
    }

    @Override
    public void run() {
//
//
//
//        boolean loginSuccesfull = false;
//        System.out.println("Enter a UserName: ");
//        //String username = userIn.nextLine();
////        try {
////            socket.close();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        while (!loginSuccesfull) {
//            //sendMessage("HELO " + username);
////            try {
////                wait(2000);
////            } catch (InterruptedException ie) {
////                ie.printStackTrace();
////            }
//
//        }
//
////        boolean quit = false;
////        while(!quit){
////            String input = userIn.nextLine();
////            if(input.equalsIgnoreCase("Q")){
////                sendMessage("QUIT");
////                quit = true;
////            }else{
////                sendMessage("BCST "+input);
////            }
////        }
    }

    public void sendMessage(String message){
        try {
            OutputStream outputStream = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(message);
            writer.flush();
        }catch (IOException ioe) {
            System.out.println("ERROR");
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}