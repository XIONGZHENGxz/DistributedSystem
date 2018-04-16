public class Solution{

		class DLL {
				String val;
				DDL prev, next;
				ReentrantLock lock;
				public DLL(String s) {
						val = s;
						prev = null;
						next = null;
						lock = new ReentrantLock();
				}
		}


		public static DLL head, tail;

		public Solution() {
				head = tail = null;
		}

		public void insert(String val) {
				DLL node = new DLL(val);
				DLL curr = head;
				while(curr != null) {
						if(curr.val.compareTo(val) == -1) {

								if(curr.next != null) {
										if (curr.next.val.compareTo(val) == 1) {
												curr.lock.lock();
												curr.next.lock.lock();
												curr.prev.lock.lock();
												node.next = curr.next;
												curr.next.prev = node;
												curr.next = node;
												node.prev = curr;
										}
								} else {
										curr.lock.lock();
										curr.next.lock.lock();
										curr.prev.lock.lock();
										node.prev = curr;
										curr.next = node;
								}
								break;
						}
						curr = curr.next;
				}
		}

		public void delete(String val) {
				DLL curr = head;
				while(curr != null) {
						if(curr.val.compareTo(val) == 0) {
								if(curr.next != null) {
										curr.prev.lock.lock();
										curr.lock.lock();
										curr.next.lock();
										curr.prev.next = curr.next;
										curr.next.prev = curr.prev;
										curr = null;
								} else {
										curr.prev.lock.lock();
										curr.lock.lock();
										curr.prev = null;
								}
								break;
						}
						curr = curr.next;
				}
		}


		public void print() {
				DLL curr = head;
				while(curr != null) {
						System.out.println(curr.val+" ");
						curr = curr.next;
				}
		}

		public void read(int index) {
			DLL curr = head;
			int count = 0;
			while(curr != null) {
				if(count == index) 
					curr.lock.lock();
					System.out.println(curr.val);
					break;
				}
				count++;
				curr = curr.next;
			}
		}

		public static void main(String...args) {
				ExecutorService es = Executors.newFixedThreadPool(5);

				String input = args[0];
				BufferedReader br = new BufferedReader(new FileReader(input));
				String line = "";
				while((line = br.readLine()) ! = null) {
					
					
					





