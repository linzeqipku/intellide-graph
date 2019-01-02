package cn.edu.pku.sei.intellide.graph.extraction.task.utils;

//import cn.edu.pku.sei.tsr.dragon.content.entity.ContentInfo;
//import cn.edu.pku.sei.tsr.dragon.stackoverflow.entity.ThreadInfo;
import com.google.common.io.Files;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author ZHUZixiao
 *
 */
public class ObjectIO {
	private static final Logger logger							= Logger.getLogger(ObjectIO.class);

	public static final String	DAT_FILE_EXTENSION				= ".dat";
	public static final String	DAT_FILE_NAME					= "threads";
	public static final String	FREQUENCY_MAP_PREFIX			= "[FrequencyMap]";
	public static final String	FREQUENCY_DISTRIBUTION_PREFIX	= "[FrequencyDistribution]";
	public static final String	FREQUENCY_FILE_PREFIX			= "[FreqFile]";
	public static final String	FREQUENCY_GRAPH_LIST_PREFIX		= "[FreqGraphList]";

	// public static final String RAW_SODATA = "raw_sodata";
	// public static final String AFTER_TREE_PARSE = "tree_parsed";
	// public static final String AFTER_EXTRACTION = "phrase_extracted";
	// public static final String CONTENTPOOL_UUID = "contentpool_uuid";
	// public static final String CONTENTPOOL = "contentpool";
	// public static final String PARSED_CONTENTS_UUID = "parsed_contents_uuid";
	// public static final String PARSED_CONTENTS = "parsed_contents";
	// public static final String PARSED_CONTENTS_LIBRARY =
	// "parsed_contents_library";

	public static final String	SOCORPUS						= "socorpus";
	public static final String	SOTHREAD_LIBRARY				= "sothread_library";
	public static final String	CODE_DATA						= "code_data";
	public static final String	CONTENTPOOL_LIBRARY				= "contentpool_library";
	public static final String	CONTENTPOOL_FROMCOMMENT			= "contentpool_fromcomment";
	public static final String	SENTENCEPOOL_LIBRARY			= "sentencepool_library";
	public static final String	STRUCTURED_SENTENCE_LIBRARY		= "structured_sentence_library";
	public static final String	CONTENT_STRUCTURED_LIBRARY		= "content_structured_library";
	public static final String	PHRASE_LIBRARY					= "phrase_library";
	public static final String	STRUCTURE_LIBRARY				= "structure_library";
	public static final String	GRAPH_LIBRARY					= "graph_library";
	public static final String	FREQUENT_SUBGRAPH				= "frequent_subgraph";
	public static final String	FREQUENT_SUBGRAPH_LIBRARY		= "frequent_subgraph_library";
	public static final String	FREQUENT_SUBGRAPH_STR			= "frequent_subgraph_str";
	public static final String	FREQUENT_SUBGRAPH_STR_LIBRARY	= "frequent_subgraph_str_library";
	public static final String	HIERARCHICAL_FEATURES			= "hierarchical_features";
	public static final String	NEW_HIERARCHICAL_FEATURES		= "new_hierarchical_features";
	public static final String	SUBJECTS						= "subjects";
	public static final String	FEATURE_VERB_PHRASE_STRUCTURE	= "feature_vps";
	public static final String	CODE_MATCHER					= "code_matcher";

	public static final String	CONTENT							= "content";
	public static final String	SOCODE							= "socode";

	public static int			count							= 0;
	public static long			start_clock						= System.currentTimeMillis();
	public static long			clock							= System.currentTimeMillis();

	public static File getFile(String path) {
		File file = new File(path);
		File parentDir = file.getParentFile();
		if (!parentDir.exists())
			parentDir.mkdirs();
		return file;
	}

