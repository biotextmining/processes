package com.silicolife.textmining.processes.corpora.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.corpora.loaders.ICorpusEntityLoader;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;

/**
 * 
 * Available in https://sourceforge.net/projects/medtag/files/medtag.tar.gz
 * 
 *
 */

public class ABGeneByMedTagCorporaLoader implements ICorpusEntityLoader{
	
	private List<IPublication> documents;
	private boolean stop = false;
	private String medtaglink = "http://www.ncbi.nlm.nih.gov/pubmed/?term=";
	private String pubmedExternalLink = "PUBMED";
	private String geneClass = "Gene";
	private Map<Long,IAnnotatedDocument> docwithentities;
	
	public ABGeneByMedTagCorporaLoader(){
		this.documents = new ArrayList<>();
		this.docwithentities = new TreeMap<>();
	}
	
	@Override
	public List<IPublication> processFile(File fileOrDirectory,
			Properties properties) throws ANoteException, IOException {
		if(verifyMedTagFile(fileOrDirectory)){
			if(fileOrDirectory.getName().endsWith(".gz")){
				GZIPInputStream gzfile = new GZIPInputStream(new FileInputStream(fileOrDirectory));
				Path medtag = Files.createTempDirectory("MedTagDirTemp");
				File dir = medtag.toFile();
				File txtextracted = null;
				if(fileOrDirectory.getName().endsWith(".tar.gz")){
					extract(new TarInputStream(gzfile), dir);
					txtextracted  = new File(dir.getPath()+"/medtag/data/medtag.txt");
				}
				if(txtextracted!=null && txtextracted.exists()){
					processFile(new InputStreamReader(new FileInputStream(txtextracted)));
				} else{
					processFile(new InputStreamReader(gzfile));
				}
				FileUtils.deleteDirectory(dir);
				Files.deleteIfExists(medtag);
			}else if(fileOrDirectory.getName().endsWith(".txt")){
				processFile(new InputStreamReader(new FileInputStream(fileOrDirectory)));
			}
			return 	getDocuments();
		}
		else{
			return null;
		}
	}

	@Override
	public boolean validateFile(File filepath) {
		if(filepath.isDirectory())
			return false;
		if(isGZipped(filepath))
			return verifyMedTagFile(filepath);	
		return false;
	}
	
	@Override
	public Map<Long,IAnnotatedDocument> getDocumentEntityAnnotations() {
		return docwithentities;
	}
	
	private void extract(TarInputStream inputStream,File dir) throws IOException {
		TarEntry entry = null;
		FileOutputStream outputStream=null;
		if (!dir.exists()) {
			dir.mkdirs();
		}
		boolean createdDir = false;
		while ((entry=inputStream.getNextEntry())!= null) {

			if (entry.isDirectory()) {
				File fileOrDir=new File(dir,entry.getName());
				fileOrDir.mkdir();
				createdDir = true;
			}else{
				if(!createdDir){
					break;
				}else{
					byte[] content=new byte[(int) entry.getSize()];
					int offset = 0;
					outputStream=new FileOutputStream(new File(dir,entry.getName()));
					inputStream.read(content,offset,content.length-offset);
					IOUtils.write(content, outputStream);
					if (outputStream  != null) {
						outputStream.close();
					}
				}
			}
		}
	}

	private void processFile(InputStreamReader file) throws IOException, ANoteException{
		Map<String, String> mapExcerptIDAndPubmedID = new HashMap<String, String>();
		Map<String, String> mapExcerptIDAndText = new HashMap<String, String>();
		Map<String, List<Long[]>> mapExcerptIDAndAnnotationOffsets = new HashMap<String, List<Long[]>>();
		BufferedReader reader = new BufferedReader(file);
		String line = new String();
		List<String> tempCodeBlock = new ArrayList<String>();
		while ((line = reader.readLine()) != null && !stop){
			if(line.isEmpty()){
				processCodeBlock(tempCodeBlock, mapExcerptIDAndPubmedID, mapExcerptIDAndText, mapExcerptIDAndAnnotationOffsets);
				tempCodeBlock = new ArrayList<String>();
				continue;
			}
			tempCodeBlock.add(line);
		}
		reader.close();
		processDocumentsAndAnnotations(mapExcerptIDAndPubmedID, mapExcerptIDAndText, mapExcerptIDAndAnnotationOffsets);
	}
	
	private void processCodeBlock(List<String> tempCodeBlock, Map<String, String> mapExcerptIDAndPubmedID, Map<String, String> mapExcerptIDAndText, Map<String, List<Long[]>> mapExcerptIDAndAnnotationOffsets){
		if(tempCodeBlock.isEmpty()){
			return;
		}
		if(tempCodeBlock.get(0).equals(">>EXCERPT") && tempCodeBlock.get(1).equals("CORPUS_CD: abgene")){
			String exerptID = tempCodeBlock.get(2);
			for(int i=3; i<tempCodeBlock.size(); i++){
				String line = tempCodeBlock.get(i);
				String[] lineparts = line.split(" ");
				if(lineparts[0].equals("SOURCE:")){
					if(lineparts[2].startsWith("P")) lineparts[2] = lineparts[2].substring(1);
					mapExcerptIDAndPubmedID.put(exerptID, lineparts[2]);
				}
				if(lineparts[0].equals("TEXT:")){
					mapExcerptIDAndText.put(exerptID, line.substring(6).trim());
				}
			}
		}
		
		if(tempCodeBlock.get(0).equals(">>ANNOTATION") && tempCodeBlock.get(1).equals("ANNOTATION: GENE") && tempCodeBlock.get(3).equals("CORPUS_CD: abgene")){
			String exerptID = tempCodeBlock.get(4);
			if(!mapExcerptIDAndAnnotationOffsets.containsKey(exerptID)){
				mapExcerptIDAndAnnotationOffsets.put(exerptID, new ArrayList<Long[]>());
			}
			List<Long[]> offList = mapExcerptIDAndAnnotationOffsets.get(exerptID);
			Long[] OffArray = new Long[2];
			String[] startoffString = tempCodeBlock.get(5).split(" ");
			String[] endtoffString = tempCodeBlock.get(6).split(" ");
			OffArray[0] = Long.valueOf(startoffString[1]);
			OffArray[1] = Long.valueOf(endtoffString[1])+(long)1;
			offList.add(OffArray);
			mapExcerptIDAndAnnotationOffsets.put(exerptID, offList);
		}
	}
	
