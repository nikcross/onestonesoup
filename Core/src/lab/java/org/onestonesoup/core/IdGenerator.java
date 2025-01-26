package org.onestonesoup.core;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nikcross on 10/08/17.
 */
public class IdGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException {

		System.out.println("killerton -> "+generateNumericId("killerton",9));
		System.out.println("tyntesfield -> "+generateNumericId("tyntesfield",9));
		System.out.println("tyntesfields -> "+generateNumericId("tyntesfields",9));
		System.out.println("sutton hoo -> "+generateNumericId("sutton hoo",9));

	}

	private static String generateNumericId(String name,int digits) throws NoSuchAlgorithmException {
		MessageDigest MD5 = MessageDigest.getInstance("MD5");

		byte[] data = MD5.digest(name.getBytes());
		String hex = FileHelper.printHexBinary(data);
		String number = ("" + new BigInteger(hex,16)).substring(0,digits);

		return number;
	}
}
