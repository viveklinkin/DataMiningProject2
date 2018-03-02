
import java.util.*;
import java.util.Map.Entry;

public class E1 {

    static final int[] seeds = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39};
    int nclusters;
    int trials;
    int n;
    List<Cluster> docs;
    HashMap<Integer, Cluster> clusters;
    Cluster globalCenter;
    double e1crit = Double.MAX_VALUE;

    public E1(int numclus, int numtrials, Map<Integer, Map<Integer, Double>> ijv) {
        clusters = new HashMap<>();
        docs = new ArrayList<>();
        this.nclusters = numclus;
        this.trials = numtrials;
        this.n = ijv.size();
        for (Entry<Integer, Map<Integer, Double>> currentEntry : ijv.entrySet()) {
            Cluster p = new Cluster(currentEntry.getKey());
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
                //computeCentroids();
            }
            double E1Val = getE1();
            System.out.println("E1:" + E1Val);
            if (E1Val < lowestE1) {
                output.clear();
                for (Cluster p : docs) {
                    output.put(p.id, p.cluster);
                }
                lowestE1 = E1Val;
            }
        }
        System.out.println("lowest:" + lowestE1);
        return output;
    }

    private double getE1() {
        double E1Val = 0;
        for (Cluster p : clusters.values()) {
            E1Val += p.n * globalCenter.cosinecomp(p);
        }
        return E1Val;
    }

    private void init(int trialnum) {
        clusters.clear();
        Random rand = new Random(seeds[trialnum]);
        for (int i = 0; i < nclusters; i++) {
            Cluster p = new Cluster(i);
            int x = rand.nextInt(n);
            p.setjv(docs.get(x).getjv());
            clusters.put(i, p);
        }
        globalCenter = new Cluster(-1);
        for (Cluster p : docs) {
            globalCenter.addPoint(p);
            int x = -1;
            boolean x1 = false;
            double dist = -1;
            for (Cluster c : clusters.values()) {
                if (dist < c.cosinecomp(p)) {
                    dist = c.cosinecomp(p);
                    x = c.id;
                    x1 = true;
                }
            }
            p.cluster = x;
            clusters.get(x).addPoint(p);
        }
        e1crit = getE1();
    }

    private int allocPoints() {
        int changes = 0;
        for (Cluster p : docs) {
            double Hsim = Double.MAX_VALUE;
            Cluster farthest = null;
            for (Cluster k : clusters.values()) {
                if (k.id != p.cluster) {
                    double temp1 = k.cosinecomp(globalCenter);
                    double temp2 = clusters.get(p.cluster).cosinecomp(globalCenter);
                    double temp4 = k.ifPointAdded(p.getjv(), globalCenter);
                    double temp3 = clusters.get(p.cluster).ifPointRemoved(p.getjv(), globalCenter);
                    double e1critd = e1crit + temp4 + temp3 - temp2 - temp1;
                    if (e1critd < e1crit) {
                        if (e1critd < Hsim) {
                            Hsim = e1critd;
                            farthest = k;
                        }
                    }
                }
            }
            if (farthest != null) {
                changes++;
                farthest.addPoint(p);
                Cluster currentc = clusters.get(p.cluster);
                currentc.removePoint(p.getjv());
                p.cluster = farthest.id;
            }
        }
        System.out.println(e1crit);
        return changes;
    }
}
