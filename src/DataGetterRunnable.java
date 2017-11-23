import java.io.*;
import java.net.Socket;


public class DataGetterRunnable implements Runnable {

    private Socket socket;
    private DataSenderRunnable sender;

    private BufferedReader bufferedReader;

    public DataGetterRunnable(Socket socket, DataSenderRunnable sender) {
        this.socket = socket;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            while (true) {
                String line = bufferedReader.readLine();
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
