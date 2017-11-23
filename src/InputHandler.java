import java.io.*;
import java.net.Socket;

public class InputHandler implements Runnable {

    private Socket socket;
    private Thread out;

    public InputHandler(Socket socket, Thread out){
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();

            while (out.isAlive()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                String line = reader.readLine();
                System.out.println(line);
                if(!line.equals("+OK Goodbye") && !line.equals("HELO Welkom to WhatsUpp!")) {
                    System.out.println("type a message to Broadcast or Q to quit");
                }
            }
        }catch (IOException ioe){
            System.out.println(ioe.getStackTrace());
        }
    }
}
