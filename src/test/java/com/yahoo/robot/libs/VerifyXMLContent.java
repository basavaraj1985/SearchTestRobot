package com.yahoo.robot.libs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class VerifyXMLContent {
	
	static VerifyXMLContent obj;
	
	private VerifyXMLContent()
	{
		
	}
	
	/**
	 * Not required, remove!!!
	 * since this class doesnt have any state and all the behaviours are common utilities that can be used across!
	 * 
	 * @return
	 */
	public static  VerifyXMLContent getInstance()
	{
		if(obj == null)
			obj = new VerifyXMLContent();
		return obj;
	}
	
	private static DocumentBuilder getDocBuilder() throws ParserConfigurationException
	{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}
	
	/**
	 * This method returns child node text value. 
	 * @param parentNode = Parent Node/Document object
	 * @param childTagName = Child tag name in string
	 * @return If only one child exist with specified tag name its value will be returned.
	 *         else value of first child node with specified tag name will be returned
	 */
	public static String getChildTextValue(Node parentNode, String childTagName)
	{
		String value = null;
		NodeList childs = ((Element)parentNode).getElementsByTagName(childTagName);
		if(childs != null && childs.getLength() > 0)
			value = childs.item(0).getTextContent();
		return value;
	}
	
	public static boolean tagNameExistsInXML(String xmlContent, String expTagName)
	{
		DocumentBuilder docBuilder;
		Document xmlDoc;
		NodeList childNodes;
		boolean tagExists = false;
		try {
			docBuilder = getDocBuilder();
			xmlDoc = docBuilder.parse(new StringInputStream(xmlContent));
			childNodes = xmlDoc.getElementsByTagName(expTagName);
			if(childNodes.getLength() > 0)
				tagExists = true;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//Logging.ERROR(true, "Error occered while creating XML Document Builder");
			e.printStackTrace();
			return tagExists;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			//Logging.ERROR(true, "Error occered while creating XML Document Builder from string object");
			e.printStackTrace();
			return tagExists;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Logging.ERROR(true, "Error occered while creating XML Document Builder from string object");
			e.printStackTrace();
			return tagExists;
		}
		return tagExists;
	}
	
	public static boolean tagNameExistsInXML(Document xmlDoc, String expTagName)
	{
		NodeList childNodes;
		boolean tagExists = false;
		childNodes = xmlDoc.getElementsByTagName(expTagName);
		if(childNodes.getLength() > 0)
			tagExists = true;
		return tagExists;
	}

	/**
	 * @param parent - Parent node
	 * @param expChildNode - expected child node name
	 * @return (length of string array) - if parent contains expected child nodes, (Index of child tag) - parent don't have expected child nodes, 
	 * (-1) - If child exist more than once, (-2) if parent has no childs
	 */
	public static int containUniqueChilds(Node parent, String[] expChildNodes)
	{
		NodeList childs;
		if(parent.hasChildNodes())
		{
			for(int i=0;i < expChildNodes.length;i++)
			{
				childs = ((Element)parent).getElementsByTagName(expChildNodes[i]);
				if(childs.getLength() == 0)
					return i;
				if(childs.getLength() > 1)
					return i;//return -1;
			}
		}else
			return -2;
		return expChildNodes.length;
	}
	
	/**
	 * @param parent - Parent node
	 * @param expChildNode - expected child node name
	 * @return (Length of string array) - if parent contains expected child nodes, (Index of child tag) - parent don't have expected child nodes,
	 * (-2) if parent has no childs 
	 */
	public static int containChilds(Node parent, String[] expChildNodes)
	{
		NodeList childs;
		if(parent.hasChildNodes())
		{
			for(int i=0;i < expChildNodes.length;i++)
			{
				childs = ((Element)parent).getElementsByTagName(expChildNodes[i]);
				if(childs.getLength() == 0)
					return i;
			}
		}else
			return -2;
		return expChildNodes.length;
	}
	
	public static int getChildsCount(Node parent, String expChildNode)
	{
		return ((Element)parent).getElementsByTagName(expChildNode).getLength();
	}
	
	/**
	 * @param parent - Parent node
	 * @param expChildNode - expected child node name
	 * @return (1) - if parent contains expected child node, (0) - parent exist and don't have expected child node, 
	 */
	
	public static int containChild(Node parent, String expChildNode)
	{
		int status = 0;
		NodeList childs;
		if(parent.hasChildNodes()){
			childs = parent.getChildNodes();
			for(int i = 0;i < childs.getLength();i++)
			{
				if(expChildNode.equals(childs.item(i).getNodeName()))
					status = 1;
			}
		}
		return status;
	}
	
	/**
	 * @param parent - Parent node
	 * @param expChildNode - expected child node name
	 * @return (1) - if parent contains expected child node, (0) - parent exist and don't have expected child node, 
	 * (-1) - If child exist more than once
	 */
	
	public static int containUniqueChild(Node parent, String expChildNode)
	{
		int status = 0;
		NodeList childs;
		if(parent.hasChildNodes()){
			childs = parent.getChildNodes();
			for(int i = 0;i < childs.getLength();i++)
			{
				if(expChildNode.equals(childs.item(i).getNodeName()))
					if(status == 0)
						status = 1;
					else
						status = -1;
			}
		}
		return status;
	}
	
	/**
	 * @param xmlContent - XML content that has to be verified
	 * @param expTagName - expected child node
	 * @param parentTagName - parent node
	 * @return (No of parents) - if parent node exist and has expected child node, (parent index - index starts with 0) - if parent exist and don't have expected child node, 
	 * (-1) - if parent node doesn't exist, (-2) - If any exception is thrown
	 */
	
	public static  int parentContainsChild(String xmlContent, String expTagName, String parentTagName)
	{
		DocumentBuilder docBuilder;
		Document xmlDoc;
		NodeList parentNodes;
		int status = 0;
		try {
			docBuilder = getDocBuilder();
			xmlDoc = docBuilder.parse(new StringInputStream(xmlContent));
			parentNodes = xmlDoc.getElementsByTagName(parentTagName);
			if(parentNodes.getLength() > 0){
				for(int i = 0; i < parentNodes.getLength();i++)
				{
					int val = containChild(parentNodes.item(i),expTagName);
					if(val == 0 || val == -1){
						return i;
					}
					if(val == 1 && i == parentNodes.getLength()-1)
						status = parentNodes.getLength();
				}
			}else{
				status = -1;
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//Logging.ERROR(true, "Error occered while creating XML Document Builder");
			e.printStackTrace();
			status = -2;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			//Logging.ERROR(true, "Error occered while creating XML Document Builder from string object");
			e.printStackTrace();
			status = -2;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Logging.ERROR(true, "Error occered while creating XML Document Builder from string object");
			e.printStackTrace();
			status = -2;
		}
		return status;
	}
	
	/**
	 * @param xmlContent - XML content that has to be verified
	 * @param expTagName - expected child node
	 * @param parentTagName - parent node
	 * @return (No of parents) - if parent node exist and has expected child node, (parent index - index starts with 0) - if parent exist and don't have expected child node, 
	 * (-1) - if parent node doesn't exist
	 */
	public static int parentContainsChild(Document xmlDoc, String expTagName, String parentTagName)
	{
		NodeList parentNodes;
		int status = 0;
		parentNodes = xmlDoc.getElementsByTagName(parentTagName);
		if(parentNodes.getLength() > 0){
			for(int i = 0; i < parentNodes.getLength();i++)
			{
				int val = containChild(parentNodes.item(i),expTagName);
				if(val == 0 || val == -1){
					return i;
				}
				if(val == 1 && i == parentNodes.getLength()-1)
					status = parentNodes.getLength();
			}
		}else{
			status = -1;
		}
		return status;
	}
	
	
//	public static void validateAgainstSchema( Document xmlDoc, String xsdLocation )
//	{
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setValidating(true);
//		factory.setAttribute(
//				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
//				"http://www.w3.org/2001/XMLSchema");
//		factory.setAttribute(
//				"http://java.sun.com/xml/jaxp/properties/schemaSource", xsdLocation);
//		Document doc = null;
//		try {
//			DocumentBuilder parser = factory.newDocumentBuilder();
//			parser.parse(new Chara)
//			doc = parser.parse("data.xml");
//		} catch (ParserConfigurationException e) {
//			System.out.println("Parser not configured: " + e.getMessage());
//		} catch (SAXException e) {
//			System.out.print("Parsing XML failed due to a "
//					+ e.getClass().getName() + ":");
//			System.out.println(e.getMessage());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
			 * Validate content against an XML schema
	621		 * @param xsdPath Path to the XML schema file
	622		 * @param format The content format (xml|json)
	623		 * @throws SAXException when response does not conform to the given schema
	624		 * @throws Exception for other errors
	625		 */
			public static void verifyXMLSchema(byte[] content, String schemaFile) 
			throws ParserConfigurationException, SAXException, IOException {
		        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		        // Configure XML schema for document
		        final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
		        final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
		        factory.setValidating(true);
		        factory.setNamespaceAware(true);
		        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, "http://www.w3.org/2001/XMLSchema");
		        factory.setAttribute(JAXP_SCHEMA_SOURCE, new File(schemaFile));            
		
		        DocumentBuilder builder;
		
		        builder = factory.newDocumentBuilder();
		        builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
		
		            public void error(SAXParseException exception)
		            throws SAXException {
		                throw exception;                        
		            }
		
		            public void fatalError(SAXParseException exception)
		            throws SAXException {
		            	throw exception;
		            }
		
		            public void warning(SAXParseException exception)
		            throws SAXException {
		                System.out.println("** Warning" 
		                        + ", line " + exception.getLineNumber()
		                        + ", uri " + exception.getSystemId()
		                        + "    " + exception.getMessage());                   
		            }
		
		        });
		        builder.parse(new ByteArrayInputStream(content));
		    }
		

}
