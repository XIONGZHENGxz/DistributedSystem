import java.util.*;

public class BookInventory {
    private int length;
    public ArrayList<Book> list;

    public BookInventory(){
        this.length = 0;
        this.list = new ArrayList<Book>();
    }

    public void add(Book a){
        this.list.add(a);
        length++;
    }

    public Book find(String name){
        Book book = null;
        for(Book a : this.list){
            if(a.getName().equals(name)){
                book = a;
                break;
            }
        }
        return book;
    }

    public synchronized boolean checkout(String name) {
        Book a = this.find(name);
        return a != null && a.checkoutBook();
    }

    public void returnBook(String name){
        Book a = this.find(name);
        a.returnBook();
    }

    public int getLength(){
        return this.length;
    }

}
