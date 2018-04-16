import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class udp extends Thread {

    DatagramSocket socket;
    int Port = 8000;
    DatagramPacket pack;

    public void run(){
        try {
            socket = new DatagramSocket(Port);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Boolean flag=true;
        byte[] buffer = new byte[1024];
        pack = new DatagramPacket(buffer, buffer.length);
        String read;
        while (flag){
            try {
                socket.receive(pack);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            read= new String (pack.getData());
            String []values= read.split(" ",3);
            String comm = values[0];
            switch (comm){
                case "borrow":
                    String output= BookServer.acess("borrow",values);
                    DatagramPacket returnpack = new DatagramPacket(output.getBytes(), output.getBytes().length, pack.getAddress(), pack.getPort());
                    try {
                        socket.send(returnpack);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case "return":
                    String output1= BookServer.acess("return",values);
                    DatagramPacket returnpack1 = new DatagramPacket(output1.getBytes(), output1.getBytes().length, pack.getAddress(), pack.getPort());
                    try {
                        socket.send(returnpack1);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case "list":
                    String output2= BookServer.acess("list",values);
                    DatagramPacket returnpack2 = new DatagramPacket(output2.getBytes(), output2.getBytes().length, pack.getAddress(), pack.getPort());
                    try {
                        socket.send(returnpack2);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case "inventory":
                    String output3 =(BookServer.acess("inventory",values));
                    DatagramPacket returnpack3 = new DatagramPacket(output3.getBytes(), output3.getBytes().length, pack.getAddress(), pack.getPort());
                    try {
                        socket.send(returnpack3);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case "exit":

                    flag =false;
                    BookServer.acess("exit",values);
                    break;
            }


        }
    }

}