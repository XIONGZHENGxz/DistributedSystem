import java.util.HashMap;

/**
 * Created by gydoy on 3/3/2018.
 */
public class Library {
    private HashMap<String, Integer> inventory;
    //need another data structure for students and what books they have

    public Library(){
        this.inventory = new HashMap<>();
    }

    public HashMap<String, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(HashMap<String, Integer> library) {
        this.inventory = library;
    }



}
