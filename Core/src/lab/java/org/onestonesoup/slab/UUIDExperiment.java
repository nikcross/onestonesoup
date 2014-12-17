package org.onestonesoup.slab;

import java.util.UUID;

public class UUIDExperiment {

	public static void main(String[] args) {
		String text1 = "a 2 3";
		String text2 = "a 2 4";
		String text3 = "b 2 3";
		
		System.out.println( UUID.nameUUIDFromBytes(text1.getBytes()) );
		System.out.println( UUID.nameUUIDFromBytes(text2.getBytes()) );
		System.out.println( UUID.nameUUIDFromBytes(text1.getBytes()) );
		System.out.println( UUID.nameUUIDFromBytes(text3.getBytes()) );
	}

}
