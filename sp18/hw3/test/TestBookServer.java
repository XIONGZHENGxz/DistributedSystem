import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestBookServer.populateInventoryTests.class,
		TestBookServer.borrowTests.class,
		TestBookServer.returnBookTests.class,
		TestBookServer.listTests.class,
		TestBookServer.inventoryTests.class,
		TestBookServer.exitTests.class,
		TestBookServer.writeInventoryTests.class,
		TestBookServer.commandToArray_Tests.class
})

public class TestBookServer {

	/** ----- populateInventory() Tests ----- **/
	public static class populateInventoryTests {
		@Test
		public void populateInv_empty() {
		}

		// Tests spaced and unspace names
		@Test
		public void populateInv_spaced() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void populateInv_singleQuantity() {

		}

		@Test
		public void populateInv_multipleQuantity() {

		}
	}


	/** ----- borrow() Tests ----- **/
	public static class borrowTests {
		// Won't work b/c quotes
		@Test
		public void borrow_nonexistent() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result = bs.borrow("Aaron", "Divergent");

				assertEquals("Request Failed - We do not have this book", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void borrow_empty() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"The Letter\"");
				String firstResult = bs.borrow("Aaron", "\"The Letter\"");
				bs.returnBook(1);
				String secondResult = bs.borrow("Rahul", "\"The Letter\"");

				assertEquals("Request Failed - Book not available", firstResult);
				assertEquals("Your request has been approved, 2 Rahul \"The Letter\"", secondResult);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void borrow_one() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result = bs.borrow("Aaron", "\"The Count of Monte Cristo\"");

