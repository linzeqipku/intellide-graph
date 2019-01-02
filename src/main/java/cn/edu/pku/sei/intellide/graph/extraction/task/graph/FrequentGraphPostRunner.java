package cn.edu.pku.sei.intellide.graph.extraction.task.graph;

import cn.edu.pku.sei.intellide.graph.extraction.task.entity.GraphInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.entity.NodeInfo;
import cn.edu.pku.sei.intellide.graph.extraction.task.utils.ObjectIO;
import de.parsemis.graph.Graph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.Frequency;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FrequentGraphPostRunner implements Runnable {
	public static final Logger logger						= Logger
			.getLogger(FrequentGraphPostRunner.class);
	public static int							THREAD_LIMIT				= 1024;
	public static int							TIME_UNIT					= 20000;			// 询问是否挖掘完成的时间间隔

	private static long							time						= System
			.currentTimeMillis();
	private static int							liveThreadCount				= THREAD_LIMIT;		// 最后一个退出的线程作为标志
	private static int							readIndex					= 0;

	private static String						libraryName;
	private static int							validGraphCount				= 0;
	private static List<GraphInfo>				frequentSubgraphs			= new ArrayList<>();
	private static HashMap<String, Integer>		graphUUIDToFrequencyMap		= new HashMap<>();
	private static HashMap<Integer, Integer>	frequencyDistributionMap	= new HashMap<>();

	// private static int conflict=0;
	// private static int conflictunsolved=0;

	public static synchronized void stopThread() {
		liveThreadCount--;
	}

	public static synchronized int getLiveThreadCount() {
		return liveThreadCount;
	}

	public static void reset(String _libraryName) {
		libraryName = _libraryName;
		validGraphCount = 0;
		frequentSubgraphs = new ArrayList<>();
		graphUUIDToFrequencyMap = new HashMap<>();
		frequencyDistributionMap = new HashMap<>();

		liveThreadCount = THREAD_LIMIT;
		readIndex = 0;
		time = System.currentTimeMillis();
	}

	@Override
	public void run() {
		Fragment<NodeInfo, Integer> nextFragment;
		while ((nextFragment = getNext()) != null) {
			Frequency freq = nextFragment.frequency();
			Graph<NodeInfo, Integer> subgraph = nextFragment.toGraph();

			GraphInfo graphInfo = GraphParser.parseGraphAndValidate(subgraph);

			if (graphInfo != null) {
				frequentSubgraphs.add(graphInfo);

				int frequency = Integer.parseInt(freq.toString());
				if (frequency > 0 && graphInfo.getUuid() != null)
					graphUUIDToFrequencyMap.put(graphInfo.getUuid(), new Integer(frequency)); // 频数统计添加到map
				else
					System.err.println("ERROR: " + frequency + "\t" + graphInfo.getUuid());

				try {
					if (frequencyDistributionMap.get(frequency) == null)
						frequencyDistributionMap.put(frequency, new Integer(1)); // initialize
					else
						frequencyDistributionMap.put(frequency,
								new Integer(frequencyDistributionMap.get(frequency) + 1)); // add
				}
				catch (Exception e) {
					System.err.println(frequency);
					System.err.println(frequencyDistributionMap.get(frequency));
					e.printStackTrace();
				}

				// String filePath = ObjectIO.FREQUENT_SUBGRAPH + File.separator
				// + libraryName
				// + File.separator + graphInfo.getUuid() +
				// ObjectIO.DAT_FILE_EXTENSION;
				// ObjectIO.writeObject(graphInfo, filePath);// 输出子图到文件夹
				// logger.info("[" + freq.toString() + "]" +
				// graphInfo.toString());

				// if(alert){
				//// System.err.println(graphInfo.toStringWithOrderNumber());
				//// System.err.println(de.parsemis.utils.GraphUtils.isDAG(subgraph));
				// conflictunsolved++;
				// }
				validGraphCount++;
			}
		}

		stopThread();// 线程退出的时候
	}

	public static List<GraphInfo> filter(String _libraryName) {
		FrequentGraphPostRunner.reset(_libraryName);

		// 分线程读取文件
		FrequentGraphPostRunner filters[] = new FrequentGraphPostRunner[THREAD_LIMIT];
		for (int i = 0; i < THREAD_LIMIT; i++) {
			filters[i] = new FrequentGraphPostRunner();

			Thread thread = new Thread(filters[i], "graph-filter" + i);
			thread.start();
		}
		// 最后交由一个线程完成数据输出
		while (true) {
			if (liveThreadCount <= 0) {
				long t_mid = System.currentTimeMillis();
//				logger.info("[" + ((t_mid - time) / (float) 1000) + "s] " + validGraphCount
//						+ " valid graphs have been filtered and outputed.");
				System.out.println("[" + ((t_mid - time) / (float) 1000) + "s] " + validGraphCount
						+ " valid graphs have been filtered and outputed.");
				// logger.info("Conflict: "+conflict+"
				// conflictunsolved:"+conflictunsolved);
				String graphListFile = ObjectIO.FREQUENT_SUBGRAPH_LIBRARY + File.separator
						+ ObjectIO.FREQUENCY_GRAPH_LIST_PREFIX + libraryName
						+ ObjectIO.DAT_FILE_EXTENSION;
				ObjectIO.writeObject(frequentSubgraphs, graphListFile);// 输出过滤后的freqGraphList到文件

				String freqFile = ObjectIO.FREQUENT_SUBGRAPH_LIBRARY + File.separator
						+ ObjectIO.FREQUENCY_FILE_PREFIX + libraryName
						+ ObjectIO.DAT_FILE_EXTENSION;
				ObjectIO.writeMapToFile(graphUUIDToFrequencyMap, freqFile);// 输出map的键值对到文件

				String uuidFreqMapFile = ObjectIO.FREQUENT_SUBGRAPH_LIBRARY + File.separator
						+ ObjectIO.FREQUENCY_MAP_PREFIX + libraryName + ObjectIO.DAT_FILE_EXTENSION;
				ObjectIO.writeObject(graphUUIDToFrequencyMap, uuidFreqMapFile);// 输出uuid-频数map到文件

				Object mapObj = ObjectIO.readObject(ObjectIO.getDataObjDirectory(uuidFreqMapFile));
				if (mapObj == null) {
//					logger.error("Frequent map invalid, write map to file.." + freqFile);
					System.out.println("Frequent map invalid, write map to file.." + freqFile);
				}

				// String freqDistributionFile =
				// ObjectIO.FREQUENT_SUBGRAPH_LIBRARY + File.separator
				// + ObjectIO.FREQUENCY_DISTRIBUTION_PREFIX + libraryName
				// + ObjectIO.DAT_FILE_EXTENSION;
				// ObjectIO.writeObject(frequencyDistributionMap,
				// freqDistributionFile);// 输出频数分布map到文件
//				logger.error(frequencyDistributionMap);
				System.out.println(frequencyDistributionMap);

				long t_end = System.currentTimeMillis();
//				logger.info("Output \"Frequency\" maps to file: " + (t_end - t_mid) + "ms");
				System.out.println("Output \"Frequency\" maps to file: " + (t_end - t_mid) + "ms");

				break;
			}

			try {
				Thread.sleep(TIME_UNIT);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return frequentSubgraphs;
	}

	// 多线程处理时，获取下一个应处理的图
	private static synchronized Fragment<NodeInfo, Integer> getNext() {
		if (FrequentGraphMiner.getFrequentSubgraphs() == null)
			return null;
		Fragment<NodeInfo, Integer> next = null;
		try {
			if (readIndex >= FrequentGraphMiner.getFrequentSubgraphs().size())
				return null;
			next = FrequentGraphMiner.getFrequentSubgraphs().get(readIndex);
			readIndex++;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return next;
	}

	public static String getLibraryName() {
		return libraryName;
	}

	public static void setLibraryName(String libraryName) {
		FrequentGraphPostRunner.libraryName = libraryName;
	}

	public static HashMap<String, Integer> getGraphUUIDToFrequencyMap() {
		return graphUUIDToFrequencyMap;
	}

	public static void setGraphUUIDToFrequencyMap(HashMap<String, Integer> map) {
		graphUUIDToFrequencyMap = map;
	}

	public static HashMap<Integer, Integer> getFrequencyDistributionMap() {
		return frequencyDistributionMap;
	}

	public static void setFrequencyDistributionMap(
			HashMap<Integer, Integer> frequencyDistributionMap) {
		FrequentGraphPostRunner.frequencyDistributionMap = frequencyDistributionMap;
	}

}
