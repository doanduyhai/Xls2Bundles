package fr.doan;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils
{
	public static Object getField(Object o, String fieldName)
	{
		Field field = getAccessibleField(fieldName, o.getClass());
		Object fieldValue = null;
		try
		{
			fieldValue = field.get(o);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return fieldValue;
	}

	public static void setField(Object o, String fieldName, Object inject)
	{
		Field field = getAccessibleField(fieldName, o.getClass());
		try
		{
			field.set(o, inject);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	// search in class, in case superclasses as well
	private static Field getAccessibleField(String fieldName, Class c)
	{
		Field field = null;
		while (c != null)
		{
			try
			{
				field = c.getDeclaredField(fieldName);
				field.setAccessible(true);
				break;
			}
			catch (NoSuchFieldException e)
			{
				c = c.getSuperclass();
			}
		}
		return field;
	}

	public static Object invoke(Object o, String methodName, Object... args)
	{

		Method[] methods = o.getClass().getDeclaredMethods();
		for (Method m : methods)
		{
			if (m.getName().equals(methodName) && m.getParameterTypes().length == args.length)
			{
				Method method = m;
				method.setAccessible(true);

				try
				{
					return method.invoke(o, args);
				}
				catch (IllegalArgumentException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
