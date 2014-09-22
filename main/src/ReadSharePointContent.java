

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



/**
 * This program was made with the intention to link it with the SharePoint Lists
 * Web Services. It is a proof of concept that can read SharePoint lists.
 */

public class ReadSharePointContent
{
	private static final Logger LOGGER = Logger.getLogger(ReadSharePointContent.class.getName());
	
    private Map<String, String> sharePointContents = new HashMap<String, String>();

   

    public ReadSharePointContent()
    {
    }
   
    /**
     * This method is used to load all sharepoint content.
     */

    public void loadContent()
    {
        try
        {
            String xmlFile = "sharepointcontent.xml";        
            fetchContent(xmlFile);
            getNodesFromXML(xmlFile);
        } catch (Exception e)
        {
        	LOGGER.log(Level.INFO,"Error caught in loadContent method of ReadSharePointContent",e);
        }
    }
    
    /**
     * This method is used to get the content form the sharepoint for a specific item
     * @param docURL     
     * @return
     * @throws Exception
     */
    
    public InputStream getItem(String docURL) throws Exception{
    	CloseableHttpResponse response = null;
    	try { 
            String host = MessageResourceUtil.getMessage("sharepoint.host");                     
            CloseableHttpClient httpclient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
      	  	HttpPost post = new HttpPost(docURL);
      	  	HttpHost target = new HttpHost(host, 80, "http");    		  	    
    		response = httpclient.execute(target, post,  connectSharepoint());           
        } catch (Exception e)
        {
        	LOGGER.log(Level.INFO,"Error caught in getListStub method of ReadSharePointContent",e);
        }
		return response.getEntity().getContent();
    }
    
    /**
     * This method is used to fetch cotnent from the Sharepoint List Service
     * @param xmlFile
     */
    
    private void fetchContent(String xmlFile){
    	try { 
          String host = MessageResourceUtil.getMessage("sharepoint.host");
          String url = MessageResourceUtil.getMessage("sharepoint.url");
          String listName = MessageResourceUtil.getMessage("sharepoint.listname");
          String viewName = MessageResourceUtil.getMessage("sharepoint.viewname");
          String rowLimit =MessageResourceUtil.getMessage("sharepoint.rowlimit");
    	  CloseableHttpClient httpclient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
    	  HttpPost post = new HttpPost(url);
    	  HttpHost target = new HttpHost(host, 80, "http");
  		  post.setHeader("Charset", "UTF-8");
  		  StringEntity se = new StringEntity("<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetListItems xmlns=\"http://schemas.microsoft.com/sharepoint/soap/\"><listName>"+listName+"</listName><viewName>"+viewName+"</viewName><rowLimit>"+rowLimit+"</rowLimit></GetListItems></soap:Body></soap:Envelope>");
  		  se.setContentType("text/xml");
  	         post.setEntity(se);
  	        CloseableHttpResponse response = null;
  		 response = httpclient.execute(target, post,  connectSharepoint());
  		 HttpEntity entity = response.getEntity();
  				
  		if (entity != null) {
  			File file = new File(xmlFile);
  			// if File exist delete it
  			if (file.exists()) {
  				file.delete();
  			}
  			// if File not exist create a new file
  			if (!file.exists()) {
  				FileOutputStream fos = new FileOutputStream(file);
  				BufferedOutputStream bos = new BufferedOutputStream(fos);		
  				entity.writeTo(bos);					
  				bos.flush();
  				bos.close();
  				fos.flush();
  				fos.close();
  			}

  		}
          
      } catch (Exception e)
      {
      	LOGGER.log(Level.INFO,"Error caught in getListStub method of ReadSharePointContent",e);
      }
    }
    
    /**
     * This method is used to create NTLM setup for Sharepoint Authentication
     * @return
     */
    
    
    private HttpClientContext connectSharepoint(){
    	
    	  
    	   String username = MessageResourceUtil.getMessage("sharepoint.username");
           String password = MessageResourceUtil.getMessage("sharepoint.password");
    	   CredentialsProvider credsProvider = new BasicCredentialsProvider();
    	   credsProvider.setCredentials(AuthScope.ANY, new NTCredentials(username,password, "", ""));    		
    	   HttpClientContext context = HttpClientContext.create();
    	   context.setCredentialsProvider(credsProvider);
    	   return context;
    	
    }
    
     private NodeList getNodesFromXML(String xmlFile) throws IOException, SAXException, ParserConfigurationException,XMLStreamException {
		XMLInputFactory factory2 = XMLInputFactory.newInstance();
		// Create the XML event reader
		FileReader reader = new FileReader(xmlFile);
		XMLEventReader r = factory2.createXMLEventReader(reader);
		// Loop over XML input stream and process events
		while (r.hasNext()) {
			XMLEvent e = (XMLEvent) r.next();
			processEvent(e);
		}

		return null;
	}

    private void processEvent(XMLEvent e)
    {
        if (e.isStartElement())
        {

            Iterator<Attribute> iter = ((StartElement) e).getAttributes();
            String aliasId = "";
            String content = "";
            while (iter.hasNext())
            {
                Attribute attr = (Attribute) iter.next();
                QName attributeName = attr.getName();
                String attributeValue = attr.getValue();
                if (attributeName.toString()
                        .equalsIgnoreCase("ows_ItemAliasId"))
                {
                    aliasId = attributeValue;
                }

                if (attributeName.toString().equalsIgnoreCase("ows_MetaInfo"))
                {
                    content = attributeValue;
                }
            }
            if (!content.equalsIgnoreCase("") && !aliasId.equalsIgnoreCase(""))
            {
            
                sharePointContents.put(aliasId, parseContent(content));
            }

        }

    }
  
    private String parseContent(String content)
    {
        String publish = "PublishingPageContent:SW|";
        int from = content.lastIndexOf(publish);
        int to = content.indexOf("vti_contentversionisdirty:BW|false");
        String parsedContent = "";
        if (to > from + publish.length()){
            parsedContent = content.substring(from + publish.length(), to);
        }      
        String replaceNewLine = Pattern.quote("\\n");
        String replaceReturn = Pattern.quote("\\r");
        String newLineRemoved = parsedContent.replaceAll(replaceNewLine, "");
        String finalContent = newLineRemoved.replaceAll(replaceReturn, "");
        String spaceContent = finalContent.replaceAll("<p> </p>", "");   
        String cleanContent = spaceContent.replaceAll("\\P{Print}", "");     
        return cleanContent;
    }

    public String getContentOnId(String contentId)
    {
        return sharePointContents.get(contentId);
    }

    public Map<String, String> getSharePointContents()
    {
        return sharePointContents;
    }

    public void setSharePointContents(Map<String, String> sharePointContents)
    {
        this.sharePointContents = sharePointContents;
    }

}