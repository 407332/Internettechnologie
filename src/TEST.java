import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TEST {

    private int counter = 0;
    private Socket socket;

    public static void main(String[] args) throws IOException {
        new TEST().run();
    }

    public void run() throws IOException {
        while(true){
            try {
                this.socket = new Socket("192.168.1.100", 1337);
            } catch (IOException e) {
                e.printStackTrace();
            }

            OutputStream outputStream = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("HELO CRASH"+counter);
            writer.flush();
            counter++;
        }
    }
}