	public static boolean writeString(String content, String path) {
		try {
			File file = new File(path);
			File parentDir = file.getParentFile();
			if (!parentDir.exists())
				parentDir.mkdirs();

			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.flush();
			fw.close();
		}
		catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean writeObject(Object obj, String path) {
		ObjectOutputStream objOut;
		try {
			File file = new File(path);
			File parentDir = file.getParentFile();
			if (!parentDir.exists())
				parentDir.mkdirs();

			objOut = new ObjectOutputStream(new FileOutputStream(file));

			// synchronized (objOut) {
			objOut.writeObject(obj);
			// }

			objOut.flush();
			objOut.close();
			objOut = null;
		}
		catch (IOException e) {
//			logger.error(e.getMessage());
			System.out.println(e.getMessage());
			e.printStackTrace();
			objOut = null;
			return false;
		}
		return true;
	}

	public static File getDataObjDirectory(String subDirName) {
//		return new File(Config.getDataObjDir() + File.separator + subDirName);
		return new File("D:\\Dragon Project\\data-np" + File.separator + subDirName);
	}

//	public static File getDataCodeObjDirectory(String subDirName) {
//		return new File(Config.getDataCodeObjDir() + File.separator + subDirName);
//	}

	public static Object readObject(String filePath) {
		return readObject(new File(filePath));
	}

	public static Object readObject(File file) {
		ObjectInputStream objIn;
		try {
			if (file == null || !file.exists())
				return null;

			objIn = new ObjectInputStream(new FileInputStream(file));
			Object object = objIn.readObject();
			objIn.close();
			objIn = null;
			return object;
		}
		catch (Exception e) {
//			logger.error("ObjectIO.readObject: " + file.getAbsolutePath());
			System.out.println("ObjectIO.readObject: " + file.getAbsolutePath());
			e.printStackTrace();
			objIn = null;
			return null;
		}
	}

//	/**
//	 * @param phase:
//	 *            数据是哪个处理阶段的
//	 * @return
//	 */
//	public static List<ThreadInfo> readAllThreads(String phase) {
//		List<ThreadInfo> threadList = new ArrayList<ThreadInfo>();
//
//		String dirPath = Config.getDataObjDir() + File.separator + phase;
//
//		try {
//			File rootDir = new File(dirPath);
//			File[] subDirs = rootDir.listFiles();
//			for (int i = 0; i < subDirs.length; i++) {
//				List<ThreadInfo> subList = readThreadsFromDir(subDirs[i]);
//				if (subDirs != null)
//					threadList.addAll(subList);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return threadList;
//	}

//	/**
//	 * @param libraryName:LibraryInfo.XXX
//	 * @param contentState:CONTENTPOOL/CONTENT_STRUCTURED
//	 * @return
//	 */
//	public static List<ContentInfo> readContentsOfLibrary(String contentState, String libraryName) {
//		String dirPath = Config.getDataObjDir() + File.separator + contentState + File.separator
//				+ libraryName;
//		File rootDir = new File(dirPath);
//		return readContentsFromDir(rootDir);
//
//	}

//	public static List<ContentInfo> readContentsFromDir(File dir) {
//		List<ContentInfo> contentList = new ArrayList<>();
//		List<Object> objList = readObjectsFromDir(dir);
//		if (objList != null && objList.size() > 0) {
//			objList.forEach(obj -> {
//				if (obj != null && obj instanceof ContentInfo) {
//					ContentInfo content = (ContentInfo) obj;
//					contentList.add(content);
//				}
//			});
//		}
//		return contentList;
//	}

//	/**
//	 * @param libraryName:LibraryInfo.XXX
//	 * @return
//	 */
//	public static List<ThreadInfo> readThreadsOfLibrary(String libraryName) {
//		String dirPath = Config.getDataObjDir() + File.separator + SOTHREAD_LIBRARY + File.separator
//				+ libraryName;
//		File rootDir = new File(dirPath);
//		return readThreadsFromDir(rootDir);
//
//	}

//	public static List<ThreadInfo> readThreadsFromDir(File dir) {
//		List<ThreadInfo> threadList = new ArrayList<>();
//		List<Object> objList = readObjectsFromDir(dir);
//		if (objList != null && objList.size() > 0) {
//			objList.forEach(obj -> {
//				if (obj != null && obj instanceof ThreadInfo) {
//					ThreadInfo thread = (ThreadInfo) obj;
//					threadList.add(thread);
//				}
//			});
//		}
//		return threadList;
//	}

	public static List<Object> readObjectsFromDir(File dir) {
		List<Object> objList = new ArrayList<>();
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return null;

		try {
			File[] dataFiles = dir.listFiles();
			for (int j = 0; j < dataFiles.length; j++) {
				File file = dataFiles[j];
				Object obj = readObject(file);
				if (obj != null)
					objList.add(obj);
				count++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return objList;
	}

	public static void writeMapToFile(HashMap<String, Integer> freqMap, String filePath) {
		try {
//			File file = new File(Config.getDataObjDir() + File.separator + filePath);
			File file = new File("D:\\Dragon Project\\data-np" + File.separator + filePath);
			File parentDir = file.getParentFile();
			if (!parentDir.exists())
				parentDir.mkdirs();

			RandomAccessFile raFile = new RandomAccessFile(file, "rw");
			for (Entry<String, Integer> entry : freqMap.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null)
					raFile.writeBytes(entry.getKey() + "\t" + entry.getValue() + "\n");
			}
			raFile.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public static HashMap<String, Integer> readMapFromFile(String filePath) {
//		try {
//			File file = new File(Config.getDataObjDir() + File.separator + filePath);
//			List<String> lines = new ArrayList<String>();
//
//			try {
//				lines = Files.readLines(file, Charset.defaultCharset());
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//			HashMap<String, Integer> freqMap = new HashMap<>();
//
//			for (int i = 0; i < lines.size(); i++) {
//				String line = lines.get(i);
//				String[] frag = line.split("\\t");
//				if (frag.length == 2) {
//					try {
//						Integer freq = new Integer(frag[1]);
//						if (freq != null)
//							freqMap.put(frag[0], freq);
//					}
//					catch (Exception e) {
//						// TODO: handle exception
//					}
//				}
//			}
//			return freqMap;
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			logger.error(filePath);
//			return null;
//		}
//	}
	public static void main(String[] args) {
		File dir = new File("D:\\Dragon Project\\data\\code_data");
		if (dir.isDirectory())
			dir.delete();

		HashMap<String, Integer> freqMap = new HashMap<>();

		// for (int i = 0; i < lines.size(); i++) {
		String line = "9f0af464-f337-4298-a383-ba50242af265\t2";
		String[] frag = line.split("\\t");
		if (frag.length == 2) {
			try {
				Integer freq = new Integer(frag[1]);
				if (freq != null)
					freqMap.put(frag[0], freq);
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		// }
		System.err.println(freqMap);
	}
}
