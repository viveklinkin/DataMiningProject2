
import java.util.*;
import java.util.Map.Entry;

class Cluster {

    private LinkedHashMap<Integer, Double> jv;
    int id, cluster;
    double n;
    double magn = 0;

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

    public double cosinecomp(Cluster b) {
        double res = 0;
        Cluster smaller = (jv.size() > b.getjv().size()) ? b : this;
        Cluster larger = (jv.size() > b.getjv().size()) ? this : b;
        for (Integer i : smaller.getjv().keySet()) {
            if (larger.getjv().containsKey(i)) {
                res += larger.getjv().get(i) * smaller.getjv().get(i);
            }
        }
        res = res / (larger.magnitude() * smaller.magnitude());
        return (res > 1) ? 1 : res;
    }

    public void setjv(Map<Integer, Double> newjv) {
        jv.clear();
        if (newjv != null) {
            for (Integer i : newjv.keySet()) {
                jv.put(i, newjv.get(i));
            }
        }
        n = 1;
        magn = 0;
        magn = magnitude();
    }

    public Map<Integer, Double> getjv() {
        return this.jv;
    }

    public Cluster(int id) {
        jv = new LinkedHashMap<>();
        this.id = id;
        cluster = -1;
        n = 0;
    }

    void addPoint(Cluster poi) {
        addPoint(poi.getjv());
    }

    public double ifPointAdded(Map<Integer, Double> newjv, Cluster gloCent) {
        addPoint(newjv);
        double res = cosinecomp(gloCent);
        removePoint(newjv);
        return res;
    }

    public double ifPointRemoved(Map<Integer, Double> newjv, Cluster gloCent) {
        removePoint(newjv);
        double res = cosinecomp(gloCent);
        addPoint(newjv);
        return res;
    }

    void addPoint(Map<Integer, Double> newjv) {
        magn = 0;
        for (int i : jv.keySet()) {
            jv.put(i, jv.get(i) * n);
        }
        for (Entry<Integer, Double> currentEntry : newjv.entrySet()) {
            if (jv.containsKey(currentEntry.getKey())) {
                jv.put(currentEntry.getKey(), jv.get(currentEntry.getKey()) + currentEntry.getValue());
            } else {
                jv.put(currentEntry.getKey(), currentEntry.getValue());
            }
        }
        for (int i : jv.keySet()) {
            jv.put(i, jv.get(i) / (n + 1));
        }
        magn = magnitude();
        n++;
    }

    void removePoint(Map<Integer, Double> newjv) {
        magn = 0;
        for (int i : jv.keySet()) {
            jv.put(i, jv.get(i) * n);
        }
        for (Entry<Integer, Double> currentEntry : newjv.entrySet()) {
            if (jv.containsKey(currentEntry.getKey())) {
                double x = ((jv.get(currentEntry.getKey())) - currentEntry.getValue());
                if (x > 0.00001) {
                    jv.put(currentEntry.getKey(), x);
                } else {
                    jv.remove(currentEntry.getKey());
                }
            }
        }
        for (int i : jv.keySet()) {
            jv.put(i, jv.get(i) / n - 1);
        }
        magn = magnitude();
        n--;
    }
}