				assertEquals("Your request has been approved, 1 Aaron \"The Count of Monte Cristo\"", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void borrow_multiple() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result;
				for (int i = 1; i <= 9; i++) {
					result = bs.borrow("Aaron", "\"Divergent\"");
					assertEquals("Your request has been approved, " + i + " Aaron \"Divergent\"", result);
				}
				result = bs.borrow("Aaron", "\"Divergent\"");
				assertEquals("Request Failed - Book not available", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/** ----- returnBook() Tests ----- **/
	public static class returnBookTests {
		@Test
		public void returnBook_nonexistent() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result = bs.returnBook(1);
				assertEquals(1 + " not found, no such borrow record", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void returnBook_recentCheckout() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"Divergent\"");
				String result = bs.returnBook(1);
				assertEquals(1 + " is returned", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void returnBook_pastCheckout() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"The Letter\"");
				bs.borrow("Rahul", "\"Divergent\"");
				String firstResult = bs.returnBook(2);
				String secondResult = bs.returnBook(1);

				assertEquals(2 + " is returned", firstResult);
				assertEquals(1 + " is returned", secondResult);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void returnBook_alreadyReturned() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"Divergent\"");
				bs.returnBook(1);
				String result = bs.returnBook(1);
				assertEquals(1 + " not found, no such borrow record", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/** ----- list() Tests ----- **/
	public static class listTests {
		@Test
		public void list_nonexistent() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result = bs.list("Aaron");

				assertEquals("No record found for Aaron", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void list_noBorrows() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"Divergent\"");
				bs.returnBook(1);
				String result = bs.list("Aaron");

				assertEquals("No record found for Aaron", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@Test
		public void list_oneRecord() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"Divergent\"");
				String result = bs.list("Aaron");

				String expected = "1 \"Divergent\"";
				assertEquals(expected, result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void list_multipleRecords() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.borrow("Aaron", "\"Divergent\"");
				bs.borrow("Aaron", "\"The Great Gatsby\"");
				bs.borrow("Rahul", "\"The Count of Monte Cristo\"");
				bs.borrow("Aaron", "\"The Letter\"");					// mixing it up
				bs.borrow("Aaron", "\"The Count of Monte Cristo\"");

				bs.borrow("Rahul", "\"The Count of Monte Cristo\"");
				bs.borrow("Rahul", "\"Divergent\"");
				bs.borrow("Aaron", "\"The Great Gatsby\"");			// mixing it up
				bs.borrow("Rahul", "\"Divergent\"");
				String firstResult = bs.list("Aaron");
				String secondResult = bs.list("Rahul");

				String firstExpected = "";
				firstExpected += "1 \"Divergent\"\n";
				firstExpected += "2 \"The Great Gatsby\"\n";
				firstExpected += "4 \"The Letter\"\n";
				firstExpected += "5 \"The Count of Monte Cristo\"\n";
				firstExpected += "8 \"The Great Gatsby\"";

				String secondExpected = "";
				secondExpected += "3 \"The Count of Monte Cristo\"\n";
				secondExpected += "6 \"The Count of Monte Cristo\"\n";
				secondExpected += "7 \"Divergent\"\n";
				secondExpected += "9 \"Divergent\"";

				assertEquals(firstExpected, firstResult);
				assertEquals(secondExpected, secondResult);
				bs.closeAll();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Interleaving
		// Aaron: nothing, something, nothing
		// Rahul: nothing, something, something
		@Test
		public void list_listAfterAlter() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result;
				String noRecord = "No record found for " ;

				// Nothing
				result = bs.list("Aaron");
				assertEquals(noRecord + "Aaron", result);
				result = bs.list("Rahul");
				assertEquals(noRecord + "Rahul", result);

				// Something
				bs.borrow("Aaron", "\"Divergent\"");
				bs.borrow("Rahul", "\"Divergent\"");
				result = bs.list("Aaron");
				assertEquals("1 \"Divergent\"", result);
				result = bs.list("Rahul");
				assertEquals("2 \"Divergent\"", result);

				// Aaron: Nothing
				// Rahul: Something
				bs.returnBook(1);
				result = bs.list("Aaron");
				assertEquals(noRecord + "Aaron", result);
				result = bs.list("Rahul");
				assertEquals("2 \"Divergent\"", result);

				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/** ----- inventory() Tests ----- **/
	public static class inventoryTests {
		@Test
		public void inventory_empty() {
			try {
				BookServer bs = new BookServer();
				String result = bs.inventory();
				assertEquals("", result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void inventory_zeroQuantity() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");

				bs.borrow("Aaron", "\"The Letter\"");
				for (int i = 0; i < 9; i++) {
					bs.borrow("Aaron", "\"Divergent\"");
				}
				String result = bs.inventory();

				String expected = "";
				expected += ("\"The Letter\" " + 0 + "\n");
				expected += ("\"The Great Gatsby\" " + 15 + "\n");
				expected += ("\"Divergent\" " + 0 + "\n");
				expected += ("\"The Count of Monte Cristo\" " + 17);

				assertEquals(expected, result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void inventory_nonZeroQuantity() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				String result = bs.inventory();

				String expected = "";
				expected += ("\"The Letter\" " + 1 + "\n");
				expected += ("\"The Great Gatsby\" " + 15 + "\n");
				expected += ("\"Divergent\" " + 9 + "\n");
				expected += ("\"The Count of Monte Cristo\" " + 17);

				assertEquals(expected, result);
				bs.closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/** ----- exit() Tests ----- **/
	public static class exitTests {
		@Test
		public void exit_stopped() {

		}

		@Test
		public void exit_overwriteFile() {

		}
	}

	/** ----- writeInventory() Tests ----- **/
	public static class writeInventoryTests {
		@Test
		public void write_empty() {
			try {
				BookServer bs = new BookServer();
				bs.exit();
				bs.closeAll();
				assertTrue(compareFiles("inventory.txt", "./test/blank.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void write_nonempty() {
			try {
				BookServer bs = new BookServer();
				bs.populateInventory("given_inventory.txt");
				bs.exit();
				bs.closeAll();
				assertTrue(compareFiles("inventory.txt", "given_inventory.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Precondition: both files are valid
		public boolean compareFiles(String file1, String file2) {
			try {
				File f1 = new File(file1);
				File f2 = new File(file2);
				Scanner scan1 = new Scanner(f1);
				Scanner scan2 = new Scanner(f2);

				while (scan1.hasNextLine()) {
					if (!scan1.nextLine().equals(scan2.nextLine())) {
						return false;
					}
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				return false;
			}
			return true;
		}
	}

	/** ----- commandToArray() Tests ----- **/
	public static class commandToArray_Tests {
		@Test
		public void emptyString() {
			try {
				BookServer testServer = new BookServer();
				UdpServerThread testThread = new UdpServerThread(testServer);

				String[] result = testThread.commandToArray("");
				String[] expected = new String[1];
				expected[0] = "";
				testServer.closeAll();
				assertArrayEquals(expected, result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void oneCharString() {
			try {
				BookServer testServer = new BookServer();
				UdpServerThread testThread = new UdpServerThread(testServer);

				String[] result = testThread.commandToArray("a");
				String[] expected = new String[1];
				expected[0] = "a";
				testServer.closeAll();
				assertArrayEquals(expected, result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void twoCharString() {
			try {
				BookServer testServer = new BookServer();
				UdpServerThread testThread = new UdpServerThread(testServer);

				String[] result = testThread.commandToArray("ab");
				String[] expected = new String[1];
				expected[0] = "ab";
				testServer.closeAll();
				assertArrayEquals(expected, result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void spacesAndQuotes() {
			try {
				BookServer testServer = new BookServer();
				UdpServerThread testThread = new UdpServerThread(testServer);

				String[] result = testThread.commandToArray("borrow Aaron \"The Count of Monte Cristo\"");
				String[] expected = new String[3];
				expected[0] = "borrow";
				expected[1] = "Aaron";
				expected[2] = "\"The Count of Monte Cristo\"";
				testServer.closeAll();
				assertArrayEquals(expected, result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void onlySpaces() {
			try {
				BookServer testServer = new BookServer();
				UdpServerThread testThread = new UdpServerThread(testServer);

				String[] result = testThread.commandToArray("We are the champions my friend");
				String[] expected = new String[6];
				expected[0] = "We";
				expected[1] = "are";
				expected[2] = "the";
				expected[3] = "champions";
				expected[4] = "my";
				expected[5] = "friend";
				testServer.closeAll();
				assertArrayEquals(expected, result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void multipleQuotes() {
			try {
				BookServer testServer = new BookServer();
				UdpServerThread testThread = new UdpServerThread(testServer);

				String[] result = testThread.commandToArray("\"Why\" \"are\" \"there\" \"so\" \"many?\"");
				String[] expected = new String[5];
				expected[0] = "\"Why\"";
				expected[1] = "\"are\"";
				expected[2] = "\"there\"";
				expected[3] = "\"so\"";
				expected[4] = "\"many?\"";
				testServer.closeAll();
				assertArrayEquals(expected, result);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
