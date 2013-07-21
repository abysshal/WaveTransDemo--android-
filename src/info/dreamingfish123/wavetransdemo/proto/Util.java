package info.dreamingfish123.wavetransdemo.proto;

/**
 * The self defined utils.
 * 
 * @author Hui
 * 
 */
public class Util {

	/**
	 * Convert an integer into an array of 4-byte length.
	 * 
	 * @param val
	 *            The integer value to be converted.
	 * @param a
	 *            The result array.
	 * @param offset
	 *            The offset of the array to fill result from.
	 */
	public static void int2byte(int val, byte[] a, int offset) {
		a[offset] = (byte) ((val >> 0) & 0xFF);
		a[1 + offset] = (byte) ((val >> 8) & 0xFF);
		a[2 + offset] = (byte) ((val >> 16) & 0xFF);
		a[3 + offset] = (byte) ((val >> 24) & 0xFF);
	}

	/**
	 * Convert an array of 4-byte length into an integer.
	 * 
	 * @param a
	 *            The array to be converted.
	 * @param offset
	 *            The offset of the array to convert from.
	 * @return The result integer.
	 */
	public static int byte2int(byte[] a, int offset) {
		int val;
		val = a[3 + offset] & 0xFF;
		val = (val << 8) + (a[2 + offset] & 0xFF);
		val = (val << 8) + (a[1 + offset] & 0xFF);
		val = (val << 8) + (a[offset] & 0xFF);
		return val;
	}

	/**
	 * Convert an array of bytes to hex string.
	 * 
	 * @param bytes
	 *            The array to be converted.
	 * @return The result string.
	 */
	public static String getHex(byte[] bytes) {
		return getHex(bytes, 0, bytes.length);
	}

	/**
	 * Convert an array of bytes to hex string.
	 * 
	 * @param bytes
	 *            The array to be converted.
	 * @param offset
	 *            Start from.
	 * @param len
	 *            Length.
	 * @return The result string.
	 */
	public static String getHex(byte[] bytes, int offset, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (i != 0 && i % 16 == 0) {
				sb.append("\n");
			}
			sb.append(String.format("%02X ", bytes[i + offset]));
		}
		return sb.toString();
	}

	/**
	 * Resample the 16bit wave format data to 8bit format.
	 * 
	 * @param bytes
	 *            The data to be resampled.
	 * @return The result data.
	 */
	public static byte[] resample16To8bit(byte[] bytes) {
		return resample16To8bit(bytes, 0, bytes.length);
	}

	/**
	 * Resample the 16bit wave format data to 8bit format.
	 * 
	 * @param bytes
	 *            The data to be resampled.
	 * @param offset
	 *            Start from.
	 * @param len
	 *            Length.
	 * @return The result data.
	 */
	public static byte[] resample16To8bit(byte[] bytes, int offset, int len) {
		byte[] ret = new byte[len / 2];
		for (int i = 0; i < ret.length; i++) {
			short s0 = (short) (bytes[i * 2 + offset] & 0xff);
			short s1 = (short) (bytes[i * 2 + offset + 1] & 0xff);
			ret[i] = (byte) (((short) (s0 | s1 << 8)) >> 8);
		}
		return ret;
	}

	/**
	 * Convert an array of 2-byte length with little endian to a short.
	 * 
	 * @param bytes
	 *            The bytes to be converted.
	 * @param offset
	 *            Start from.
	 * @return The result short.
	 */
	public static int readShortLittleEndian(byte[] bytes, int offset) {
		short s0 = (short) (bytes[offset] & 0xff);
		short s1 = (short) (bytes[offset + 1] & 0xff);
		return ((short) (s0 | s1 << 8)) + Short.MAX_VALUE + 1;
	}

	/**
	 * Convert an array of 2-byte length with big endian to a short.
	 * 
	 * @param bytes
	 *            The bytes to be converted.
	 * @param offset
	 *            Start from.
	 * @return The result short.
	 */
	public static int readShortBigEndian(byte[] bytes, int offset) {
		short s1 = (short) (bytes[offset] & 0xff);
		short s0 = (short) (bytes[offset + 1] & 0xff);
		return ((short) (s0 | s1 << 8)) + Short.MAX_VALUE + 1;
	}

	/**
	 * @deprecated Useless.
	 * @param b
	 * @param offset
	 * @return
	 */
	public static short byteToShort(byte[] b, int offset) {
		short s0 = (short) (b[offset] & 0xff);
		short s1 = (short) (b[offset + 1] & 0xff);
		return (short) (s0 | s1 << 8);
	}

	/**
	 * Amplify rawdata of sound source.
	 * 
	 * @param src
	 *            The source.
	 * @return The result.
	 */
	public static int amplify(int src) {
		int ret = 0;
		if (src > Constant.AMPLIFICATION_LEVEL_MUTE) {
			ret = (src - Constant.AMPLIFICATION_LEVEL_MUTE)
					* Constant.MAGNIFICATION
					+ Constant.AMPLIFICATION_LEVEL_MUTE;
			if (ret > Short.MAX_VALUE * 2) {
				ret = Short.MAX_VALUE * 2;
			}
		} else {
			ret = Constant.AMPLIFICATION_LEVEL_MUTE
					- (Constant.AMPLIFICATION_LEVEL_MUTE - src)
					* Constant.MAGNIFICATION;
			if (ret < 0) {
				ret = 0;
			}
		}
		return ret;
	}
}
