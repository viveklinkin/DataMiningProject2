
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

class Point {

    private LinkedHashMap<Integer, Double> jv;
    int id, cluster, n;
    boolean isCentroid = false;
    String crit;
    private LinkedHashMap<Integer, Double> allPoints;

    public double cosinecomp(Point b){
        double res = 0;
        Point smaller = (jv.size() > b.getjv().size())? b  : this;
        Point larger = (jv.size() > b.getjv().size())? this  : b;
        for(Integer i : smaller.getjv().keySet()){
            if(larger.getjv().containsKey(i)){
                res += b.getjv().get(i) * jv.get(i);
            }
        }
        return res;
    }
    public double ssecomp(Point b) {
        HashSet<Integer> done = new HashSet<>();
        double res = 0;
        for (Integer i : b.getjv().keySet()) {
            if (!jv.containsKey(i)) {
                res += b.getjv().get(i);
            } else {
                res += Math.abs(jv.get(i) - b.getjv().get(i));
                done.add(i);
            }
        }
        for (Integer i : jv.keySet()) {
            if (!done.contains(i)) {
                res += jv.get(i);
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
        for (Integer currentKey : allPoints.keySet()) {
            jv.put(currentKey, allPoints.get(currentKey) / n);
        }
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
