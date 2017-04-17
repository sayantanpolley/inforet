package crawlAndSearch;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.lang.model.util.Elements;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;



public class FileSearch {

	//public Map<Integer,String> MapForGui = new HashMap<Integer,String>();	
	public Map<Integer,ResultsForGui> MapForGui = new HashMap<Integer,ResultsForGui>();


	private final static String fieldFileContents="text";
	private final static String fieldFilePath="url";
	private final static String titleField="title";
	private String line=null;
	private int totalDocsDisplay=10;
	private int totalnumDocs;
	public String urlFolder;
	public String urlFolderName;
	public String splChrs = "-/@#$%^&_+=():.| " ;
	Repository repository;
	//Path crawlerPath;
	int count_of_uni_URLs=0;


	public Map<Integer, ResultsForGui> getMapForGui() {
		return MapForGui;
	}


	public void setMapForGui(Map<Integer, ResultsForGui> mapForGui) {
		MapForGui = mapForGui;
	}


	public void searchInitializations(String searchQry,int displayRes,String contextPath)throws Exception {
		repository=new Repository();
		
		//crawlerPath = Paths.get(repository.crawlerFolder); // contextPath == /IRP3V01
		Analyzer analyzer = new StandardAnalyzer();
		String indexFolderName = repository.indexFolder;
		try{
			IndexReader indexReader =DirectoryReader.open(FSDirectory.open(Paths.get(contextPath,indexFolderName)));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//			IndexReader indexReader =DirectoryReader.open(FSDirectory.open(Paths.get(indexFolderName)));
//			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			//BufferedReader bufferedReader = null;
			//System.out.println("Please enter the input query");
			//bufferedReader = new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8));
			// send the user query for parsing and stemming
			line=stemQuery(analyzer,searchQry);
			QueryParser parser = new QueryParser(fieldFileContents, analyzer);
			Query query = parser.parse(line);
			System.out.println("System is searching for the string " +query.toString(fieldFileContents));
			SearchDoc(indexSearcher, query,fieldFilePath,titleField,displayRes,contextPath);
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}


	private void SearchDoc(IndexSearcher indexSearcher, Query query,String fieldFilePath,String titleField,int displayRes,String contextPath) throws Exception{
		try{
//			String ValueForMap="";
//			String Delimiter="~";
			String PathtoRepImage="";
//			String ScoretoMap="";	

			//int i=0;

			//To calculate relevance
			TopDocs results = indexSearcher.search(query, displayRes); //indexSearcher.search(query, totalDocsDisplay);
			//Document scoring
			ScoreDoc[] hits = results.scoreDocs;
			//No of hits
			totalnumDocs = results.totalHits;
			//System.out.println(totalnumDocs + " relevant documents ");
			int first = 0;
			int last = Math.min(totalnumDocs, displayRes); //int last = Math.min(totalnumDocs, totalDocsDisplay);
			setTotalnumDocs(totalnumDocs); // for use in controller
			
			
			Map<String, String> uniq_srch_results = new TreeMap<String, String>();
						
			while(first < last){
				
				Document doc = indexSearcher.doc(hits[first].doc);
				String path = doc.get(fieldFilePath); //path = URL
				//System.out.println("fieldFilePath ="+fieldFilePath);
				//title=title.replaceAll("\\s+","").replace(",", "").toLowerCase();


				if (!path.equals(null) && !path.equals("") && !path.equals(" "))// to confirm that path really has some value
				{
					int Rank=first+1;
					Float score=hits[first].score;
					org.jsoup.nodes.Document jsDocument = Jsoup.connect(path).get();
					String title =jsDocument.title();
					title=title.replaceAll("\\s+","").replace(",", "").toLowerCase();
					urlFolder=title;
					//System.out.println(urlFolder);
					urlFolder = urlFolder.replaceAll(splChrs, "");
					urlFolder = urlFolder.replaceAll("[\\-\\+\\.\\^:,|\"\\?$><'?!:%]", "");
					
					if(!path.isEmpty())
					{
						//i++;
						count_of_uni_URLs++;
						urlFolder=String.valueOf(count_of_uni_URLs);
						// contextPath = IRP3V01
						// /IRP3V01/WebContent/WEB-INF/tst
						//urlFolderName =  Paths.get(contextPath, "WebContent","WEB-INF","Pics",urlFolder).toString();
						urlFolderName =  Paths.get(contextPath,"pics","TempCrawl",urlFolder).toString();
						//urlFolderName = Paths.get(crawlerPath.toString(), urlFolder).toString();
						boolean subDirCreated = new File(urlFolderName).mkdir();
						/*org.jsoup.nodes.Document jsDocument = Jsoup.connect(path).get();
						download images*/
						org.jsoup.select.Elements image = jsDocument.getElementsByTag("img");
						for(Element element: image){
							//for each element get the source url
							String imageUrl = element.absUrl("src");
							if(!imageUrl.isEmpty() && ((imageUrl.contains("gif") || (imageUrl.contains("jpg")) || (imageUrl.contains("png"))))){
								//startTime = System.currentTimeMillis();
								//while(System.currentTimeMillis()-startTime<totalExecutionTime)
								//this methods created the directories and downloads the image for each unique URL
								getImages(imageUrl,urlFolderName); // getImages(imageUrl)
								//Remove images not in .gif, .jpg , .png format
								//doImageCleanUp(urlFolderName);
							}
						}

					}
					//System.out.println("Rank of the document is : "+(Rank));
					//System.out.println("Url of the document : "+path);
					//System.out.println("title of the document : "+title);
					RepImg repimg=new RepImg();
					//System.out.println("Proceeding to get image path");
					//System.out.println("title ="+title+" contextPath ="+contextPath);
					//System.out.println(" ");
					File RepImagePath=repimg.ReprensatativeImage(urlFolder,contextPath); //ReprensatativeImage(title,contextPath);
				//1. new file with a new name. 2. content same as 
					int rand= (int) Math.random();
					
					Random rnd = new Random();
					int min =1;
					int max =10000;
					int range = max -min+1;
					int randomKey = -1;
					randomKey = (rnd.nextInt(range)+min);
					File tempFileName = new File(urlFolderName,"mostRepresImgPerAlgoRndmNo"+String.valueOf(randomKey));

					// Rename file (or directory)
					boolean success = RepImagePath.renameTo(tempFileName);
					
									
					//path =URL, so this map wont allow any dupl URLs, even if returned wrongly!
					uniq_srch_results.put(path.trim(), Rank+"~"+String.valueOf(score)+"~"+tempFileName.toString()+"~"+title);

				//Delimiter="~";
				if(RepImagePath!=null)
				PathtoRepImage= RepImagePath.toString();
				
//					ResultsForGui res = new ResultsForGui();
//					res.setScore(score);
//					res.setPath(tempFileName.toString());//this is image path
//					res.setTitle(title);
//					res.setUrl(path);
//					this.MapForGui.put(Rank,res);
					
					 }
				else{
					System.out.println((first+1) + ". " + "There is no path for this document");
				}
				first++;
				

			}
			
			//Here we remove the duplicate URLs using the Set and the treemap
			//System.out.println("Printing search results ..");
			
			Map<Integer, String> ranked_Map = new TreeMap<Integer, String>();
		
		    for(Map.Entry<String, String> uniq_map :uniq_srch_results.entrySet() ){
		    	
		    	String [] all_vals = uniq_map.getValue().split("~");
		    	//System.out.println(" all_vals.length = "+all_vals.length);
		    	int tuple_rank = Integer.parseInt(all_vals[0]);
		    	
		    	ranked_Map.put(tuple_rank, uniq_map.getValue()+"~"+uniq_map.getKey().trim()); //==> this is whole tuple with concat, uniq url at the end
		    	// this ranked_Map will sort the values in natural order of key-which is int!, so lowest rank comes first
			}
		    System.out.println("Ranked - Treemap ="+ranked_Map);
		   

		    //finally set the sorted map in bean class for use in controller,
		    int r=0;
		    for(Map.Entry<Integer, String> km1: ranked_Map.entrySet()){
		    	r++;
		    	String [] all_vals = km1.getValue().split("~");
		    	
			    ResultsForGui res = new ResultsForGui();
				res.setScore(Float.parseFloat(all_vals[1]));
				res.setPath(all_vals[2]);//this is image path
				res.setTitle(all_vals[3]);
				res.setUrl(all_vals[4]);
				
				this.MapForGui.put(r,res);// km1.getKey() 
		    }

		    setMapForGui(this.MapForGui);
		}
		catch (IOException e){
			e.printStackTrace();
		}

		//System.out.println("Mapcontents :"+MapForGui);
		
	}

	public String stemQuery(Analyzer analyzer,String searchQry) throws IOException{
		String term="",stemmedQuery="";
		try{
			String line=null;
			//System.out.println("Please enter the search query: ");
			line= searchQry;

			boolean found=line.matches("[" + splChrs + "]+");
			if(line.contentEquals("")||(found==true))
			{
				System.out.println("User query must be in proper format Query must be appended with a text string.\n");
				System.out.println();
				System.exit(2);
			}

			TokenStream tokenStream = analyzer.tokenStream("tokensOfQuery", line);
			tokenStream=new PorterStemFilter(tokenStream);
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.reset();
			boolean queryIsAStopWord=true;
			while (tokenStream.incrementToken()) {
				term = charTermAttribute.toString();
				stemmedQuery+=" "+term;
				queryIsAStopWord=false;
			}
			tokenStream.close();
			if(queryIsAStopWord==true){
				System.out.println("The input query contains only stop words. Kindly re-run and enter a new search query");
				System.exit(2);
			}
		} catch (IOException unableToResetException) {
			unableToResetException.printStackTrace();
		} 
		return stemmedQuery;
	}  


	public void getImages(String imageUrl,String urlFolderName){
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
				//OutputStream output = new BufferedOutputStream(new FileOutputStream(urlFolderName+ name));
				OutputStream output = new BufferedOutputStream(new FileOutputStream(urlFolderName+ name)); // urlFolderName - this has context+pics+TempCrawl!!
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
				//imageUrl = imageUrl.replaceAll("\\?", "");
				imageUrl = imageUrl.replaceAll("\\?\\^\\+\\-\\%", ""); //("[\\-\\+\\.\\^:,|\"\\?$><'?!:%]", "");
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

	}


	public int getTotalDocsDisplay() {
		return totalDocsDisplay;
	}


	public void setTotalDocsDisplay(int totalDocsDisplay) {
		this.totalDocsDisplay = totalDocsDisplay;
	}


	public int getTotalnumDocs() {
		return totalnumDocs;
	}


	public void setTotalnumDocs(int totalnumDocs) {
		this.totalnumDocs = totalnumDocs;
	}



}