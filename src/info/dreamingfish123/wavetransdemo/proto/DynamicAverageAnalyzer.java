package info.dreamingfish123.wavetransdemo.proto;

/**
 * The dynamic average analyzer to analyze the input sound data.
 * 
 * @author Hui
 * 
 */
public class DynamicAverageAnalyzer {

	/**
	 * Max length of the input buffer size.
	 */
	private int bufferSize = Constant.WAVEOUT_BUF_SIZE * 2;

	/**
	 * The input buffer.
	 */
	private int[] buffer;

	/**
	 * The start point of the buffer.
	 */
	private int start = 0;

	/**
	 * The last start point of the buffer.
	 */
	private int lastStart = 0;

	/**
	 * The last remain length of the buffer.
	 */
	private int lastRemainLen = 0;

	/**
	 * The remain legnth of the buffer.
	 */
	private int remainLen = 0;

	/**
	 * The analyzed result in byte array.
	 */
	private byte[] result = new byte[Constant.MAX_TRANSFER_DATA_LEN * 2];

	/**
	 * The analyzed result in WTPPacket.
	 */
	private WTPPacket packet = null;

	/**
	 * Progress flag. The packet start point found.
	 */
	private boolean startPointFound = false;

	/**
	 * Progress flag. The packet size found.
	 */
	private boolean packetSizeFound = false;

	/**
	 * Progress flag. The whole packet found.
	 */
	private boolean packetFound = false;

	/**
	 * Progress flag. The size of the found packet.
	 */
	private int packetSize = 0;

	/**
	 * Progress flag. The size of decoded bytes.
	 */
	private int bytesDecoded = 0;

	/**
	 * With default buffer size.
	 */
	public DynamicAverageAnalyzer() {
		buffer = new int[bufferSize];
		resetAll();
	}

	/**
	 * Set a buffer size.
	 * 
	 * @param bufferSize
	 *            The size to be setted.
	 */
	public DynamicAverageAnalyzer(int bufferSize) {
		this.bufferSize = bufferSize;
		buffer = new int[bufferSize];
		resetAll();
	}

	/**
	 * Should be called after one packet is found and continue to find more
	 */
	public void resetForNext() {
		int len = remainLen;
		reallocBuffer(start, remainLen);
		resetAll();
		remainLen = len;
	}

	/**
	 * Add wavein bytes to this analyzer's buffer
	 * 
	 * @param data
	 *            The wavein bytes
	 * @return True if there is enough space of the buffer to append these bytes
	 */
	public boolean appendBuffer(byte[] data) {
		return appendBuffer(data, 0, data.length);
	}

	/**
	 * Add wavein bytes to this analyzer's buffer
	 * 
	 * @param data
	 *            The wavein bytes
	 * @param offset
	 *            Copy from
	 * @param len
	 *            How much to copy
	 * @return True if there is enough space of the buffer to append these bytes
	 */
	public boolean appendBuffer(byte[] data, int offset, int len) {
		if (start + remainLen + len / 2 > bufferSize) {
			return false;
		}
		for (int i = 0; i < len / 2; i++) {
			buffer[start + remainLen + i] = Util.amplify(Util
					.readShortLittleEndian(data, i * 2));
		}
		remainLen += len / 2;
		return true;
	}

	/**
	 * Add wavein data in Integer format to this analyzer's buffer
	 * 
	 * @param val
	 *            The transformed wavein data
	 * @return True if there is enough space of the buffer to append this
	 */
	public boolean appendBuffer(int val) {
		if (start + remainLen + 1 > bufferSize) {
			return false;
		}
		buffer[start + remainLen] = val;
		remainLen += 1;
		return true;
	}

	/**
	 * Analyze from the buffer
	 * 
	 * @return True if one packet is found
	 */
	public boolean analyze() {
		while (isBufferAnalysis() && !packetFound) {
			if (!startPointFound) { // 1. to find the start flag
				if (!locateDataHead()) {
					continue;
				}
			}
			if (!packetSizeFound) {// 2. to find the packet size
				if (!getPacketSize()) {
					continue;
				}
			}
			while (!packetFound) { // 3. decode UART
				if (!getPacketData()) {
					break;
				}
			}
		}
		return packetFound;
	}

	/**
	 * Get the decoded Wave Trans Proto packet
	 * 
	 * @return The result packet
	 */
	public WTPPacket getPacket() {
		return this.packet;
	}

	/**
	 * Check if there is enough bytes in the buffer to be analyzed
	 * 
	 * @return True if there is enough bytes
	 */
	private boolean isBufferAnalysis() {
		return (remainLen >= Constant.POINT_PER_UART);
	}

	/**
	 * Move the remain bytes in the buffer to the offset 0
	 * 
	 * @param offset
	 *            Move from
	 * @param len
	 *            How much to move
	 */
	private void reallocBuffer(int offset, int len) {
		int j = 0;
		for (int i = offset; i < offset + len; i++) {
			buffer[j++] = buffer[i];
		}
	}

	/**
	 * Reset all params to its defaults
	 */
	private void resetAll() {
		start = 0;
		remainLen = 0;
		startPointFound = false;
		packetSizeFound = false;
		packetFound = false;
		bytesDecoded = 0;
		packetSize = 0;
		packet = null;
	}

