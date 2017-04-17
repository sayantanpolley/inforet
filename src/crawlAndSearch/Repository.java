/*Contains repository of file location.
 */
package crawlAndSearch;
/*Contains repository of file location.
 */

import java.io.File;
import java.io.IOException;
public class Repository {
	private static final String userDirectoryPath=".";
	//public final String currentDirectoryPath;
	public final String indexFolder; // for lucene index
	public static String crawlerFolder; // for crawled images
	public static String searchedDocsFolder; // temp storage for weka files, clustering

	public Repository() throws IOException{
		//currentDirectoryPath = getCurrentDirPath();
		indexFolder="Index";
		//crawlerFolder="//home/sayantan/InstalledSoftwares/helios_working_direc/IRP3V01/WebContent/pics";
		//crawlerFolder="Crawler";
		searchedDocsFolder="SearchedDocsFolder";
		
	}
//	public String getCurrentDirPath() throws IOException{
//		return (new File(userDirectoryPath)).getCanonicalPath();
//	}
}

