package org.onestonesoup.core.data;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.onestonesoup.core.data.EntityTree;
import org.onestonesoup.core.data.XmlHelper;
import org.onestonesoup.core.data.XmlHelper.XmlParseException;

public class XmlHelperTest {

	@Test
    public void toXmlWithValueRenders()
    {
            String xml = "<data>a value</data>";
            
            EntityTree element = null;
            try{
                    element = XmlHelper.parseElement(xml);
            }
            catch(XmlParseException xe)
            {
                    fail(xe.getMessage());
            }
            
            assertEquals(xml,XmlHelper.toXml(element));
    }
    
	@Test
	public void testCanParseMinimalTag() {
		String xml = "<t1><t2/><t3/><t4/></t1>";
		
		EntityTree element = null;
		try {
			element = XmlHelper.parseElement(xml);
		} catch (XmlParseException xe) {
			fail(xe.getMessage());
		}
		
		assertEquals("t1",element.getName());
		assertEquals(3,element.getChildren().size());
		assertEquals("t2",element.getChildren().get(0).getName());
	}
	
	@Test
    public void toXmlBeautifyWithValueRenders()
    {
            String xml = "<doc>\n\t<data>a value</data>\n</doc>";
            
            EntityTree element = null;
            try{
                    element = XmlHelper.parseElement(xml);
            }
            catch(XmlParseException xe)
            {
                    fail(xe.getMessage());
            }
            
            assertEquals(xml,XmlHelper.toXml(element));
    }
    