	/**
	 * Called when error occurred while decoding, to reset params for a new
	 * analysis
	 * 
	 * @param remainLen
	 *            How much bytes remained to be moved
	 */
	private void resetOnDecodeError() {
		// int len = start + remainLen;
		// resetAll();
		// remainLen = len > 0 ? len - 1 : 0;
		// start = remainLen > 0 ? 1 : 0;

		int len = lastRemainLen;
		reallocBuffer(lastStart, lastRemainLen);
		resetAll();
		start = 0;
		remainLen = len;
	}

	/**
	 * Check if a packet has been found
	 */
	private void finishPacket() {
		if (bytesDecoded == packetSize) { // finished
			packetFound = true;
			packet = WTPPacket.decodePacketBytes(result);
		}
	}

	/**
	 * To find out the start flag from the buffer
	 * 
	 * @return True if found
	 */
	private boolean locateDataHead() {
		while (true) {
			if (remainLen < Constant.POINT_PER_UART) {
				break;
			}
			if (buffer[start] > buffer[start + Constant.POINT_PER_BIT_HALF]
					+ Constant.WAVE_DIFF_LEVEL) {
				int val = decodeUART();
				if (val == (Constant.PACKET_START_FLAG & 0xff)) { // found
					reallocBuffer(lastStart, lastRemainLen);
					start = (start - lastStart);
					result[bytesDecoded++] = Constant.PACKET_START_FLAG;
					startPointFound = true;
					return true;
				} else {
					start = lastStart + 1;
					remainLen = lastRemainLen - 1;
				}
			} else {
				start += 1;
				remainLen -= 1;
			}
		}

		// not found
		reallocBuffer(start, remainLen);
		start = 0;
		return false;
	}

	/**
	 * To found out the packet size from the buffer
	 * 
	 * @return True if found
	 */
	private boolean getPacketSize() {
		if (remainLen < Constant.POINT_PER_UART) {
			return false;
		}
		int val = decodeUART();
		if (val >= 0) {
			packetSize = val;
			result[bytesDecoded++] = (byte) (val & 0xff);
			packetSizeFound = true;
			finishPacket();
			return true;
		} else {
			resetOnDecodeError();
		}
		return false;
	}

	/**
	 * To find out the packet data from the buffer
	 * 
	 * @return True if found
	 */
	private boolean getPacketData() {
		if (remainLen < Constant.POINT_PER_UART) {
			return false;
		}
		int val = decodeUART();
		if (val >= 0) {
			result[bytesDecoded++] = (byte) (val & 0xff);
			finishPacket();
			return true;
		} else {
			resetOnDecodeError();
		}
		return false;
	}

	/**
	 * Convert some sample point bytes to a bit data.<br/>
	 * Use abs level
	 * 
	 * @return 1 - bit 1;<br/>
	 *         0 - bit 0;<br/>
	 *         other - error<br/>
	 */
	private int convertBit() {
		int ave1 = 0;
		int ave2 = 0;
		int ave = 0;

		for (int i = 0; i < Constant.POINT_PER_BIT_HALF; i++) {
			ave1 += (buffer[start + i]);
			ave2 += (buffer[start + i + Constant.POINT_PER_BIT_HALF]);
		}

		ave1 /= Constant.POINT_PER_BIT_HALF;
		ave2 /= Constant.POINT_PER_BIT_HALF;
		ave = (ave1 + ave2) / 2;

		if ((((buffer[start + 3]) > ave) && ((buffer[start + 2]) > ave))
				|| (((buffer[start + 3]) < ave) && ((buffer[start + 2]) < ave))) {
			if (((ave1 > ave2) && ((buffer[start + 2]) < ave))
					|| ((ave1 < ave2) && ((buffer[start + 2]) > ave))) {
				if (start > 0) {
					start -= 1;
				}
			} else if (((ave1 < ave2) && ((buffer[start + 3]) < ave))
					|| ((ave1 > ave2) && ((buffer[start + 3]) > ave))) {
				start += 1;
			}
		}

		if (ave1 > ave2) {
			return Constant.MANCHESTER_LOW;
		} else {
			return Constant.MANCHESTER_HIGH;
		}
	}

	/**
	 * Decode an entire UART data
	 * 
	 * @return >= 0 if decode succeed and the result should be returned;<br/>
	 *         < 0 if error occurred
	 */
	private int decodeUART() {
		int retTmp = 0;
		int ret = 0;

		lastStart = start;
		lastRemainLen = remainLen;

		try {
			retTmp = convertBit();
			if (retTmp < 0 || retTmp == 1) {
				return -101;
			}

			start += Constant.POINT_PER_BIT;

			for (int i = 0; i < 8; i++) {
				retTmp = convertBit();
				if (retTmp < 0) {
					return -103;
				}
				start += Constant.POINT_PER_BIT;
				ret += (retTmp << (7 - i));
			}

			retTmp = convertBit();
			if (retTmp <= 0) {
				return -102;
			}
			start += Constant.POINT_PER_BIT;

			remainLen -= (start - lastStart);
		} finally {
		}
		return ret;
	}
}
