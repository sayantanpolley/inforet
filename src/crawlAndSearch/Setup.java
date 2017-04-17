package crawlAndSearch;
import java.io.File;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class Setup {
	
	private static String url;
	private static int crawlDepth;
	
	public static void main11(String[] args) throws Exception {
		
		Scanner scanFromCommandLine=new Scanner(System.in);
		
		System.out.println("Enter the seed url:");
		url=scanFromCommandLine.next();
		System.out.println(url);
		
		System.out.println("Enter crawl depth");
		crawlDepth=scanFromCommandLine.nextInt();
		
		Crawler crawler = new Crawler();
		System.out.println(System.currentTimeMillis());
		//crawler.initialize(url,crawlDepth);
		System.out.println(System.currentTimeMillis());
		FileSearch filesearch=new FileSearch();
		//filesearch.searchInitializations();
		scanFromCommandLine.close();
		
	}

}
