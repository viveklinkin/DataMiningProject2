
import java.io.*;
import java.util.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vivek
 */
public class kcluster {

    final static String SSE_CRITERION = "SSE";
    final static String I2_CRITERION = "I2";
    final static String E1_CRITERION = "E1";

    static final LinkedHashMap<Integer, Map<Integer, Double>> ijv = new LinkedHashMap<>();
    static final TreeSet<Point> centroids = new TreeSet<>();

    static String inputFile = "/home/vivek/NetBeansProjects/JavaApplication5/output/ijvFile1";
    static String outputFile = "";
    static String classFile = "";
    static int numtrials = 20;
    static int numclusters = 10;

    static int outp = 0;
    static String clusterteringCriterion = "SSE";

    public static void main(String args[]) {
        getijv();
        if (clusterteringCriterion.equals(SSE_CRITERION)) {
            new SSE(numclusters, numtrials, ijv).cluster();
        } else if (clusterteringCriterion.equals(I2_CRITERION)) {

        } else if (clusterteringCriterion.equals(E1_CRITERION)) {

        } else {
            throw new UnsupportedOperationException("This Criterion is not supported");
        }
    }

    //format: 0:input-file 1:criterion-function 2:class-file 3:#clusters 4:#trials 5:output-file 
    static void getVals(String[] args) {
        inputFile = args[0];
        clusterteringCriterion = args[1];

    }

    static void getijv() {
        for (String x : readFile(new File(inputFile))) {
            if (!x.isEmpty() && x.contains(",")) {
                String[] y = x.split(",");
                int i = Integer.parseInt(y[0]);
                int j = Integer.parseInt(y[1]);
                double v = Double.parseDouble(y[2]);

                if (!ijv.containsKey(i)) {
                    ijv.put(i, new HashMap<Integer, Double>());
                }
                ijv.get(i).put(j, v);
            }
        }
    }

    static void writeFile(String filePath, List<String> data) {
        File f = new File(filePath);
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(f));
            for (String x : data) {
                bw.write(x + "\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("Error creating file");
        }

    }

    public static List<String> readFile(File f) {
        List<String> res = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    res.add(line);
                }
            }

            br.close();

        } catch (IOException e) {
            System.err.println("Error reading file");
        }
        return res;
    }
}
