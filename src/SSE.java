
import java.util.*;
import java.util.Map.Entry;

public class SSE {

    static final int[] seeds = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39};
    int nclusters;
    int trials;
    int n;
    List<Point> docs;
    Set<Point> clusters;

    public SSE(int numclus, int numtrials, Map<Integer, Map<Integer, Double>> ijv) {
        clusters = new HashSet<>();
        docs = new ArrayList<>();
        this.nclusters = numclus;
        this.trials = numtrials;
        this.n = ijv.size();
        for (Entry<Integer, Map<Integer, Double>> currentEntry : ijv.entrySet()) {
            Point p = new Point(currentEntry.getKey(), kcluster.SSE_CRITERION, false);
            p.setjv(currentEntry.getValue());
            docs.add(p);
        }
    }

    public Map<Integer, Integer> cluster() {
        Map<Integer, Integer> output = new HashMap<>();
        System.out.println("N=" + n);
        double lowestSSE = Double.MAX_VALUE;
        for (int current_trial = 0; current_trial < trials; current_trial++) {
            System.out.println("iterNumber:" + current_trial);
            init(current_trial);
            while (true) {
                double numchange = allocPoints();
                System.out.println("NUMCHANHES: " + numchange);
                if (numchange / (double) n < 0.01) {
                    break;
                }
                computeCentroids();
            }

            double SSEVal = 0;
            Map<Integer, Point> clusteringData = new HashMap<>();
            for (Point p : clusters) {
                clusteringData.put(p.id, p);
            }
            for (Point p : docs) {
                Point pr = clusteringData.get(p.cluster);
                for (Integer i : pr.getjv().keySet()) {
                    if (p.getjv().containsKey(i)) {
                        SSEVal += Math.pow(pr.getjv().get(i) - p.getjv().get(i), 2);
                    }
                }
            }
            System.out.println("SSE:" + SSEVal);
            if (SSEVal < lowestSSE) {
                output.clear();
                for (Point p : docs) {
                    output.put(p.id, p.cluster);
                }
                lowestSSE = SSEVal;
            }
        }
        System.out.println("lowest" + lowestSSE);
        return output;
    }

    private void init(int trialnum) {
        clusters.clear();
        for (Point p : docs) {
            p.cluster = -1;
        }
        Random rand = new Random(seeds[trialnum]);
        for (int i = 0; i < nclusters; i++) {
            Point p = new Point(i, kcluster.SSE_CRITERION, true);
            int x = rand.nextInt(n);
            p.setjv(docs.get(x).getjv());
            clusters.add(p);
        }

    }

    private int allocPoints() {
        int changes = 0;
        for (Point p : docs) {
            double dist = Double.MAX_VALUE;
            Point closest = null;
            for (Point k : clusters) {
                double temp = k.ssecomp(p);
                if (temp < dist) {
                    dist = temp;
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
