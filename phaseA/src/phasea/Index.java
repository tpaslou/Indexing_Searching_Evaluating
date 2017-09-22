/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phasea;

import gr.uoc.csd.hy463.NXMLFileReader;

import java.io.*;
import java.io.File;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Map.Entry;
import mitos.stemmer.Stemmer;
import org.apache.commons.collections4.map.MultiValueMap;

/**
 *
 * @author thano
 */
public class Index {

    /**
     * @param args the command line arguments
     */
    public static TreeMap<String, HashMap<String, Double>> DocTF = new TreeMap<>();
    public static HashMap<String, Long> DocFilePos = new LinkedHashMap<>();//position of word into document file
    public static MultiValueMap<String, ArrayList<String>> WordMap = new MultiValueMap();//map of words we use multivalue map for double entries
    public static int Number_Of_Files = 0;//total number of files
    public static TreeMap<String, Integer> DocFreq = new TreeMap<>();//words document frequency 
    public static TreeMap<String, Double> IDF = new TreeMap<>();//Inverted doc freq
    public static TreeMap<String, Double> TF = new TreeMap<>();//term frequency 
    public static TreeMap<String, Double> WordsTF_IDF = new TreeMap<>();//from the ones above we calculate TF IDF
    public static TreeMap<String, String> UrlMap = new TreeMap<String, String>();//Urls
    public static String delimit = "[], ";//Global delimiter for iterating 

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        System.getProperty("file.encoding", "UTF-8");
        File folder = new File("C:\\MedicalCollection");
        File stopWordsEn = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\src\\phasea\\stopwordsEn.txt");
        FileInputStream fileInputStreamEn = new FileInputStream(stopWordsEn);
        BufferedReader inEn = new BufferedReader(new InputStreamReader(fileInputStreamEn));

        File stopWordsGr = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\src\\phasea\\stopwordsGr.txt");
        FileInputStream fileInputStreamGr = new FileInputStream(stopWordsGr);
        BufferedReader inGr = new BufferedReader(new InputStreamReader(fileInputStreamGr));

        HashSet<String> stopWords = new HashSet<>();
        String s;
        while ((s = inEn.readLine()) != null) {
            stopWords.add(s);
        }

        while ((s = inGr.readLine()) != null) {
            stopWords.add(s);
        }

        listFilesForFolder(folder, stopWords);
        
