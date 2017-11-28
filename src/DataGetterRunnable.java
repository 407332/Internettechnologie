import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class DataGetterRunnable implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private Client client;

    private boolean waitingForData;
    private boolean kill;



    public DataGetterRunnable(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            while (!kill) {
                String line = bufferedReader.readLine();
                if(waitingForData){
                    client.confirmData(line);
                }
                System.out.println(line);
            }

        }catch(SocketException se){

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("killing getter");
    }

    public void setWaitingForData() {
        this.waitingForData = true;
    }

    public void kill(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.kill=true;
    }
}
