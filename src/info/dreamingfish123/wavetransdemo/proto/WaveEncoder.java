package info.dreamingfish123.wavetransdemo.proto;

public class WaveEncoder {

	/*
	 * Encode the data to self-defined wave format
	 */
	public static byte[] encode(byte[] data) {
		byte[] ret = new byte[data.length * Constant.BIT_PER_BYTE
				* Constant.POINT_PER_BIT];

		int retOffset = 0;

		for (int i = 0; i < data.length; i++) {
			// UART start
			convertBit(ret, retOffset, 0);
			retOffset += Constant.POINT_PER_BIT;

			// UART data
			for (int j = 0; j < 8; j++) {
				convertBit(ret, retOffset, (data[i] & (0x01 << (7 - j))));
				retOffset += Constant.POINT_PER_BIT;
			}

			// UART stop
			convertBit(ret, retOffset, 1);
			retOffset += Constant.POINT_PER_BIT;
		}

		return ret;

	}

	/*
	 * Impl the Manchester encoding, and fit the POINT_PER_SAMPLE
	 */
	private static void convertBit(byte[] ret, int offset, int bit) {
		int index = offset;
		for (int i = 0; i < Constant.POINT_PER_BIT / 2; i++) {
			ret[index++] = (bit > 0 ? Constant.WAVE_LOW_LEVEL
					: Constant.WAVE_HIGH_LEVEL);
		}
		for (int i = 0; i < Constant.POINT_PER_BIT / 2; i++) {
			ret[index++] = (bit > 0 ? Constant.WAVE_HIGH_LEVEL
					: Constant.WAVE_LOW_LEVEL);
		}
	}
}
