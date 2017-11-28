
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

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

    static final String SSE_CRITERION = "SSE";
    static final String I2_CRITERION = "I2";
    static final String E1_CRITERION = "E1";

    static final LinkedHashMap<Integer, Map<Integer, Double>> ijv = new LinkedHashMap<>();
    static final TreeSet<Point> centroids = new TreeSet<>();
    static final Map<Integer, String> classFileVals = new HashMap<>();
    static final Map<String, Integer> labels = new HashMap<>();

    static String inputFile = "/home/vivek/NetBeansProjects/JavaApplication5/output/ijvFile1";
    static String classFile = "/home/vivek/NetBeansProjects/JavaApplication5/output/reuters.class";
    static String outputFile = "";
    static int numtrials = 20;
    static int numclusters = 20;
    static String clusterteringCriterion = "SSE";

    public static void main(String args[]) {
        getijv();
        getClassFileContents();
        Map<Integer, Integer> res = null;
        if (clusterteringCriterion.equals(SSE_CRITERION)) {
            res = new SSE(numclusters, numtrials, ijv).cluster();

        } else if (clusterteringCriterion.equals(I2_CRITERION)) {
            res = new I2(numclusters, numtrials, ijv).cluster();
        } else if (clusterteringCriterion.equals(E1_CRITERION)) {
            res = new E1(numclusters, numtrials, ijv).cluster();
        } else {
            throw new UnsupportedOperationException("This Criterion is not supported, try SSE, I2 or E1");
        }
        int[][] matrix = new int[numclusters][labels.size()];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = 0;
            }
        }
        for (Entry<Integer, Integer> currentEntry : res.entrySet()) {
            int articleNo = currentEntry.getKey();
            int clusterNo = currentEntry.getValue();
            int labelNo = labels.get(classFileVals.get(articleNo));
            matrix[clusterNo][labelNo]++;
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + "  ");
            }
            System.out.println("");
        }
        System.out.println("Purity: " + getTotalPurity(matrix));
        System.out.println("Entropy: " + getTotalEntropy(matrix));
    }

    static double getEntropy(int[] a) {
        double x = 0;
        double sum = getSum(a);
        for (int i : a) {
            double pij = ((double) i) / sum;
            if (pij != 0) {
                x += Math.abs(pij * (Math.log(pij) / Math.log(2)));
            }
        }
        return x;
    }

    static double getSum(int[] a) {
        double res = 0;
        for (int i : a) {
            res += i;
        }
        return res;
    }

    static double getTotalEntropy(int[][] a) {
        double m = 0, res = 0;
        for (int[] i : a) {
            double mj = getSum(i);
            double ej = getEntropy(i);
            res += mj * ej;
            m += mj;
        }
        return res / m;
    }

    static double getTotalPurity(int[][] a) {
        double res = 0, m = 0;
        for (int[] i : a) {
            double mj = getSum(i);
            double pj = getPurity(i);
            res += mj * pj;
            m += mj;
        }
        return res / m;
    }

    static double getPurity(int[] a) {
        double max = -1, sum = getSum(a);
        for (int i : a) {
            if (((double) i) / sum > max) {
                max = ((double) i) / sum;
            }
        }
        return max;
    }

    static void getClassFileContents() {
        int counter = 0;
        for (String x : readFile(new File(classFile))) {
            if (!x.isEmpty() && x.contains(",")) {
                String y[] = x.split(",");
                classFileVals.put(Integer.parseInt(y[0]), y[1]);
                if (!labels.containsKey(y[1])) {
                    labels.put(y[1], counter++);
                }
            }
        }
    }

    //format: 0:input-file 1:criterion-function 2:class-file 3:#clusters 4:#trials 5:output-file 
    static void getVals(String[] args) {
        inputFile = args[0];
        clusterteringCriterion = args[1];
        classFile = args[2];
        numclusters = Integer.parseInt(args[3]);
        numtrials = Integer.parseInt(args[4]);
        outputFile = args[5];
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
