public class Record {
    private String user;
    private String book;
    private int id;
    private boolean returned;


    public Record(String user, String book, int id){
        this.user = user;
        this.book = book;
        this.id = id;
        this.returned = false;
    }

    public int getId(){
        return id;
    }

    public String getUser(){
        return user;
    }

    public String getBook(){
        return book;
    }

    public boolean getReturned(){
        return returned;
    }

    public synchronized boolean returnBook(){
        if(returned){
            return false;
        }
        else{
            returned = true;
            return true;
        }
    }
}