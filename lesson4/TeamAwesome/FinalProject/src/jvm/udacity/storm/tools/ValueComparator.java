package udacity.storm.tools;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String> {

	  private static final long serialVersionUID = -1549827195410578903L;
    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}