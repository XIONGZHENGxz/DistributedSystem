/*
Mohamed Nasreldin man2766
Hamza Ghani hhg263
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPThread extends Thread {
    private byte[] buf;
    private parseInventory inventory;
    private DatagramPacket datapacket;
    private DatagramSocket datasocket;
    private String order;


    public UDPThread(parseInventory inventory, DatagramSocket datasocket, String order, DatagramPacket datapacket){
        this.inventory = inventory;
        this.datasocket = datasocket;
        this.order = order;
        this.datapacket = datapacket;
    }

    public void run() {
        try {
            String result = inventory.Command(order);
            buf = result.getBytes();
            datasocket.send(new DatagramPacket(buf,buf.length,datapacket.getAddress(),datapacket.getPort()));
        } catch (IOException e) {}
    }
}