package OSProjects;

public class Parameter {
	static public char SIZE_OF_FILEINFO = 16;
	static public char SIZE_OF_SECTOR = 512;
	static public char NUM_OF_ROOTFILE =
		(char) (SIZE_OF_SECTOR / SIZE_OF_FILEINFO);
	static public char NUM_OF_SUBFILE =
		(char) (SIZE_OF_SECTOR / SIZE_OF_FILEINFO);
	static public char NUM_OF_DATASECTOR = 3 * 1024;
	static public char END_OF_FILE = '#'; //0xffff; 	
	static public char FREE_FOR_FAT = '*'; //0xfffe;
	static public char POS_NAME = 0;
	static public char LEN_OF_NAME = 12;
	static public char POS_STATE = 12;
	static public char POS_FAT = 13;
	static public char POS_SIZE = 14;
	static public char POS_READONLY = 15;

	static public char FREE = 100;
	static public char FILE = 101;
	static public char DIRECTORY = 102;
	static public char MAX_SECTOR = 100;

	static public final int WIDTH = 700;
	static public final int HEIGHT = 500;
	
	static public final String INIT_FILE ="deploy.ini";
}