        FrequenciesInit();
        DocumentInit();
        RandomAccessFile PostFile=null;
        PostFile = new RandomAccessFile("PostingFile.txt","rw");
        VocabularyInit(PostFile);

        

        
       /* RandomAccessFile file=null;
        try{
            file = new RandomAccessFile("rand.txt","rw");
            String a="1jkdlkasj\r\n";
           
            file.writeUTF(a);
            
            file.writeUTF("2a;lskjdlkas\r\n");
            file.writeUTF("3kjdlkas\r\n");
            
            file.seek(0);
            System.out.print(file.readUTF());
            String sad=file.readUTF();
            System.out.println(sad.charAt(1));
            //file.seek(file.getFilePointer());
            //long pr=file.getFilePointer();
            System.out.print(file.readUTF());
            
            //file.seek(file.getFilePointer());
            //System.out.print(file.readUTF());
            
            //file.seek(pr);
            System.out.println(file.readUTF());
            
           
            
        }catch(Exception e){}
        */

    }

    public static void listFilesForFolder(File folder, HashSet<String> stopWords)
            throws IOException {

        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, stopWords);

            } else {
                IndexInit(stopWords, fileEntry.getAbsolutePath());
                Number_Of_Files++;

            }
        }

    }

    //clears stopwords
    public static HashMap< String, ArrayList<String>> clearSW(String Id, String Tag, String Words, HashSet<String> stopWords) {

        HashMap< String, ArrayList<String>> TWordMap = new HashMap<>();
        String line = Words;
        String delimiter = " \t.,:;!?<>{}()[]/\"'";
        StringTokenizer tokenizer = new StringTokenizer(line, delimiter);
        ArrayList<String> inf;

        //initialize words into map
        while (tokenizer.hasMoreTokens()) {
            String currentToken = tokenizer.nextToken();
            Stemmer.Initialize();//initialize stemming too
            currentToken = Stemmer.Stem(currentToken);
            if (!TWordMap.containsKey(currentToken)) {
                inf = new ArrayList<String>(3);
                inf.add("1");//initial number of appearances
                inf.add(Tag);
                inf.add(Id);
                TWordMap.put(currentToken, inf);
            } else {
                //if it exists we change the number of appearances (1st value of hashmap)
                TWordMap.get(currentToken).set(0, (Integer.parseInt(TWordMap.get(currentToken).get(0)) + 1) + "");
                //String conversion again (+ "")
            }
        }
        //remove stopwords

        Iterator<Entry<String, ArrayList<String>>> it = TWordMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, ArrayList<String>> pair = it.next();
            if (stopWords.contains(pair.getKey())) {
                String key = pair.getKey();
                it.remove();

            }
        }

        return TWordMap;

    }

    public static void IndexInit(HashSet<String> stopWords, String Path) throws IOException {
        File collection = new File(Path);
        NXMLFileReader xmlFile = new NXMLFileReader(collection);
        String pmcid = xmlFile.getPMCID();
        String title = xmlFile.getTitle();
        String abstr = xmlFile.getAbstr();
        String body = xmlFile.getBody();
        String journal = xmlFile.getJournal();
        String publisher = xmlFile.getPublisher();
        ArrayList<String> authors = xmlFile.getAuthors();
        HashSet<String> categories = xmlFile.getCategories();

        WordMap.putAll(clearSW(pmcid, "pmcid", pmcid, stopWords));
        WordMap.putAll(clearSW(pmcid, "title", title, stopWords));
        WordMap.putAll(clearSW(pmcid, "abstr", abstr, stopWords));
        WordMap.putAll(clearSW(pmcid, "body", body, stopWords));
        WordMap.putAll(clearSW(pmcid, "journal", journal, stopWords));
        WordMap.putAll(clearSW(pmcid, "publisher", publisher, stopWords));
        WordMap.putAll(clearSW(pmcid, "authors", authors.toString(), stopWords));
        WordMap.putAll(clearSW(pmcid, "categoties", categories.toString(), stopWords));
        MultiValueMap<String, ArrayList<String>> DocMap = new MultiValueMap();
        DocMap.putAll(clearSW(pmcid, "pmcid", pmcid, stopWords));
        DocMap.putAll(clearSW(pmcid, "title", title, stopWords));
        DocMap.putAll(clearSW(pmcid, "abstr", abstr, stopWords));
        DocMap.putAll(clearSW(pmcid, "body", body, stopWords));
        DocMap.putAll(clearSW(pmcid, "journal", journal, stopWords));
        DocMap.putAll(clearSW(pmcid, "publisher", publisher, stopWords));
        DocMap.putAll(clearSW(pmcid, "authors", authors.toString(), stopWords));
        DocMap.putAll(clearSW(pmcid, "categoties", categories.toString(), stopWords));
        DocumentTF(pmcid, DocMap);

        UrlMap.put(pmcid, Path);


        
    }

    public static void DocumentTF(String pmcid, MultiValueMap<String, ArrayList<String>> Words) {
        double max_freq = 0;
        HashMap<String, Double> RetHash = new HashMap<>();
        for (Entry<String, Object> entry : Words.entrySet()) {
            String key = entry.getKey();
            String values = entry.getValue().toString();
            int val, test;
            val = test = 0;
            StringTokenizer tokenizer = new StringTokenizer(values, delimit);
            while (tokenizer.hasMoreTokens()) {
                String currentToken = tokenizer.nextToken();
                if (val % 3 == 0) {
                    test = test + Integer.parseInt(currentToken);
                }
                val++;
            }
            if (test > max_freq) {
                max_freq = test;
            }
            RetHash.put(key, (double) test);

        }
        for (Entry<String, Double> entry : RetHash.entrySet()) {
            Double value = entry.getValue();
            entry.setValue(value / max_freq);
        }
        DocTF.put(pmcid, RetHash);

    }
    public static void FrequenciesInit(){
        TreeMap<String, ArrayList<String>> documents_map = new TreeMap<>((MultiValueMap) WordMap);
    
        for (Map.Entry<String, ArrayList<String>> entry : documents_map.entrySet()) {
            //calculates Document Frequency 
            String key = entry.getKey();
            ArrayList<String> values = (ArrayList<String>) entry.getValue();
            String t_Values = values.toString();

            HashSet<String> Temp_HashSet = new HashSet<>();
            int i = 1;
            StringTokenizer tokenizer = new StringTokenizer(t_Values, delimit);
            while (tokenizer.hasMoreTokens()) {
                String currentToken = tokenizer.nextToken();
                if (i % 3 == 0) {
                    Temp_HashSet.add(currentToken);
                }
                i++;

            }
            DocFreq.put(key, Temp_HashSet.size());
            IDF.put(key, Math.log(Number_Of_Files / DocFreq.get(key)) / Math.log(2));
        }
    }
        

    public static void VocabularyInit(RandomAccessFile PostFile) throws IOException {
        TreeMap<String, ArrayList<String>> documents_map = new TreeMap<>((MultiValueMap) WordMap);

        File file = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\Vocabulary.txt");
        file.getParentFile().mkdirs();
        BufferedWriter ToWrite = new BufferedWriter(new FileWriter(file, false));
        ToWrite.write(Integer.toString(Number_Of_Files));
        ToWrite.newLine();
        long WordPosition = 0;//we count in bytes position into voc file
        for (Map.Entry<String, ArrayList<String>> entry : documents_map.entrySet()) {
            //calculates Document Frequency 
            String key = entry.getKey();
            ArrayList<String> values = (ArrayList<String>) entry.getValue();
            String t_Values = values.toString();

            HashSet<String> Temp_HashSet = new HashSet<>();
            int i = 1;
           
            long post_pos=PostingFile(key,PostFile);
            ToWrite.append(key + "\t" + DocFreq.get(key)+"  "+post_pos);
            ToWrite.newLine();
          
            

        }
        ToWrite.close();
    }

    public static void DocumentInit() throws IOException {
        
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\Document.txt", true)));
        long position = 0;

        //calculates  and stores TF for every word
        //also calculates doc weight
        for (Map.Entry<String, HashMap<String, Double>> entry : DocTF.entrySet()) {
            double weight = 0;
            String pmcid = entry.getKey();
            HashMap<String, Double> WordsTF = entry.getValue();
            for (Entry<String, Double> entry2 : WordsTF.entrySet()) {
                WordsTF_IDF.put(entry2.getKey(), entry2.getValue() * IDF.get(entry2.getKey()));
                weight = weight + Math.pow(WordsTF_IDF.get(entry2.getKey()), 2);
            }
            //System.out.println(entry.getKey()+" "+entry.getValue());
            double norm = Math.sqrt(weight);
            DocFilePos.put(pmcid, position);
            position += pmcid.length() + UrlMap.get(pmcid).length() + String.valueOf(norm).length() + 2;
            out.println(pmcid + "\t" + UrlMap.get(pmcid) + "\t" + String.valueOf(norm));
        }
        out.close();

    }

    public static long PostingFile(String key,RandomAccessFile PostFile) throws IOException {
        long pos=0;
        int where=0;
        
        
        for(Entry<String, HashMap<String, Double>> entry : DocTF.entrySet()){
            String doc_num=entry.getKey();
            HashMap<String, Double> WordsTF = entry.getValue();
            if(WordsTF.containsKey(key)){
                if(where==0){
                    pos=PostFile.getFilePointer();
                    where++;
                }
                PostFile.writeBytes(doc_num+" "+WordsTF.get(key)+"    "+DocFilePos.get(doc_num)+"\r\n" );
               
                
            }
            
        }
       
        PostFile.writeUTF("\r\n");
        return pos;

    }
    
}
