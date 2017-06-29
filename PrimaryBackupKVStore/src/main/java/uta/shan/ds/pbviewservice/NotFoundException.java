public class NotFoundException extends Exception{
	public NotFoundException(Object key){
		super("The key "+key.toString()+"couldn't be found! ");
	}
}

	
