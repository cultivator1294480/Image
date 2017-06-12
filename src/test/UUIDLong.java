package test;


import java.util.UUID;

/*我们需要将UUID作为实体的内部ID，UUID长度太长，而且是字符型的，检索速度很慢。
 * 因此，这里设计了一个在本系统里使用的UUID，只保证在本系统中是唯一的ID，即UUIDLong，生成的结果是一个Long型的UUID。
 */
public class UUIDLong {

	/*
	 * 生成一个long型的UUID。
	 */
	public static long longUUID() {
		return UUID.randomUUID().getMostSignificantBits();
	}

	public static long absLongUUID() {
		while (true) {
			long r = longUUID();
			if (r > 0) {
				return r;
			}
		}
	}

}
