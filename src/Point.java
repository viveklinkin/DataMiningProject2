
import java.util.*;
import java.util.Map.Entry;

class Point {

    private LinkedHashMap<Integer, Double> jv;
    int id, cluster, n;
    boolean isCentroid = false;
    String crit;
    double magn = 0;
    private LinkedHashMap<Integer, Double> allPoints;

    public double magnitude() {
        if (magn != 0) {
            return magn;
        }
        double d = 0;
        for (int i : jv.keySet()) {
            d += jv.get(i) * jv.get(i);
        }
        if (d > 1) {
            return 1.0;
        }
        return Math.sqrt(d);
    }

    public double cosinecomp(Point b) {
        double res = 0;
        Point smaller = (jv.size() > b.getjv().size()) ? b : this;
        Point larger = (jv.size() > b.getjv().size()) ? this : b;
        for (Integer i : smaller.getjv().keySet()) {
            if (larger.getjv().containsKey(i)) {
                res += larger.getjv().get(i) * smaller.getjv().get(i);
            }
        }
        return res / (larger.magnitude() * smaller.magnitude());
    }

    public double ssecomp(Point b) {
        HashSet<Integer> done = new HashSet<>();
        double res = 0;
        for (int i : b.getjv().keySet()) {
            if (!jv.containsKey(i)) {
                res += b.getjv().get(i) * b.getjv().get(i);
            } else {
                res += (jv.get(i) - b.getjv().get(i)) * (jv.get(i) - b.getjv().get(i));
                done.add(i);
            }
        }
        for (int i : jv.keySet()) {
            if (!done.contains(i)) {
                res += jv.get(i) * jv.get(i);
            }
        }
        return res;
    }

    public void setjv(Map<Integer, Double> newjv) {
        jv.clear();
        if (newjv != null) {
            for (Integer i : newjv.keySet()) {
                jv.put(i, newjv.get(i));
            }
        }
    }

    public Map<Integer, Double> getjv() {
        return this.jv;
    }

    public Point(int id, String crit, boolean isCentroid) {
        allPoints = new LinkedHashMap<>();
        jv = new LinkedHashMap<>();
        this.id = id;
        this.crit = crit;
        this.isCentroid = isCentroid;
        cluster = -1;
        n = 0;
    }

    void computeCentroid() {
        if (n == 0) {
            return;
        }
        jv.clear();
        magn = 0;
        for (Integer currentKey : allPoints.keySet()) {
            magn += (allPoints.get(currentKey) / n) * (allPoints.get(currentKey) / n);
            jv.put(currentKey, allPoints.get(currentKey) / n);
        }
        magn = Math.sqrt(magn);
        n = 0;
        allPoints.clear();
    }

    void addPoint(Point poi) {
        addPoint(poi.getjv());
    }

    void addPoint(Map<Integer, Double> newjv) {
        for (Entry<Integer, Double> currentEntry : newjv.entrySet()) {
            if (allPoints.containsKey(currentEntry.getKey())) {
                allPoints.put(currentEntry.getKey(), allPoints.get(currentEntry.getKey()) + currentEntry.getValue());
            } else {
                allPoints.put(currentEntry.getKey(), currentEntry.getValue());
            }
        }
        n++;
    }
}