	private void processDocumentsAndAnnotations(Map<String, String> mapExcerptIDAndPubmedID, Map<String, String> mapExcerptIDAndText, Map<String, List<Long[]>> mapExcerptIDAndAnnotationOffsets) throws ANoteException {
		for(String exceprtID : mapExcerptIDAndPubmedID.keySet()){
			if( mapExcerptIDAndText.containsKey(exceprtID)){
				IPublicationExternalSourceLink externalID = new PublicationExternalSourceLinkImpl(mapExcerptIDAndPubmedID.get(exceprtID), pubmedExternalLink);
				List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<>();
				publicationExternalIDSource.add(externalID);
				IPublication pub = new PublicationImpl("", "MedTag Team", "", "", "", "", "", "", "", "", mapExcerptIDAndText.get(exceprtID), medtaglink+mapExcerptIDAndPubmedID.get(exceprtID), false, "", "", publicationExternalIDSource, new ArrayList<IPublicationField>(), new ArrayList<IPublicationLabel>());
				getDocuments().add(pub);
				if(mapExcerptIDAndAnnotationOffsets.containsKey(exceprtID)){
					getDocumentEntityAnnotations().put(pub.getId(),new AnnotatedDocumentImpl(pub,null, null, getEntities(mapExcerptIDAndAnnotationOffsets.get(exceprtID), mapExcerptIDAndText.get(exceprtID))));
				}
			}
		}

	}
	
	
	private List<IEntityAnnotation> getEntities(List<Long[]> entitiesOffsets, String text) throws ANoteException {
		List<IEntityAnnotation> entities = new ArrayList<IEntityAnnotation>();
		IAnoteClass toadd = new AnoteClass(geneClass);
		IAnoteClass klass = ClassPropertiesManagement.getClassIDOrinsertIfNotExist(toadd);
		NavigableMap<Integer, Integer> whitespaceMap = getOffsetAddSpaces(text);
		for(Long[] offsets : entitiesOffsets){
			long start = offsets[0];
			long end = offsets[1];
			start =  start + (long) whitespaceMap.floorEntry((int)start).getValue();
			end =  end + (long) whitespaceMap.floorEntry((int)end).getValue();
			String entityString = text.substring((int)start, (int)end);
			String trimedEntity = entityString.trim();
			int size = entityString.length() - trimedEntity.length();
			if(size>0){
				if(trimedEntity.charAt(0)!=entityString.charAt(0)){
					start = start + (long)1;
					end = start + (long)trimedEntity.length();
				}
			}
			IEntityAnnotation entity = new EntityAnnotationImpl(start, end,klass,null, trimedEntity, false, null);
			entities.add(entity);
		}
		return entities;
	}
	
	private NavigableMap<Integer, Integer> getOffsetAddSpaces(String text){
		NavigableMap<Integer, Integer> whitespaceMap = new TreeMap<Integer, Integer>();
		Pattern p = Pattern.compile("\\s");
		Matcher matcher = p.matcher(text);
		int countWhiteSpace = 0;
		whitespaceMap.put(0, countWhiteSpace);
		countWhiteSpace = 1;
		while (matcher.find()) {
			whitespaceMap.put(matcher.end()-countWhiteSpace+1, countWhiteSpace++);
		}
		return whitespaceMap;
	}
	
	private boolean verifyMedTagFile(File filepath){
		if(filepath.getName().endsWith(".gz")){
			try {
				GZIPInputStream gzfile = new GZIPInputStream(new FileInputStream(filepath));
				BufferedReader reader = new BufferedReader(new InputStreamReader(gzfile));
				String line;
				int i = 0;
				while (((line = reader.readLine()) != null) & i<2) {
					if(line.length()>=7 && i==0 & line.substring(0, 7).equals("medtag/")){
						reader.close();
						return true;
					}
					if(line.equals(">>CORPUS")){
						reader.close();
						return true;
					}
					i++;
				}
				reader.close();
			} catch (IOException e) {
				return false;
			}
		}else if(filepath.getName().endsWith(".txt")){
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
				String line;
				int i = 0;
				while (((line = reader.readLine()) != null) & i<2) {
					if(line.length()>=7 && i==0 & line.substring(0, 7).equals("medtag/")){
						reader.close();
						return true;
					}
					if(line.equals(">>CORPUS")){
						reader.close();
						return true;
					}
					i++;
				}
				reader.close();
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}
	
	/**
	  * Checks if a file is gzipped.
	  * 
	  * @param f file
	  * @return
	  */
	private boolean isGZipped(File f) {
		int magic = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
			raf.close();
		}catch (Throwable e) {
			e.printStackTrace(System.err);
			}
		return magic == GZIPInputStream.GZIP_MAGIC;
	}

	public List<IPublication> getDocuments() {
		return documents;
	}

	public void stop() {
		this.stop  = true;		
	}


}
