import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class OutputHandler implements Runnable {

    private Socket socket;

    public OutputHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a UserName: ");
        String username = reader.nextLine();

        sendMessage("HELO "+username);
        boolean quit = false;
        while(!quit){
            String input = reader.nextLine();
            if(input.equalsIgnoreCase("Q")){
                sendMessage("QUIT");
                quit = true;
            }else{
                sendMessage("BCST "+input);
            }
        }
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
}
