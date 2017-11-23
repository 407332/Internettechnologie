import java.io.*;
import java.net.Socket;

public class Cient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("LOCALHOST", 1337);

        OutputHandler out =  new OutputHandler(socket);
        Thread outputThread = new Thread(out);

        InputHandler inputHandler = new InputHandler(socket,outputThread);
        Thread inputThread = new Thread(inputHandler);

        outputThread.start();
        inputThread.start();




//        MultiThreadSort multiThreadSort = new MultiThreadSort(list,threshold);
//        Thread multiThreadStart = new Thread(multiThreadSort);
//        multiThreadStart.start();
//        try {
//            multiThreadStart.join();
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        result = multiThreadSort.getList();

    }
}
