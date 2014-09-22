Sharepoint Webservice integration with HTTPClient
=================================================

Integration with Sharepoint Webservice Using NTLM V2 Authentication. Http Component above version 4 has inbuilt support of NTLM V2 authentication. 
If we are using library below Version 4 and could not upgrade the jars we can use JCIF to integrate with NTLM.


Maven
==========
<dependency>
	<groupId>org.apache.httpcomponents</groupId>
	<artifactId>httpclient</artifactId>
	<version>4.3.5</version>
</dependency>







Integration with Webspeher Applicaiton Server
=============================================
 Websphere applicaiton server has JAX-RS plugin which has lower http version and because of which even if the applicaiton has right version of the HTTP component, it picks the version that is avaialble in the system class loader and it fails to authenticate.
 
 In order to overcome that issue we can create an isolate shared library and map the library for that applicaiton so that it is able to override the system class loader jar and estabilsh NTLM authentication.








