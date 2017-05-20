import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomMap extends TreeMap<Object, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Lock getLock = new ReentrantLock();

	private final Lock putLock = new ReentrantLock();

	private final long limit;

	private final String fileName;

	public CustomMap(long size) {
		this.limit = size;
		Random ran = new Random();
		this.fileName = "MyMap.ser" + ran.nextInt(6) + 5;

	}

	@Override
	public Object put(Object key, Object value) throws ClassCastException,
			NullPointerException {

		getLock.lock();

		try {
			if (size() >= limit) {
				writeObject(key);
				writeObject(value);

			} else {
				super.put(key, value);
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			getLock.unlock();

		}
		return value;
	}

	/**
	 * @param key
	 * @param value
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeObject(Object obj) throws FileNotFoundException,
			IOException {

		boolean exists = new File(fileName).exists();
		FileOutputStream fos = new FileOutputStream(fileName, true);
		ObjectOutputStream oos = exists ? new ObjectOutputStream(fos) {
			protected void writeStreamHeader() throws IOException {
				reset();
			}
		} : new ObjectOutputStream(fos);
		oos.writeObject(obj);
		oos.close();
		fos.close();
	}

	@Override
	public Object get(Object key) throws ClassCastException,
			NullPointerException {
		putLock.lock();

		Object returnObj = null;
		Object ob = null;
		int i = 0;
		try {
			returnObj = super.get(key);

			if (returnObj == null) {

				FileInputStream fis = new FileInputStream(fileName);
				ObjectInputStream ois = new ObjectInputStream(fis);

				while (fis.available() > 0) {
					try {
						ob = ois.readObject();
						if (key.equals(ob) && i % 2 == 0) {
							returnObj = ois.readObject();
							break;
						}
						i++;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				ois.close();

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			putLock.unlock();
		}
		return returnObj;
	}

	@Override
	public int size() {
		return super.size();
	}

	public static void main(String[] args) {

		CustomMap map = new CustomMap(2);

		map.put(1, "a");
		map.put(2, "b");
		map.put(3, "c");
		map.put(4, "d");

		System.out.println(map.get(1));
		System.out.println(map.get(2));
		System.out.println(map.get(3));
		System.out.println(map.get(4));
	}

}
