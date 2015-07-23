package OSProjects;

import java.util.ArrayList;

public class FileManager {
	char[][] rootTable;

	char[] fatTable;

	char[][] dataArea;

	ArrayList directoryStack;

	ArrayList path;

	private void test() {
	}

	public FileManager() {
		System.out.println("Create filemanager!");
		rootTable = new char[Parameter.NUM_OF_ROOTFILE][Parameter.SIZE_OF_FILEINFO];
		fatTable = new char[Parameter.NUM_OF_DATASECTOR];
		dataArea = new char[Parameter.NUM_OF_DATASECTOR][Parameter.SIZE_OF_SECTOR];

		this.formatAll();

	}

	/**
	 * ÅäÖÃÎÄ¼þ´æ´¢»·¾³
	 */
	private boolean deploy() {

		return true;
	}

	/**
	 * ¸ñÊ½»¯ºÍÇå³ýÐÅÏ¢
	 */
	void formatAll() {
		for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
			rootTable[i][Parameter.POS_STATE] = Parameter.FREE;
			rootTable[i][Parameter.POS_FAT] = Parameter.FREE_FOR_FAT;
		}

		for (int i = 0; i < Parameter.NUM_OF_DATASECTOR; i++) {
			fatTable[i] = Parameter.FREE_FOR_FAT;
		}

