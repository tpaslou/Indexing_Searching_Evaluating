/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluating;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.collections4.map.MultiValueMap;

/**
 *
 * @author Charis
 */
public class Evaluation {

    //periexei kathe fora gia kathe topic to pmcid tou sunafes eggrafou apo to results.txt
    private static ArrayList<String> resultsDoc = new ArrayList<>();
    //periexei kathe fora gia kathe topic to pmcid tou sunafes eggrafou apo to qrel.txt
    private static ArrayList<String> qrel = new ArrayList<>();
    ////periexei kathe fora gia kathe topic to pmcid tou sunafes eggrafou apo to results.txt
    //alla sortarismena me vasi to qrels.txt
    private static ArrayList<String> resultsDocSorted = new ArrayList<>();
    private static int topicNumber;
    private static double DCG;
    private static double IDCG;
    private static double NDCG;
    private static HashSet<String> ResultsHashSet = new HashSet<>();
    private static MultiValueMap<String, String> Topics = new MultiValueMap();
    private static MultiValueMap<String, String> Avep = new MultiValueMap<>();

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //kathe fora gia diaforetiko topic
        for (topicNumber = 1; topicNumber < 31; topicNumber++) {
            readResults();
            readQrel();
            calculateNDCG();
            resultsDoc.clear();
            qrel.clear();
            resultsDocSorted.clear();
        }
        L_ReadResults();
        L_ReadQrls();
        L_CalcAveP();
    }

    public static void readResults() throws UnsupportedEncodingException, FileNotFoundException, IOException {

        File res = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\src\\evaluating\\results.txt");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(res), "UTF8"));

        String s;

        while ((s = in.readLine()) != null) {
            String tokens[] = s.split("\\s+");
            //gia to topic topicNumber
            if (topicNumber == Integer.parseInt(tokens[0])) {
                //tokens[2] einai to pmcid eggrafou
                resultsDoc.add(tokens[2]);
            }
        }

        in.close();

        System.out.println("resultsDoc gia topic " + topicNumber + " -> " + resultsDoc.toString());
    }

    public static void readQrel() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        File qr = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\src\\evaluating\\qrels.txt");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(qr), "UTF8"));

        String s;

        while ((s = in.readLine()) != null) {
            String tokens[] = s.split("\\s+");
            //tokens[0] o arithmos tou iatrikou thematos
            if (topicNumber == Integer.parseInt(tokens[0])) {
                //tokens[3] relevance score 0,1,2
                //prwta ta poly sxetika
                if (Integer.parseInt(tokens[3]) == 2) {
                    qrel.add(tokens[2]);
                }
            }
        }
        in.close();

        BufferedReader in2 = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(qr), "UTF8"));

        while ((s = in2.readLine()) != null) {
            String tokens2[] = s.split("\\s+");

            if (topicNumber == Integer.parseInt(tokens2[0])) {
                //meta ta apla sxetika
                if (Integer.parseInt(tokens2[3]) == 1) {

                    qrel.add(tokens2[2]);
                }
            }
        }
        in2.close();
        System.out.println("qrel gia topic " + topicNumber + " " + qrel.toString());
    }

    public static void calculateNDCG() {
        //gia to DCG prwta
        DCG = 0;
        IDCG = 0;
        NDCG = 0;
        int rel = 0;
        if (qrel.contains(resultsDoc.get(0))) {
            rel = 1;
        } else {
            rel = 0;
        }

        for (int i = 1; i < resultsDoc.size(); i++) {

            //mono an uparxoun sto qrel
            if (qrel.contains(resultsDoc.get(i))) {

                DCG = DCG + 1 / (Math.log(i) / Math.log(2));
            }
        }
        DCG = DCG + rel;
        System.out.println("DCG!!!!!!" + DCG);

        //gia IDCG
        for (int i = 0; i < qrel.size(); i++) {
            if (resultsDoc.contains(qrel.get(i))) {

                resultsDocSorted.add(qrel.get(i));
                String s = qrel.get(i);
                System.out.println("resultsDoc prin to remove! " + resultsDoc.toString());
                //to diagrafw gia na min to ksana perasw
                resultsDoc.remove(s);
                System.out.println("resultsDoc meta to remove! " + resultsDoc.toString());
            }
        }

        //vazw ta upoloipa documents
        for (int i = 0; i < resultsDoc.size(); i++) {
            resultsDocSorted.add(resultsDoc.get(i));
        }

        if (qrel.contains(resultsDocSorted.get(0))) {
            rel = 1;
        } else {
            rel = 0;
        }

        for (int i = 1; i < resultsDocSorted.size(); i++) {

            //mono an uparxoun sto qrel
            if (qrel.contains(resultsDocSorted.get(i))) {
                DCG = DCG + 1 / (Math.log(i) / Math.log(2));
            }
        }
        IDCG = IDCG + rel;
        System.out.println("IDCG!!!!!" + IDCG);
        NDCG = DCG / IDCG;

        System.out.println("NDCG!!!!!!!!!!!! gia topic " + topicNumber + " -> " + NDCG);
    }

    private static void L_ReadResults() throws FileNotFoundException, IOException {
        File Delimit = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\src\\evaluating\\results.txt");
        FileInputStream Results = new FileInputStream(Delimit);
        BufferedReader results = new BufferedReader(new InputStreamReader(Results));

        String line = null;
        while ((line = results.readLine()) != null) {
            String t_array[] = line.split("\t");
            ResultsHashSet.add(t_array[2]);//pmcid is here
            //we create a set that contains all of them
        }
    }

    private static void L_ReadQrls() throws FileNotFoundException, IOException {
        File Delimit = new File("C:\\Users\\thano\\Desktop\\hy463_project\\phaseA\\src\\evaluating\\qrels.txt");
        FileInputStream Qrls = new FileInputStream(Delimit);
        BufferedReader qrls = new BufferedReader(new InputStreamReader(Qrls));

        String line = null;
        while ((line = qrls.readLine()) != null) {

            String t_array[] = line.split("\t");
            String document = t_array[2];
            if (ResultsHashSet.contains(document)) {
                Topics.put(t_array[0], t_array[3]);
                //we create a multivalue map with key number of topic and value relevance
            }

        }
    }

    private static void L_CalcAveP() {
        TreeMap<String, ArrayList<String>> tmp = new TreeMap<>((MultiValueMap) Topics);

        for (Entry<String, ArrayList<String>> curr_topic : tmp.entrySet()) {
            int cnt = 0;
            double sum = 0;
            double Rprec = 0;
            double Retrieved = curr_topic.getValue().size();

            for (int key = 0; key < Retrieved; key++) {
                int r = Integer.parseInt(curr_topic.getValue().get(key));
                if (r != 0) {
                    cnt++;
                }
                Rprec = cnt / Retrieved;
                sum += Rprec * r;
            }
            double avep = sum / Retrieved;
            Avep.put(curr_topic.getKey().toString(), (String.valueOf(avep) + " "));

        }
        //printf Aveps
        System.out.println("\nThese are the AvepS for each topic  : \n");
        String outputString = null;
        for (Entry<String, Object> entry : Avep.entrySet()) {
            ArrayList<String> evals = (ArrayList<String>) entry.getValue();
            outputString = entry.getKey() + "";
            for (int i = 0; i < evals.size(); i++) {
                outputString += "\t" + evals.get(i);
            }
            System.out.println(outputString);
        }
    }

}
