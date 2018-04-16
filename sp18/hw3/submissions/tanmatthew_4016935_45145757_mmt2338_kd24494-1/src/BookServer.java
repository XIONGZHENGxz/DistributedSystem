import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BookServer {
    public static HashMap<String, Integer> files=new HashMap<>();
    public static HashMap<Integer, String> idvalues= new HashMap<>();
    public static HashMap<Integer, datarec> idvalu= new HashMap<>();
    public static HashMap<String, Set<String>> studentrec= new HashMap<>();
    public static HashMap<String, Set<datarec2>> Nameid=new HashMap<>();
    public static AtomicInteger records = new AtomicInteger(0);


    public static class datarec{
        public  String Borrowername; // name of the person
        public  String Bookname;          // book name
        public datarec(String Borrow, String book){
            Borrowername=Borrow;


        }

    }
    public static class datarec2{
        public  Integer record; // name of the person
        public  String Bookname;          // book name
        public datarec2(Integer record, String bookname ){
            this.record=record;
            Bookname= bookname;
        }

    }

    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }

       // String fileName = args[0];
        File CP_file = new File(args[0]);
        try{
            Scanner xy = new Scanner(CP_file);
            while (xy.hasNextLine()){
                String curr= xy.nextLine();
                int index = curr.indexOf('\"', 1);
                String two = curr.substring(index+1);
                two=two.trim();
              //  String[]h = curr.split(" \" ");
                String key = curr.substring(0,index+1);
                Integer value= Integer.parseInt(two);
                files.put(key,value);

            }    //creates a map
        }
         catch (Exception e){


        }

        tcpPort = 7000;
        ServerSocket sock1=null;
        udpPort = 8000;
        try{   // establishing primary connection/
            //initialize UDP port
            try {
                sock1 = new ServerSocket(tcpPort);


            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            Socket client;

            byte[] buf = new byte[1024*1024];  // byte array
            byte[] buf2 ;
            Thread x = new Thread(new udp());
            x.start();
            while (true){

                try{
                    while ( (client=sock1.accept() ) !=null){
                        Thread T = new Thread(new Server(client,0));
                        T.start();

                    }

                }
                catch (IOException e){


                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        // TODO: handle request from clients



    }


    public  static synchronized String acess(String command, String [] Values){  // acess the map
        switch (command){                       //possible errors: Check if the values works how its supposed to
            case "borrow":                          // output format: look for spaces
                String studname = Values[1];
                boolean contains = false;
                for(String Key: files.keySet()){
                  if( Values[2].equals(Key) ){ // Values[2] should contain the name of the book
                    contains=true;
                  }
               }

               if(contains){
                if( files.get(Values[2]) ==0 ){
                    return "Request Failed - Book not available";
                }
                else{
                    Integer temp= files.get(Values[2]);
                    String ret= "Your request has been approved "+records.incrementAndGet()+ " " +Values[2];    // " +"values[2] should have the string value
                    temp--;
                    idvalues.put(records.get(), Values[2]); // should associate the record with the name of the book
                    datarec nui = new datarec(Values[1],Values[2]);
                    idvalu.put(records.get(),nui);
                    Set<String> students = new HashSet<String>(); //creating a set of all books to add
                    if(!studentrec.containsKey(studname)){
                        students.add(Values[2]);
                    }

                    else{
                        students.add(Values[2]);
                        for(String h:  studentrec.get(studname)){
                            students.add(h);
                        }
                    }

                    //changed code
                    datarec2 recordpair = new datarec2(records.get(),Values[2]);
                    if(Nameid.isEmpty()){
                        Set<datarec2> tempset = new HashSet<>();
                        tempset.add(recordpair);
                        Nameid.put(Values[1],tempset);
                    }
                    else{
                        if(Nameid.containsKey(Values[1])){
                            Set<datarec2> tempset = new HashSet<>();
                            tempset.add(recordpair);
                            Nameid.remove(Values[1]);
                            Nameid.put(Values[1],tempset);

                        }
                        else{
                            Set<datarec2> tempset = new HashSet<>();
                            Nameid.put(studname, new HashSet<datarec2>());
                            tempset= Nameid.get(Values[1]);
                            tempset.add(recordpair);
                            Nameid.remove(Values[1]);
                            Nameid.put(Values[1],tempset);
                        }

                    }

                    studentrec.remove(studname);
                    studentrec.put(studname,students);
                    files.remove(Values[2]);
                    files.put(Values[2],temp);
                    return  ret;
                }
            }


            else{
                return "Request Failed - We do not have this book";
            }

            case "return":
                Boolean cont=false;
                Integer idvale= Integer.parseInt(Values[1]);
                for(Integer Key: idvalues.keySet()) {
                    if (idvale.equals(Key)) { // Values[2] should contain the name of the book
                        cont = true;
                    }
                }
                if(cont){
                    Integer curr1;
                    String bookname = idvalues.get(idvale);
                    String ret = idvale+ " is returned";
                    // need to increemnt the number of books
                    curr1= files.get(bookname);
                    curr1= curr1+1;
                    files.remove(bookname);
                    files.put(bookname,curr1);
                    datarec temp300=   idvalu.get(idvale);
                    String dude= temp300.Borrowername;
                    Set<String>glo=
                            studentrec.get(dude);
                    glo.remove(bookname);
                    if(glo.isEmpty()){
                        studentrec.remove(dude);
                    }else{
                        studentrec.remove(dude);
                        studentrec.put(dude,glo);
                    }
                    // need to update student records
                    // Set<datarec2>=Nameid.get(Values[1]);
                    if(!Nameid.isEmpty()){
                        Set<datarec2> setofdatarecs =   Nameid.get(dude);
                        for (Iterator<datarec2> it = setofdatarecs.iterator(); it.hasNext();){
                            datarec2 element = it.next();
                            if(element.record == idvale){
                                it.remove();
                            }

                        }
                        if(setofdatarecs.isEmpty()){
                            Nameid.remove(dude);
                        }
                        else{
                            Nameid.remove(dude);
                            Nameid.put(dude,setofdatarecs);
                        }
                    }
                    return  ret;

                }
                else{
                    return  (Values[1] +"not found, no such borrow record");
                }
            case "list":
                String studename= Values[1];
                Set<String> now= new TreeSet<>();
                 now=studentrec.get(studename); // might throw an error
                if(now== null || now.isEmpty() ){
                    return ("No record found for "+ studename);
                }
                else{
                    String return1= new String();
                    Set <Integer> gh =idvalues.keySet();
                    Set <datarec2> datercobjects;
                    datercobjects=   Nameid.get(studename)  ;
                    for(datarec2 x: datercobjects){
                        return1= return1+ x.record+ " " + x.Bookname;
                    }

                    return  return1;
                }
             case "inventory":
                 String rt= new String();
                 Set<String> bookss= files.keySet();
                 for(String v: bookss){
                     rt += v+ " " +files.get(v); //name + number
                     rt += "\r\n";
                 }
                 rt = rt.substring(0, rt.length()-2);
                 return  rt;
            case "exit":
                String rt2= new String();
                Set<String> books1= files.keySet();
                for(String v: books1){
                    rt2= rt2+v+ " " +files.get(v)+"\n"; //name + number
                }
                try{

                    PrintWriter writer = new PrintWriter("inventory.txt");
                    writer.print(rt2);
                    writer.flush();
                }catch (java.io.FileNotFoundException e){

                }
                break;



        }


        return "";

    }


}

/*
        // Test methods
        // not part of real code
        // Borrow Tester:
        String [] x = new String[3];
        x[0]= "borrow";
        x[1]= "Mike";
        x[2]="\"The Letter\"";
        //acess(x[0], x);
        // return Tester:
        String[] y = new String[2];
        y[0]="return";
        y[1]="1";
        //acess(y[0], y);
*/