		directoryStack = new ArrayList();
		directoryStack.add(new Integer(-1));
		path = new ArrayList();
		path.add("MyRoot:");
	}

	void formatSectorForData(int freeIndex) {
		for (int i = 0; i < Parameter.SIZE_OF_SECTOR; i++) {
			dataArea[freeIndex][i] = 0;
		}
	}

	void formatSectorForDir(int freeIndex) {
		char[] sector = dataArea[freeIndex];
		for (int i = 0; i < Parameter.NUM_OF_SUBFILE; i++) {
			sector[i * Parameter.SIZE_OF_FILEINFO + Parameter.POS_STATE] =
				Parameter.FREE;
			sector[i * Parameter.SIZE_OF_FILEINFO + Parameter.POS_FAT] =
				Parameter.FREE_FOR_FAT;
		}
	}

	/**
	 * ÐÂ½¨Ä¿Â¼»òÎÄ¼þÐÅÏ¢Ïî
	 */
	boolean createInfo(char type, String name) {
		if (name.length() > 12) {
			return false;
		}
		int currentDirectory =
			((Integer) directoryStack.get(directoryStack.size() - 1))
				.intValue();
		int fatIndex;

		if (this.inDirectory(type, name, false) != -1) {
			System.out.println(
				"collide fatIndex:" + inDirectory(type, name, false));
			return false;
		}

		if (currentDirectory == -1) {
			for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
				if (rootTable[i][Parameter.POS_STATE] == Parameter.FREE) {
					if ((fatIndex = getFreeSector(type)) != -1) {
						rootTable[i][Parameter.POS_FAT] = (char) fatIndex;
						rootTable[i][Parameter.POS_STATE] = type;
						this.changeNameOfFileInfo(
							rootTable[i],
							Parameter.POS_NAME,
							name);
						return true;
					}
					return false;
				}
			}
			return false;
		}

		char[] subDirectory = dataArea[currentDirectory];

		for (int i = 0; i < Parameter.NUM_OF_SUBFILE; i++) {
			if (subDirectory[i * Parameter.SIZE_OF_FILEINFO + Parameter.POS_STATE]
				== Parameter.FREE) {
				if ((fatIndex = getFreeSector(type)) != -1) {
					subDirectory[i * Parameter.SIZE_OF_FILEINFO
						+ Parameter.POS_FAT] =
						(char) fatIndex;
					subDirectory[i * Parameter.SIZE_OF_FILEINFO
						+ Parameter.POS_STATE] =
						type;
					this.changeNameOfFileInfo(
						subDirectory,
						i * Parameter.SIZE_OF_FILEINFO,
						name);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	private void testCreate() {
		System.out.println("------testCreate--------");
		this.createInfo(Parameter.FILE, "file1");
		this.createInfo(Parameter.DIRECTORY, "dir1");
	}

	int getFreeSector(char type) {
		for (int i = 0; i < Parameter.NUM_OF_DATASECTOR; i++) {
			if (fatTable[i] == Parameter.FREE_FOR_FAT) {
				if (type == Parameter.FILE) {
					this.formatSectorForData(i);
				} else if (type == Parameter.DIRECTORY) {
					this.formatSectorForDir(i);
				}
				fatTable[i] = Parameter.END_OF_FILE;
				return i;
			}
		}
		return -1;
	}

	void changeNameOfFileInfo(char[] fileInfo, int index, String name) {
		for (int i = 0; i < Parameter.LEN_OF_NAME; i++) {
			fileInfo[index + i] = 0;
		}
		for (int i = 0; i < name.length(); i++) {
			fileInfo[index + i] = name.charAt(i);
		}
	}

	/**
	 * ÐÞ¸ÄÎÄ¼þËùÐèµÄÉÈÇø
	 */
	public boolean loadFile(String name, StringBuffer content) {
		int fatIndex = inDirectory(Parameter.FILE, name, false);
		if (fatIndex == -1) {
			return false;
		}

		int nextIndex = fatTable[fatIndex];
		while (true) {
			content.append(
				String
					.valueOf(dataArea[fatIndex], 0, Parameter.SIZE_OF_SECTOR)
					.trim());
			System.out.println("file content : " + content.toString());
			if (nextIndex == Parameter.END_OF_FILE) {
				return true;
			}
			fatIndex = nextIndex;
			nextIndex = fatTable[fatIndex];
		}
	}

	public boolean writeFile(String name, String content) {
		int fatIndex = inDirectory(Parameter.FILE, name, false);
		if (fatIndex == -1) {
			return false;
		}
		if (content.length() == 0) {
			return true;
		}

		int objectTotal =
			content.length() % Parameter.SIZE_OF_SECTOR == 0
				? content.length() / Parameter.SIZE_OF_SECTOR
				: content.length() / Parameter.SIZE_OF_SECTOR + 1;

		if (this.modifySector(name, objectTotal) == false) {
			return false;
		}

		int bufferIndex = 0;
		int bufferLeft = content.length();

		for (int i = 0; i < objectTotal; i++) {
			if (i == objectTotal - 1) {
				for (int j = 0; j < bufferLeft; j++) {
					char a = content.charAt(bufferIndex++);
					this.dataArea[fatIndex][j] = a;
				}
			} else {
				for (int k = 0; k < Parameter.SIZE_OF_SECTOR; k++) {
					this.dataArea[fatIndex][k] = content.charAt(bufferIndex++);
				}
			}
			bufferLeft = bufferLeft - Parameter.SIZE_OF_SECTOR;
			fatIndex = fatTable[fatIndex];
		}
		return true;
	}

	boolean modifySector(String name, int objectTotal) {
		int fatIndex = this.inDirectory(Parameter.FILE, name, false);
		if (fatIndex == -1) {
			return false;
		}
		showFatList(fatIndex);

		int initIndex = fatIndex;
		int[] fatArray = new int[Parameter.MAX_SECTOR];
		int arrayIndex = -1;

		fatArray[++arrayIndex] = fatIndex;
		while (true) {
			if (fatTable[fatIndex] != Parameter.END_OF_FILE) {
				fatArray[++arrayIndex] = fatTable[fatIndex];
				fatIndex = fatArray[arrayIndex];
			} else
				break;
		}
		int orientTotal = arrayIndex + 1;
		int diff = Math.abs(objectTotal - orientTotal);
		if (objectTotal > orientTotal) {
			int freeIndex;
			for (int i = 0; i < diff; i++) {
				freeIndex = getFreeSector(Parameter.FILE);
				if (freeIndex == -1) {
					return false;
				}
				fatArray[++arrayIndex] = freeIndex;
			}
			arrayIndex = orientTotal - 1;
			for (int i = 0; i < diff; i++) {
				fatTable[fatArray[arrayIndex]] =
					(char) fatArray[arrayIndex + 1];
				arrayIndex++;
			}
			fatTable[fatArray[arrayIndex]] = Parameter.END_OF_FILE;
			showFatList(initIndex);
		}
		if (objectTotal < orientTotal) {
			arrayIndex = objectTotal - 1;
			fatTable[arrayIndex] = Parameter.END_OF_FILE;
			for (int i = 1; i <= diff; i++) {
				fatTable[fatArray[arrayIndex + i]] = Parameter.FREE_FOR_FAT;
			}
			showFatList(initIndex);
		}
		return true;
	}

	private void showFatList(int fatIndex) {
		System.out.print("Fat List : " + fatIndex);
		while (fatTable[fatIndex] != Parameter.END_OF_FILE) {
			fatIndex = fatTable[fatIndex];
			System.out.print(" " + fatIndex);
		}
		System.out.println(" " + fatTable[fatIndex]);
	}

	private void testModify() {
		System.out.println("------testModify--------");
		this.showCurrentDirInfo();
		this.createInfo(Parameter.FILE, "file1");
		this.createInfo(Parameter.DIRECTORY, "dir1");
		this.showCurrentDirInfo();
		this.changeDirectory("dir1");
		this.createInfo(Parameter.FILE, "dir1");
		this.showCurrentDirInfo();
		modifySector("dir1", 3);
		this.showCurrentDirInfo();
	}

	/**
	 * É¾³ýÄ¿Â¼»òÎÄ¼þÐÅÏ¢Ïî
	 */
	boolean deleteInfo(char type, String name) {
		if (type == Parameter.FILE) {
			int fatIndex = this.inDirectory(Parameter.FILE, name, true);
			if (fatIndex != -1) {
				deleteSectorList(fatIndex);
				return true;
			}
		} else if (type == Parameter.DIRECTORY) {
			int fatIndex = this.inDirectory(Parameter.DIRECTORY, name, true);
			if (fatIndex != -1) {
				deleteSubDir(fatIndex);
				return true;
			}
		}
		return false;
	}

	private void deleteSubDir(int fatIndex) {
		deleteSectorList(fatIndex);
		char[] subDirectory = dataArea[fatIndex];
		for (int i = 0; i < Parameter.NUM_OF_SUBFILE; i++) {
			int initPos = i * Parameter.SIZE_OF_FILEINFO;
			if (subDirectory[initPos + Parameter.POS_STATE] != Parameter.FREE) {
				if (subDirectory[initPos + Parameter.POS_STATE]
					== Parameter.FILE) {
					deleteSectorList(subDirectory[initPos + Parameter.POS_FAT]);
				} else if (
					subDirectory[initPos + Parameter.POS_STATE]
						== Parameter.DIRECTORY) {
					deleteSubDir(subDirectory[initPos + Parameter.POS_FAT]);
				}
				subDirectory[initPos + Parameter.POS_STATE] = Parameter.FREE;
			}
		}
	}

	void deleteSectorList(int firstIndex) {
		if (firstIndex < 0 || firstIndex > Parameter.NUM_OF_DATASECTOR) {
			return;
		}

		int nextIndex = fatTable[firstIndex];
		while (true) {
			fatTable[firstIndex] = Parameter.FREE_FOR_FAT;
			if (nextIndex == Parameter.END_OF_FILE) {
				return;
			}
			firstIndex = nextIndex;
			nextIndex = fatTable[firstIndex];
		}
	}

	private void testDelete() {
		System.out.println("------testDelete--------");
		this.showCurrentDirInfo();

		changeDirectory("dir1");
		this.showCurrentDirInfo();

		changeDirectory("dir2");
		this.showCurrentDirInfo();

		changeDirectory("dir3");
		this.showCurrentDirInfo();

		changeDirectory("\\");
		this.showCurrentDirInfo();

		deleteInfo(Parameter.FILE, "file1");
		this.showCurrentDirInfo();

		deleteInfo(Parameter.DIRECTORY, "dir1");
		this.showCurrentDirInfo();

		this.createInfo(Parameter.FILE, "file6");
		this.createInfo(Parameter.DIRECTORY, "dir6");
		this.showCurrentDirInfo();

		changeDirectory("dir6");
		this.createInfo(Parameter.FILE, "file7");
		this.showCurrentDirInfo();
		this.createInfo(Parameter.DIRECTORY, "dir7");
		this.showCurrentDirInfo();

		this.changeDirectory("\\");
		this.deleteInfo(Parameter.FILE, "file6");
		changeDirectory("dir6");
		changeDirectory("dir7");
		this.showCurrentDirInfo();

		this.createInfo(Parameter.FILE, "file8");
		this.createInfo(Parameter.DIRECTORY, "dir8");
		this.showCurrentDirInfo();

	}

	/**
	 * ¸Ä±äµ±Ç°Ä¿Â¼
	 */
	boolean changeDirectory(String name) {
		if (name.length() > 12) {
			return false;
		}

		if (name.compareTo(".") == 0) {
			return true;
		}

		if (name.compareTo("..") == 0) {
			if (directoryStack.size() <= 1) {
				return true;
			}
			path.remove(path.size() - 1);
			directoryStack.remove(directoryStack.size() - 1);
			return true;
		}

		if (name.compareTo("\\") == 0) {
			int deleteSize = directoryStack.size() - 1;
			for (int i = 1; i <= deleteSize; i++) {
				path.remove(1);
				directoryStack.remove(1);
			}
			return true;
		}

		int fatIndex = inDirectory(Parameter.DIRECTORY, name, false);
		if (fatIndex != -1) {
			path.add(name);
			directoryStack.add(new Integer(fatIndex));
			return true;
		}
		return false;
	}

	private void testChangeDir() {
		System.out.println("------testChangeDir--------");

		if (!changeDirectory("dir1"))
			System.out.println("can't change");

		this.createInfo(Parameter.FILE, "file2");
		this.createInfo(Parameter.DIRECTORY, "dir2");
		if (!changeDirectory("dir2"))
			System.out.println("can't change");

		this.createInfo(Parameter.FILE, "file3");
		this.createInfo(Parameter.DIRECTORY, "dir3");
		if (!changeDirectory("dir3"))
			System.out.println("can't change");

		if (!changeDirectory("\\"))
			System.out.println("can't change");
	}

	/**
	 * Ä¿Â¼ÐÅÏ¢¶ÁÈ¡ºÍÅÐ¶Ï
	 */
	int inDirectory(char type, String name, boolean delete) {
		String tempName;
		if (((Integer) directoryStack.get(directoryStack.size() - 1))
			.intValue()
			== -1) {
			if (type == Parameter.FILE) {
				for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
					if (rootTable[i][Parameter.POS_STATE] == Parameter.FILE) {
						tempName =
							new String(
								rootTable[i],
								Parameter.POS_NAME,
								Parameter.LEN_OF_NAME)
								.trim();
						if (tempName.compareTo(name) == 0) {
							if (delete == true) {
								rootTable[i][Parameter.POS_STATE] = Parameter.FREE;
							}
							return rootTable[i][Parameter.POS_FAT];
						}
					}
				}
			} else if (type == Parameter.DIRECTORY) {
				for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
					if (rootTable[i][Parameter.POS_STATE] == Parameter.DIRECTORY) {
						tempName =
							new String(
								rootTable[i],
								Parameter.POS_NAME,
								Parameter.LEN_OF_NAME)
								.trim();
						if (tempName.compareTo(name) == 0) {
							if (delete == true) {
								rootTable[i][Parameter.POS_STATE] = Parameter.FREE;
							}
							return rootTable[i][Parameter.POS_FAT];
						}
					}
				}
			}
			return -1;
		}

		int fatIndex =
			((Integer) directoryStack.get(directoryStack.size() - 1))
				.intValue();
		char[] subDirectory = dataArea[fatIndex];

		if (type == Parameter.FILE) {
			for (int i = 0; i < Parameter.NUM_OF_SUBFILE; i++) {
				if (subDirectory[i * Parameter.SIZE_OF_FILEINFO
					+ Parameter.POS_STATE]
					== Parameter.FILE) {
					tempName =
						new String(
							subDirectory,
							i * Parameter.SIZE_OF_FILEINFO + Parameter.POS_NAME,
							Parameter.LEN_OF_NAME)
							.trim();
					if (tempName.compareTo(name) == 0) {
						if (delete == true) {
							subDirectory[i * Parameter.SIZE_OF_FILEINFO
								+ Parameter.POS_STATE] =
									Parameter.FREE;
						}
						return subDirectory[i * Parameter.SIZE_OF_FILEINFO
							+ Parameter.POS_FAT];
					}
				}
			}
		} else if (type == Parameter.DIRECTORY) {
			for (int i = 0; i < Parameter.NUM_OF_SUBFILE; i++) {
				if (subDirectory[i * Parameter.SIZE_OF_FILEINFO
					+ Parameter.POS_STATE]
					== Parameter.DIRECTORY) {
					tempName =
						new String(
							subDirectory,
							i * Parameter.SIZE_OF_FILEINFO + Parameter.POS_NAME,
							Parameter.LEN_OF_NAME)
							.trim();
					if (tempName.compareTo(name) == 0) {
						if (delete == true) {
							subDirectory[i * Parameter.SIZE_OF_FILEINFO
								+ Parameter.POS_STATE] =
									Parameter.FREE;
						}
						return subDirectory[i * Parameter.SIZE_OF_FILEINFO
							+ Parameter.POS_FAT];
					}
				}
			}
		}
		return -1;
	}

	ArrayList getCurrentDirInfo() {
		ArrayList infoList = new ArrayList();
		int currentDirectory =
			((Integer) directoryStack.get(directoryStack.size() - 1))
				.intValue();
		System.out.println("currentDirectory : " + currentDirectory);
		if (currentDirectory == -1) {
			for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
				if (rootTable[i][Parameter.POS_STATE] != Parameter.FREE) {
					infoList.add(new String(rootTable[i]));
				}
			}
			return infoList;
		}

		char[] subDirectory = dataArea[currentDirectory];
		for (int i = 0; i < Parameter.NUM_OF_SUBFILE; i++) {
			if (subDirectory[i * Parameter.SIZE_OF_FILEINFO + Parameter.POS_STATE]
				!= Parameter.FREE) {
				String addStr =
					new String(
						subDirectory,
						i * Parameter.SIZE_OF_FILEINFO,
						Parameter.SIZE_OF_FILEINFO);
				infoList.add(addStr);
			}
		}
		return infoList;
	}

	private void showCurrentDirInfo() {
		ArrayList infoList = this.getCurrentDirInfo();
		String name;
		char state, fatIndex, size, readonly;
		for (int i = 0; i < infoList.size(); i++) {
			String temp = (String) infoList.get(i);
			name = temp.substring(0, 12);
			state = temp.charAt(12);
			fatIndex = temp.charAt(13);
			size = temp.charAt(14);
			readonly = temp.charAt(15);
			System.out.println(
				"Name : "
					+ name.trim()
					+ "  State:"
					+ (int) state
					+ "  Fat:"
					+ (int) fatIndex
					+ "  Size:"
					+ (int) size
					+ "  ReadOnly:"
					+ (int) readonly);
		}
		if (infoList.size() == 0) {
			System.out.println("Empty!!");
		}
		return;
	}

	public String getCurrentPath() {
		String pathStr = "", temp;
		for (int i = 0; i < path.size(); i++) {
			temp = (String) path.get(i);
			pathStr = pathStr + temp + "\\";
		}
		pathStr = pathStr + ">";
		System.out.println(pathStr);
		return pathStr;
	}

}
