package crawlAndSearch;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sun.security.validator.ValidatorException;


// 1. Visit the url.
// 2. Extract url,text and images of specific format.
//0. Store all urls to be visited in an array
public class Crawler {

	public String urlFolder;
	public String urlFolderName;
	public int noOfURLS=0;
	//contains a ArrayList of urls
	private ArrayList<String> links;
	//pages visited should be unique. Hence Set. Sets wont add duplicate entries
	private Set<String> pagesVisited;
	String splChrs = "-/@#$%^&_+=():.| " ;
	private static final Pattern filters = Pattern.compile(
			".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|ppt|pptx|doc|docx|pdf" +
			"|rm|smil|wmv|swf|wma|zip|rar|gz|jar))$");

	Indexing indexing;
	Path crawlerPath;
	String href=null;
	public Crawler(){
	
	}

	public Crawler(String contextPath) throws IOException{
		//repository = new Repository();
		//crawler//title
		//crawlerPath = Paths.get(repository.crawlerFolder);
		crawlerPath = Paths.get(contextPath,"pics","TempCrawl"); //IRPEV01/TempCrawl - "TempCrawl"
		//boolean picsFolder= new File(crawlerPath.toString()).mkdir();//creating a directory for storing images
		Files.createDirectories(crawlerPath); // creates both pics and temp at the same time
		//System.out.println(" folder (for images created?) = "+crawlerPath);
		
		indexing = new Indexing(contextPath);
		links = new ArrayList<String>();
		pagesVisited = new HashSet<String>();

	}
	
