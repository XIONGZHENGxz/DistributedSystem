public class Book {
    private String name;
    private int amount;

    public Book(String name, int amount){
        this.name = name;
        this.amount = amount;
    }

    public synchronized boolean checkoutBook(){
        if(amount - 1 < 0){
            return false;
        }
        else{
            amount--;
            return true;
        }
    }

    public void returnBook(){
        amount++;
    }

    public String getName(){
        return this.name;
    }

    public int getAmount(){return this.amount;}
}