	@Test
    public void toXmlWithMultipleChildrenRenders()
    {
            String xml = "<data>\n"+
                                            "\t<value>a</value>\n"+
                                            "\t<value>\n"+
                                                    "\t\t<value value=\"b\"/>\n"+
                                                    "\t\t<value>b</value>\n"+
                                            "\t</value>\n" +
                                    "</data>";
            
            EntityTree element = null;
            try{
                    element = XmlHelper.parseElement(xml);
            }
            catch(XmlParseException xe)
            {
                    fail(xe.getMessage());
            }
            
            assertEquals(xml,XmlHelper.toXml(element));
    }       
//	@Test
    public void XmlHelperCanParseAttributeWithGTandLTInValue()
    {
            String nonXmlData = " >some <data> in here '< ";
            String xmlData="<xml><element attribute=\""+nonXmlData+"\"></element></xml>";
            
            EntityTree doc = null;
            try{
                    doc = XmlHelper.parseElement( xmlData );
            }
            catch(XmlParseException pex)
            {
                    fail(pex.getMessage());
            }
            
            String result = doc.getChild("element").getAttribute("attribute");
            
            assertEquals("Failed to parse attribute",nonXmlData,result);            
    }
	@Test
	@Ignore
    public void parserReadsCDATA()
    {
            String nonXmlData = " >some \" \n line2 \n line3 [text] in here '< ";
            String xmlData="<xml><cdata><![CDATA["+nonXmlData+"]]></cdata></xml>";
            
            EntityTree doc = null;
            try{
                    doc = XmlHelper.parseElement( xmlData );
            }
            catch(XmlParseException pex)
            {
                    fail(pex.getMessage());
            }
            
            String result = doc.getChild("cdata").getValue();
            
            assertEquals("Failed to parse CDATA",nonXmlData,result);
    }
    @Test
	@Ignore
    public void parserWritesCDATA()
    {
            String nonXmlData = " >some \" \n line2 \n line3 [text] in here '< ";
            String xmlData="<xml><cdata><![CDATA["+nonXmlData+"]]></cdata></xml>";          

            EntityTree doc = null;
            try{
                    doc = XmlHelper.parseElement( xmlData );
            }
            catch(XmlParseException pex)
            {
                    fail(pex.getMessage());
            }

            String result = XmlHelper.toXml(doc,false);

            assertEquals("Failed to write CDATA",xmlData,result);
    }
//    @Test
    public void parserReadsDOCTYPE()
    {
            String xmlData="<!DOCTYPE project ["+
                    "<!ENTITY global_properties SYSTEM \"file:global_properties.xml\">"+
                    "<!ENTITY global_classpaths SYSTEM \"file:global_classpaths.xml\">"+
                    "<!ENTITY global_taskdefs SYSTEM \"file:global_taskdefs.xml\">"+
                    "<!ENTITY global_targets SYSTEM \"file:global_targets.xml\">"+
                    "<!ENTITY properties SYSTEM \"file:properties.xml\">"+
                    "]><doc><data><![CDATA[text]]></data></doc>";
            
            EntityTree doc = null;
            try{
                    doc = XmlHelper.parseElement( xmlData );
            }
            catch(XmlParseException pex)
            {
                    fail(pex.getMessage());
            }
            
            String result = XmlHelper.toXml(doc,false);
            
            assertEquals("Failed to parse DOCTYPE","<doc><data><![CDATA[text]]></data></doc>",result);
    }
//    @Test
    public void parserReadsDOCTYPEandENTITIES()
    {
            String xmlData="<!DOCTYPE project ["+
                    "<!ENTITY global_properties TEST \"file:global_properties.xml\">"+
                    "]><doc><data><![CDATA[text]]></data></doc>";
            
            EntityTree doc = null;
            try{
                    doc = XmlHelper.parseElement( xmlData );
            }
            catch(XmlParseException pex)
            {
                    fail(pex.getMessage());
            }
            
            String result = XmlHelper.toXml(doc,false);
            
            assertEquals("Failed to parse DOCTYPE","<doc><data><![CDATA[text]]></data></doc>",result);
            
    }     
//    @Test
    public void parserReadsENTITY()
    {
            String xmlData="<!ENTITY name \"the name\"><doc><data><![CDATA[text]]></data></doc>";
            
            EntityTree doc = null;
            try{
                    doc = XmlHelper.parseElement( xmlData );
            }
            catch(XmlParseException pex)
            {
                    fail(pex.getMessage());
            }
            
            String result = XmlHelper.toXml(doc,false);
            
            assertEquals("Failed to parse CDATA","<doc><data><![CDATA[text]]></data></doc>",result);
    }
    @Test
    public void canParseElementAttribute()
    {
            String data="<name attrib=\"value\"/>";
            
            EntityTree result = null;
            try{
                    result = XmlHelper.parseElement( data );
            }
            catch(XmlParseException xe)
            {
                    fail(xe.getMessage());
            }
            
            assertEquals(data,XmlHelper.toXml(result).trim());
    }
    @Test
    public void canParseElementAttributeWithSpaceAroundEquals()
    {
            String data="<name attrib = \"value\"/>";
            String expectedResult="<name attrib=\"value\"/>";
            
            EntityTree result = null;
            try{
                    result = XmlHelper.parseElement( data );
            }
            catch(XmlParseException xe)
            {
                    fail(xe.getMessage());
            }
            
            assertEquals(expectedResult,XmlHelper.toXml(result).trim());
    }
    @Test
    public void canParseElementAttributeAfterLineBreak()
    {
            String data="<name \nattrib=\"value\"/>";
            String expectedResult="<name attrib=\"value\"/>";
            
            EntityTree result = null;
            try{
                    result = XmlHelper.parseElement( data );
            }
            catch(XmlParseException xe)
            {
                    fail(xe.getMessage());
            }
            
            assertEquals(expectedResult,XmlHelper.toXml(result).trim());
    }
    
    @Test
    public void testCanParseCDATA() {
    	String data = "<data>\n<pageName><![CDATA[OpenForum/JarManager/Mailer]]></pageName></data>";
    	
        EntityTree result = null;
        try{
                result = XmlHelper.parseElement( data );
                assertEquals("OpenForum/JarManager/Mailer",result.getChild("pageName").getValue());
        }
        catch(XmlParseException xe)
        {
                fail(xe.getMessage());
        }
    }
}
