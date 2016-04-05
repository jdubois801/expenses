package expense.api.services;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// A utility class to copy bean properties, ignoring null properties in the source.
// Sorry to do this the hard way, but we're avoiding a dependency on Apache BeanUtils
public class NullAwareBeanUtils {

	public static void copy(Object dest, Object src) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvocationTargetException {
		for (Field f : src.getClass().getDeclaredFields()) {
			Method getterMethod = getterMethod(src.getClass(), f);
			
			// A little trick from the JPA internals - we call getter and setter methods instead of calling field.get() or field.set() directly
			// because this avoids issues with visibility of declared fields.  
			
			Object value = getterMethod.invoke(src);
			
			if (value != null) {
				Method setter = setterMethod(dest.getClass(), f);
				setter.invoke(dest, value);
			}
		}
	}

	// get the setter method for the specified field in the class.
	private static Method setterMethod(Class<?> clz, Field f) throws NoSuchFieldException {
		String setterMethodName = setterMethodName(f);
		
		for (Method meth : clz.getDeclaredMethods()) {
			if (meth.getName().equals(setterMethodName)) {
				return meth;
			}
		}
		
		throw new NoSuchFieldException();
	}
	
	// get the getter method for the specified field in the class.
	private static Method getterMethod(Class<?> clz, Field f) throws NoSuchFieldException {
		String getterMethodName = getterMethodName(f);
		
		// naively select the first declared method with a name match instead of a full method signature match.
		// a poor idea for classes in general but almost always fine for data storage POJOs.
		for (Method meth : clz.getDeclaredMethods()) {
			if (meth.getName().equals(getterMethodName)) {
				return meth;
			}
		}
		
		throw new NoSuchFieldException();
	}

	
	private static String getterMethodName(Field f) {
		return "get" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
	}

	private static String setterMethodName(Field f) {
		return "set" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
	}
}
