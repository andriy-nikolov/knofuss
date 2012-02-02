package uk.ac.open.kmi.fusion.util;

import java.io.PrintWriter;
import java.util.Iterator;

import uk.ac.open.kmi.common.utils.Utils;
import uk.ac.open.kmi.fusion.learning.cache.CachedPair;
import uk.ac.open.kmi.fusion.learning.cache.MemoryInstanceCache;

public class LoggingUtils {

	private LoggingUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static void writeURIPairsToFile(MemoryInstanceCache cache, String fileName, boolean sampling) {
		try {
			PrintWriter writer = Utils.openPrintFileWriter(fileName);
			
			Iterator<CachedPair> iterator = cache.getComparablePairsIterator(sampling);
			
			CachedPair pair;
			while(iterator.hasNext()) {
				pair = iterator.next();
				writer.println(pair.getCandidateInstance().getUri().toString()+"\t"+pair.getTargetInstance().getUri().toString());
			}
			
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeURIPairsToFile(MemoryInstanceCache cache, String fileName) {
		try {
			PrintWriter writer = Utils.openPrintFileWriter(fileName);
			
			Iterator<CachedPair> iterator = cache.getAllPairsIterator();
			
			CachedPair pair;
			while(iterator.hasNext()) {
				pair = iterator.next();
				writer.println(pair.getCandidateInstance().getUri().toString()+"\t"+pair.getTargetInstance().getUri().toString());
			}
			
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
