
import java.util.*;
import java.util.Map.Entry;

public class E1 {

    static final int[] seeds = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39};
    int nclusters;
    int trials;
    int n;
    List<Point> docs;
    Set<Point> clusters;
    Point globalCenter;
    HashMap<Integer, Integer> sizeMap;

    public E1(int numclus, int numtrials, Map<Integer, Map<Integer, Double>> ijv) {
        clusters = new HashSet<>();
        docs = new ArrayList<>();
        this.nclusters = numclus;
        this.trials = numtrials;
        this.n = ijv.size();
        for (Entry<Integer, Map<Integer, Double>> currentEntry : ijv.entrySet()) {
            Point p = new Point(currentEntry.getKey(), kcluster.E1_CRITERION, false);
            p.setjv(currentEntry.getValue());
            docs.add(p);
        }
    }

    public Map<Integer, Integer> cluster() {
        Map<Integer, Integer> output = new HashMap<>();
        System.out.println("N=" + n);
        double lowestE1 = Double.MAX_VALUE;
        for (int current_trial = 0; current_trial < trials; current_trial++) {
            System.out.println("iterNumber:" + current_trial);
            init(current_trial);
            while (true) {
                double numchange = allocPoints();
                System.out.println("NUMCHANHES: " + numchange);
                if (numchange == 0) {
                    break;
                }
                computeCentroids();
            }

            double E1Val = 0;
//            Map<Integer, Point> clusteringData = new HashMap<>();
//            for (Point p : clusters) {
//                clusteringData.put(p.id, p);
//            }
            for(Point p : clusters){
                E1Val += sizeMap.get(p.id) * globalCenter.cosinecomp(p);
            }
            System.out.println("E1:" + E1Val);
            if (E1Val < lowestE1) {
                output.clear();
                for (Point p : docs) {
                    output.put(p.id, p.cluster);
                }
                lowestE1 = E1Val;
            }
        }
        System.out.println("lowest:" + lowestE1);
        return output;
    }

    private void init(int trialnum) {
        sizeMap = new HashMap<>();
        clusters.clear();
        globalCenter = new Point(-1, kcluster.E1_CRITERION, true);
        for (Point p : docs) {
            p.cluster = -1;
            globalCenter.addPoint(p);
        }
        globalCenter.computeCentroid();
        Random rand = new Random(seeds[trialnum]);
        for (int i = 0; i < nclusters; i++) {
            Point p = new Point(i, kcluster.I2_CRITERION, true);
            int x = rand.nextInt(n);
            p.setjv(docs.get(x).getjv());
            clusters.add(p);
            sizeMap.put(i, 0);
        }

    }

    private int allocPoints() {
        for(int i : sizeMap.keySet()){
            sizeMap.put(i, 0);
        }
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
                
            }  
            closest.addPoint(p);
            sizeMap.put(closest.id, sizeMap.get(closest.id) + 1);
        }
        return changes;
    }

    private void computeCentroids() {
        for (Point p : clusters) {
            p.computeCentroid();
        }
    }
}