	public void initialize(String url,int crawlDepth,int maxURLs){

		try {
			crawlUrl(url,crawlDepth,maxURLs);
			setNoOfURLS(pagesVisited.size());
			//System.out.println("pagesVisited = "+pagesVisited);
			//System.out.println("links = "+links);
			indexing.indexWriter.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public int getNoOfURLS() {
		return noOfURLS;
	}

	public void setNoOfURLS(int noOfURLS) {
		this.noOfURLS = noOfURLS;
	}

	public void crawlUrl(String url, int crawlDepth,int maxURLs) throws IOException{
		
		if(pagesVisited.size()<=maxURLs){
			pagesVisited.add(url.trim());
		if(crawlDepth==0){
			crawlingUrl(url);
		}else{
			while(crawlDepth > 0){
				crawlingUrl(url);
				crawlDepth--;
				Document jsDocumentTemp = Jsoup.connect(url).get();
				Elements linksOnPage = jsDocumentTemp.select("a[href]");
				for(Element link : linksOnPage){
					//Normalize the url.
					String href=link.absUrl("href").trim();
					//Check if link already exists in links object. If it does, dont add again. This is a way to normalize the url(removing duplicates)
					if(!pagesVisited.contains(link.absUrl("href").trim()) && (!this.links.contains(link.absUrl("href").trim())) && (!filters.matcher(href).matches())){
						this.links.add(link.absUrl("href").trim());
						crawlUrl(link.absUrl("href").trim(),crawlDepth,maxURLs);
					}
				}
			} 
		}
		//System.out.println("Crawled WebPages "+pagesVisited.size());
		}

	}
	public void crawlingUrl(String url){
		long totalExecutionTime = 8L;
		long startTime;

		try {
			//visit the url and get the contents from the webpage
			//if(!url.isEmpty())
			//	pagesVisited.add(url);
			Document jsDocument = Jsoup.connect(url).get();
			//print the title or something
			String title = jsDocument.title().toLowerCase();
			/*urlFolder = title;
			urlFolder = urlFolder.replaceAll(splChrs, "");
			urlFolder = urlFolder.replaceAll("[\\-\\+\\.\\^:,|\"\\?$><'?!:]", "");*/
			//contents text
			String contents = jsDocument.body().text();

			//indexing the web page
			org.apache.lucene.document.Document luceneDocument = indexing.indexWebPage(contents,title,url);
			indexing.indexWriter.addDocument(luceneDocument);
			indexing.indexWriter.commit();
			//create folder with the specific title inside the "Crawler" folder
			/*urlFolderName = Paths.get(crawlerPath.toString(), urlFolder).toString();
			boolean subDirCreated = new File(urlFolderName).mkdir();*/

//			//download images
//			Elements image = jsDocument.getElementsByTag("img");
//			for(Element element: image){
//				//for each element get the source url
//				String imageUrl = element.absUrl("src");
//				if(!imageUrl.isEmpty() && ((imageUrl.contains("gif") || (imageUrl.contains("jpg")) || (imageUrl.contains("png"))))){
//					startTime = System.currentTimeMillis();
//					while(System.currentTimeMillis()-startTime<totalExecutionTime)
//						getImages(imageUrl);
//					//Remove images not in .gif, .jpg , .png format
//					doImageCleanUp(urlFolderName);
//				}
//			}
		}catch(SSLHandshakeException ss){
			System.err.println(ss);
		}catch(MalformedURLException mu){
			System.out.println("MalFormed URL");
		}catch(IllegalArgumentException ie){
			System.out.println("Illegal Argument");
		}catch(ConnectException c){ 
			System.err.println("Connection Issue");
		}catch(SocketTimeoutException s){
			System.err.println("SocketConnection Issue");
		}catch(HttpStatusException http){
			System.err.println("Http Connection Issue");
		}catch(NullPointerException ne){
			System.err.println("Null Pointer Exception");
		}catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/*private void doImageCleanUp(String tempURLFolderName) throws IOException{
		String tempImageName,tempImgFormat = null;
		int imgIndex=0;
		File folder = new File(tempURLFolderName);
		try{
			//for all files inside the folder, check for format. If not found delete
			File[] files = new File(tempURLFolderName).listFiles();
			//If folder is empty delete the folder.
			for(File file:files){
				tempImageName = file.getName();
				imgIndex = tempImageName.lastIndexOf(".");
				if(imgIndex > 0){
					tempImgFormat = tempImageName.substring(imgIndex+1);
				}
				if(!((tempImgFormat.equalsIgnoreCase("gif")) || (tempImgFormat.equalsIgnoreCase("jpg")) || (tempImgFormat.equalsIgnoreCase("png"))) || (file.length()==0)){
					FileUtils.deleteQuietly(file);
				}
			}
		}catch(NullPointerException folderEmpty){
			System.err.println("Folder empty!");
			//			FileUtils.deleteDirectory(folder);
		}

	}*/

	/*public void getImages(String imageUrl){
		String folder = null;
		String name="";
		URL url;
		BufferedImage image = null;
		String imageFormat=null;
		String destinationFolder=null;
		//Extract the name of the image from the img src attribute
		try{
			int indexname = imageUrl.lastIndexOf("/");
			if(!imageUrl.startsWith("http")){
				if (indexname == imageUrl.length()) {
					imageUrl = imageUrl.substring(1, indexname);
				}

				name = imageUrl.substring(indexname, imageUrl.length());
				url = new URL(imageUrl);
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new BufferedOutputStream(new FileOutputStream(urlFolderName+ name));
				for (int b; (b = input.read()) != -1;) {
					output.write(b);
				}
				output.close();
				input.close();
			}else{
				//for webpages which store their images on another server
				name=imageUrl;
				//Open a URL Stream
				url=new URL(imageUrl);
				imageFormat = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
				imageUrl = imageUrl.replaceAll(splChrs,"");
				imageUrl = imageUrl.replaceAll("\\?", "");
				if(!(imageFormat.startsWith("com")||imageFormat.startsWith("jpg?"))){
					imageUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
					destinationFolder = Paths.get(urlFolderName+ imageUrl).toString();
					FileOutputStream src = new FileOutputStream(destinationFolder);

					OutputStream output = new BufferedOutputStream(src);
					image = ImageIO.read(url);
					if (image != null) {
						ImageIO.write(image, imageFormat, output);
					}
					File srcFile = new File(destinationFolder);
					File destDir = new File(urlFolderName);
					FileUtils.copyFileToDirectory(srcFile, destDir);
					output.close();
					FileUtils.deleteQuietly(srcFile);
				}else{

				}
			}
		}catch(IOException e){
			System.err.println("DNS Problem");
		}
	}*/
}

