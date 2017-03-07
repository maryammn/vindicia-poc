/**
 * A singleton class that holds configuration parameters for a Vindicia Client required 
 * through out the execution of various operations that interact with Vindicia
 * 
 * @author mnaeini
 * 
 */
public class VindiciaClient {
	 private static VindiciaClient client;
	   
	 /**
	     * Create private constructor
	     */
	    private VindiciaClient(){
	    	com.vindicia.client.ClientConstants.DEFAULT_VINDICIA_SERVICE_URL = "prodtest URL";
			com.vindicia.client.ClientConstants.SOAP_LOGIN = "username";
			com.vindicia.client.ClientConstants.SOAP_PASSWORD = "password";
			com.vindicia.client.ClientConstants.DEFAULT_TIMEOUT = 25000; // ms
	 }
	    /**
	     * Create a static method to get instance.
	     */
	    public static VindiciaClient getInstance(){
	        if(client == null){
	        	client = new VindiciaClient();
	        }
	        return client;
	    }
}
