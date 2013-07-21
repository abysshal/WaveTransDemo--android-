package info.dreamingfish123.wavetransdemo.proto;

/**
 * The self defined constants.
 * 
 * @author Hui <br>
 * @see <a href="https://ccrma.stanford.edu/courses/422/projects/WaveFormat/"
 *      target="_blank">The Wave format reference</a>
 */
public class Constant {

	/**
	 * Wave frequency in Hz.
	 */
	public static final int WAVE_RATE_INHZ = 44100;

	/**
	 * The max length of a data packet in bytes, include the packet head and
	 * payload.
	 */
	public static final int MAX_TRANSFER_DATA_LEN = 200;

	/**
	 * The number of wave sample points of one certain data bit.
	 */
	public static final int POINT_PER_BIT = 6;

	/**
	 * The half number of wave sample points of one certain data bit.
	 */
	public static final int POINT_PER_BIT_HALF = 3;

	/**
	 * The number of bits of a UART encoded data byte.
	 */
	public static final int BIT_PER_BYTE = 10;

	/**
	 * The number of wave sample points of one certain UART encoded data byte,
	 * equals to POINT_PER_BIT * BIT_PER_BYTE.
	 */
	public static final int POINT_PER_UART = POINT_PER_BIT * BIT_PER_BYTE;

	/**
	 * The length of wave formated data header, equals to 0x2c, defined by
	 * MicroSoft.
	 */
	public static final int WAVE_HEAD_LEN = 0x2c;

	/**
	 * The max length of buffer per data packet.
	 */
	public static final int WAVEOUT_BUF_SIZE = MAX_TRANSFER_DATA_LEN
			* POINT_PER_BIT * BIT_PER_BYTE + WAVE_HEAD_LEN;

	/**
	 * The offset of the 4 bytes param of data length in the wave header.
	 */
	public static final int WAVE_DATA_LEN_OFFSET = 0x28;

	/**
	 * The offset of the 4 bytes param of file length in the wave header.
	 */
	public static final int WAVE_FILE_LEN_OFFSET = 0x04;

	/**
	 * The byte value for the high level.
	 */
	public static final byte WAVE_HIGH_LEVEL = (byte) 0xF0;

	/**
	 * The byte value for the low level.
	 */
	public static final byte WAVE_LOW_LEVEL = (byte) 0x10;

	/**
	 * The byte value for the mute level.
	 */
	public static final byte WAVE_MUTE_LEVEL = (byte) 0x80;

	/**
	 * The value indicate the two different levels.
	 */
	public static final int WAVE_DIFF_LEVEL = 0x30 * 0xFF;

	/**
	 * The sum value indicate the two different levels.
	 */
	public static final int WAVE_DIFF_SUM_LEVEL = POINT_PER_BIT_HALF
			* (WAVE_DIFF_LEVEL & 0xff);

	/**
	 * The wave header template.
	 */
	public static final byte WAVE_HEADER[] = { 0x52, 0x49, 0x46, 0x46,
			(byte) 0xF8, 0x00, 0x00, 0x00, 0x57, 0x41, 0x56, 0x45, 0x66, 0x6D,
			0x74, 0x20, 0x10, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x44,
			(byte) 0xAC, 0x00, 0x00, 0x44, (byte) 0xAC, 0x00, 0x00, 0x01, 0x00,
			0x08, 0x00, 0x64, 0x61, 0x74, 0x61, 0x00, 0x00, 0x00, 0x00 };

	/**
	 * The value of start flag of the data packet.
	 */
	public static final byte PACKET_START_FLAG = (byte) 0xFF;

	/**
	 * 1 - on PC;<br/>
	 * 0 - on Android;
	 */
	public static final int MANCHESTER_HIGH = 1;

	/**
	 * 0 - on PC;<br/>
	 * 1 - on Android;
	 */
	public static final int MANCHESTER_LOW = 0;

	/**
	 * The magnification for the rawdata of the wave.
	 */
	public static final int MAGNIFICATION = 10;

	/**
	 * The value of mute level after amplificated.
	 */
	public static final int AMPLIFICATION_LEVEL_MUTE = Short.MAX_VALUE;
}
