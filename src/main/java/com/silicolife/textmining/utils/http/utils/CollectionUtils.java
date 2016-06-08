package com.silicolife.textmining.utils.http.utils;

import java.util.Collection;
import java.util.Iterator;

public class CollectionUtils {
	
	/*
	 * author Joao Cardoso
	 */
	/**
	 * Takes a collection and returns all elements assembled in
	 * a {@link String} joined by the defined separator.
	 * <br>
	 * Example: Create a {@link String} using a {@link List<Integer>}
	 * separated by "\n":
	 * <br>
	 * <code>
	 * List<Integer> list = new ArrayList<Integer>();
	 * <br>
	 * list.add(1);
	 * <br>
	 * list.add(2);
	 * <br>
	 * list.add(3);
	 * <br>
	 * String output = CollectionUtils.join(list, "\n");
	 * <br>
	 * </code>
	 * 
	 * @param collection a {@link Collection} of objects to join.
	 * @param separator the {@link String} separator used to join the collection. 
	 * @return {@link String} joined string.
	 */
	public static String join(Collection<?> collection, String separator) {
		String output = "";
		if(collection!=null){
			Iterator<?> iterator = collection.iterator();
			while(iterator.hasNext()) {
				Object o = iterator.next();
				output += o;
				if(iterator.hasNext()) output += separator;
			}
		}
		return output;
	}
	
	/**
	 * Takes an array of Objects and returns all elements assembled in
	 * a {@link String} joined by the defined separator.
	 * <br>
	 * Example: Create a {@link String} using an {@link Integer[]}
	 * separated by "\n":
	 * <br>
	 * <code>
	 * Integer[] array = {1, 2, 3};
	 * <br>
	 * String output = CollectionUtils.join(array, "\n");
	 * <br>
	 * </code>
	 * 
	 * @param collection a {@link Collection} of objects to join.
	 * @param separator the {@link String} separator used to join the collection. 
	 * @return {@link String} joined string.
	 */
	public static String join(Object[] collection, String separator) {
		String output = "";
		if(collection !=null)
		for (int i = 0; i < collection.length-1; i++) {
			Object o = collection[i];
			output += o;
			output += separator;
		}
		if(collection.length > 0)
			output += collection[collection.length-1];
		return output;
	}

}
