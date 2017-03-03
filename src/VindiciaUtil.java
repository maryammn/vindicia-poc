
/**
 * A class that provides utility methods that perform common functionalities 
 * sometimes needed for various Vindicia services
 * @author mnaeini
 *
 */
public class VindiciaUtil {
	
	/**
	 * Generates a unique Id on our end that we pass to Vindicia when we are creating billingplans, products, autobills, etc.
	 * Vindicia refers to this ids as merchant+objectname E.G merchandProductId, merchantAutoBillId, etc. 
	 * In Vindicia prodtest UI this ID is referred as object name ID E.G Autobill ID, Account ID which is different from VID and it has to be unique
	 * 
	 * @param prefix
	 * @return a unique Vindicia Id given this prefix. This refers to the unique Id's of object that we store in our db
	 */
	public static String createUniqueId(String prefix) {
		return prefix + System.currentTimeMillis();
	}
}
