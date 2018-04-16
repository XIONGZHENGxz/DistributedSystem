
public class CommandParser {

		public static String parseAndExecute(String command, Inventory lib, RecordList rec)
		{
			String[] tokens = command.split(" ");

			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = tokens[i].trim();
			}
			//System.out.println("[debug] In Command Parser " + command + " (" + tokens[0].equals("inventory") + ")" );

			if (tokens[0].equals("borrow")) {
				/*
				if (tokens.length < 3) {
					System.err.println("Insufficient arguments for borrow command");
					return "";
				}
				*/

				String student = tokens[1];
				String book = new String(); 
				for(int i = 2; i < tokens.length; i++) {
					book += tokens[i] + " ";		
				}
				book = book.trim();
			//	System.out.println("\"" + student + "\"");
			//	System.out.println("\"" + book + "\"");
				
				int borrowResult = lib.borrow(book);
				int recordId;

				switch (borrowResult) {
					case 0:
						recordId = rec.borrow(student, book);
						return ("Your request has been approved, " + recordId + " " + student + " " + book);
					case -1:
						return ("Request Failed - Book not available");
					case -2:
						return ("Request Failed - We do not have this book");		
				}	
			} else if (tokens[0].equals("return")) {
				int recordId = Integer.parseInt(tokens[1]);

				String returnResult = rec.ret(recordId);

				if(returnResult != "DNE") {
					lib.ret(returnResult);
					return (recordId + " is returned");	
				}

				return (recordId + " not found, no such borrow record");
				
			} else if (tokens[0].equals("inventory")) {
				//System.out.println("[debug] In inventory handler");
				return (lib.list());

			} else if (tokens[0].equals("list")) {
				String student = tokens[1];
				return(rec.list(student));

			}/* else if (tokens[0].equals("exit")) {*/
				lib.exit();
				return ("End thread");
			//}
		}
}
