package org.one.stone.soup.core.data;

/**
 * @author nikcross
 *
 */

public class BitString {
	private byte[] bits;
	private int length=0;

public BitString() {
	super();

	length = 0;

	bits = new byte[1];
}

public BitString(byte[] data) throws Exception
{
	this(data.length*8);

	for(int loop=0;loop<data.length;loop++)
	{
		setBits(loop,data[loop]);
	}
}

public BitString(boolean[] data) {
	super();

	this.length = data.length;

	if(length%8==0)
		bits = new byte[length/8];
	else
		bits = new byte[(length/8)+1];

	for(int loop=0;loop<bits.length;loop++)
	{
		bits[loop]=0;
	}

	for(int loop=0;loop<data.length;loop++)
	{
		try{setBit(loop,data[loop]);}catch(Exception e){}
	}
}

public BitString(int size) {
	super();

	length = size;

	bits = new byte[(size/8)+1];
}

public void addBit(boolean value) {

	length++;

	if(bits.length<(length/8)+1)
		increaseArraySize();

	if(value==true)
	{
		byte mask = (byte)(1 << ((length-1)%8));
		bits[length/8] += mask;
	}
}

public boolean getBit(int index) throws Exception {
	if(bits.length<index/8)
		throw new Exception("BitString.getBit("+index+") index out of range.");

	byte mask = (byte)(1 << (index%8));

	if((bits[index/8] & mask) == 0)
		return false;
	else
		return true;
}
public byte[] getBits(){
	return bits;
}
public int getLength()
{
	return length;
}
public int getSize()
{
	return bits.length;
}
private void increaseArraySize() {
	byte[] temp = new byte[bits.length+1];
	System.arraycopy(bits,0,temp,0,bits.length);
	bits[bits.length-1]=0;
	bits=temp;
}

public void setBit(int index,boolean value) throws Exception {

	if(bits.length<index/8)
		throw new Exception("BitString.setBit("+index+") index out of range Max."+bits.length*8);

	byte mask = (byte)(1 << (index%8));

	if(value==true)
	{
		if((bits[index/8] & mask)==0)
			bits[index/8] += mask;
	}
	else
	{
		if((bits[index/8] & mask)!=0)
			bits[index/8] -= mask;
	}
}

public void setBits(int byteIndex,byte byteData) throws Exception {

	if(bits.length<byteIndex)
		throw new Exception("BitString.setBits("+byteIndex+") index out of range Max."+bits.length);

	bits[byteIndex]=byteData;
}

public String toString() {
	String data = super.toString()+" Bits:[ ";

	for(int loop=0;loop<length;loop++)
	{
		try{

		if(getBit(loop)==true)
			data+="1";
		else
			data+="0";

		}catch(Exception e)
		{
			data+=e;
		}
	}

	data+=" ]";
	return data;
}
}
