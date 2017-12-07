import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class UserInputRunnable implements Runnable {

    private Queue<String> messages = new PriorityQueue<>();
    private boolean kill;

    private Scanner userIn = new Scanner(System.in);

    @Override
    public void run() {
        while(!kill){
            messages.add(userIn.nextLine());
        }
    }

    public boolean hasMessages(){
        return messages.size() >0;
    }

    public String getNextMessage(){
        //System.out.println(messages.toString());
        return messages.poll();
    }

    public void kill(){
        userIn.close();
        kill = true;
    }
}
