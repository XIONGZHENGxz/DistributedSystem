
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import  java.io.InputStreamReader;
import java.util.Arrays;


public class Server implements  Runnable {

    Socket tcp;
    DatagramSocket udp;
    int mode;

    @Override
    public void run() {
        System.out.println("thread started");
        mode = 0;
        if(mode == 0){       // TCP

            PrintWriter out;
            BufferedReader reader;
            int flag =0; //flag will be set to 1 when  exited
            try{
                out = new PrintWriter(tcp.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(
                        tcp.getInputStream()));
                while(flag==0){
                    System.out.println("looping");
                    String read = reader.readLine();
                    String [] values= read.split(" ", 3);
                    System.out.println(Arrays.toString(values));
                    String first = values[0];
                    switch (first){
                        case "borrow":
                            String output= BookServer.acess("borrow",values);
                            System.out.println("borriwng");
                            out.println(output);
                            out.flush();
                            System.out.println("flushed");
                            break;
                        case "return":
                            out.println(BookServer.acess("return",values));
                            out.flush();
                            break;
                        case "list":
                            out.println(BookServer.acess("list",values));
                            out.flush();
                            break;
                        case "inventory":
                            out.println(BookServer.acess("inventory",values));
                            out.flush();
                             break;
                        case "exit":
                            flag =1;
                            BookServer.acess("exit",values);
                            out.flush();
                            break;
                    }


                }


            }
            catch (Exception E){
                System.out.print("you screwed up");

            }








        }



        else{    // Datagram socket    // to do : Finish the acess : Testing
                                        // Need to finish exit
                                        // Get the file hashmap to work : done
                                        // get datagram socket to work : need to work on that
                                        // Figure out the client
                                        // Test the client server communication
                                        // Test the transition between TCP and UDP protocol
                                        // atomic integer

        }

    }


    public Server(Socket tcp, int mode) { // lol should be 0
        this.tcp = tcp;
        this.udp = udp;
        this.mode = mode;
    }

}