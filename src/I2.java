
import java.util.*;
import java.util.Map.Entry;

public class I2 {

    static final int[] seeds = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39};
    int nclusters;
    int trials;
    int n;
    List<Point> docs;
    Set<Point> clusters;

    public I2(int numclus, int numtrials, Map<Integer, Map<Integer, Double>> ijv) {
        clusters = new HashSet<>();
        docs = new ArrayList<>();
        this.nclusters = numclus;
        this.trials = numtrials;
        this.n = ijv.size();
        for (Entry<Integer, Map<Integer, Double>> currentEntry : ijv.entrySet()) {
            Point p = new Point(currentEntry.getKey(), kcluster.I2_CRITERION, false);
            p.setjv(currentEntry.getValue());
            docs.add(p);
        }
    }

    public Map<Integer, Integer> cluster() {
        Map<Integer, Integer> output = new HashMap<>();
        System.out.println("N=" + n);
        double highestI2 = -1;
        for (int current_trial = 0; current_trial < trials; current_trial++) {
            System.out.println("iterNumber:" + current_trial);
            init(current_trial);
            while (true) {
                double numchange = allocPoints();
                System.out.println("changes: " + numchange);
                if (numchange == 0) {
                    break;
                }
                computeCentroids();
            }

            double I2Val = getI2();
            System.out.println("I2:" + I2Val);
            if (I2Val > highestI2) {
                output.clear();
                for (Point p : docs) {
                    output.put(p.id, p.cluster);
                }
                highestI2 = I2Val;
            }
        }
        System.out.println("highest:" + highestI2);
        return output;
    }

    private double getI2() {
        double I2Val = 0;
        Map<Integer, Point> clusteringData = new HashMap<>();
        for (Point p : clusters) {
            clusteringData.put(p.id, p);
        }
        for (Point p : docs) {
            Point pr = clusteringData.get(p.cluster);
            I2Val += pr.cosinecomp(p);
        }
        return I2Val;
    }

    private void init(int trialnum) {
        clusters.clear();
        for (Point p : docs) {
            p.cluster = -1;
        }
        Random rand = new Random(seeds[trialnum]);
        for (int i = 0; i < nclusters; i++) {
            Point p = new Point(i, kcluster.I2_CRITERION, true);
            int x = rand.nextInt(n);
            p.setjv(docs.get(x).getjv());
            clusters.add(p);
        }

    }

    private int allocPoints() {
        int changes = 0;
        for (Point p : docs) {
            double sim = -1;
            Point closest = null;
            for (Point k : clusters) {
                double temp = k.cosinecomp(p);
                if (temp > sim) {
                    sim = temp;
                    closest = k;
                }
            }
            if (p.cluster != closest.id) {
                changes++;
                p.cluster = closest.id;
                closest.addPoint(p);
            } else {
                closest.addPoint(p);
            }
        }
        return changes;
    }

    private void computeCentroids() {
        for (Point p : clusters) {
            p.computeCentroid();
        }
    }
}
