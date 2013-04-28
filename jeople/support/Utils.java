package jeople.support;

import jeople.Entity;
import jeople.Query;

/**
 * Internal utility class.
 * 
 * @author Reda El Khattabi
 */
class Utils {
	static String toString(Query<?> q) {
		String s = "";
		String sep = "";
		for (Entity e : q) {
			s += sep + e.toString();
			sep = "\n";
		}
		s += "";
		return s;
	}

	static String getSimpleClassName(Object o) {
		String[] ss = o.getClass().getName().split("[\\.\\$]");
		return ss[ss.length - 1];
	}
}
