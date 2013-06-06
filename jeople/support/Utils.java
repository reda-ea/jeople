package jeople.support;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jeople.Entity;
import jeople.Query;
import jeople.errors.InternalError;
import jeople.support.DataSourceSupport.EntityInfo;

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

	static Object getHiddenField(Entity entity, String field) {
		try {
			Field dsf = Entity.class.getDeclaredField(field);
			if (dsf.isAccessible())
				throw new InternalError("Only use this for the internal fields");
			dsf.setAccessible(true);
			Object o = dsf.get(entity);
			dsf.setAccessible(false);
			return o;
		} catch (NoSuchFieldException e) {
			throw new InternalError(e);
		} catch (SecurityException e) {
			throw new InternalError(e);
		} catch (IllegalArgumentException e) {
			throw new InternalError(e);
		} catch (IllegalAccessException e) {
			throw new InternalError(e);
		}
	}

	static void setHiddenField(Entity entity, String field, Object value) {
		try {
			Field dsf = Entity.class.getDeclaredField(field);
			if (dsf.isAccessible())
				throw new InternalError("Only use this for the internal fields");
			dsf.setAccessible(true);
			dsf.set(entity, value);
			dsf.setAccessible(false);
		} catch (NoSuchFieldException e) {
			throw new InternalError(e);
		} catch (SecurityException e) {
			throw new InternalError(e);
		} catch (IllegalArgumentException e) {
			throw new InternalError(e);
		} catch (IllegalAccessException e) {
			throw new InternalError(e);
		}
	}

	private static boolean check_method(Method method, Object[] parameters) {
		if (method.getParameterTypes().length != parameters.length)
			return false;
		for (int i = 0; i < parameters.length; ++i)
			if (!method.getParameterTypes()[i].isAssignableFrom(parameters[i]
					.getClass()))
				return false;
		return true;
	}

	static Object runHiddenMethod(Entity entity, String method,
			Object... parameters) {
		for (Method m : Entity.class.getDeclaredMethods()) {
			if (!m.getName().equals(method))
				continue;
			if (!check_method(m, parameters))
				continue;
			if (m.isAccessible())
				throw new InternalError("Only use this for the internal fields");
			try {
				m.setAccessible(true);
				Object o = m.invoke(entity, parameters);
				m.setAccessible(false);
				return o;
			} catch (IllegalAccessException e) {
				throw new InternalError(e);
			} catch (IllegalArgumentException e) {
				throw new InternalError(e);
			} catch (InvocationTargetException e) {
				throw new InternalError(e);
			}
		}
		throw new InternalError("Method '" + method + "' not found");
	}
}
