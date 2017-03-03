import com.vindicia.client.Account;
import com.vindicia.client.Address;
import com.vindicia.client.PaymentMethod;
import com.vindicia.client.VindiciaReturnException;
import com.vindicia.client.VindiciaServiceException;
import com.vindicia.soap.v20_0.Vindicia.PaymentMethodType;

/**
 * This service allows you to check entitlements status for an account once subscription and an entitlement object is created
 * @author mnaeini
 *
 */
public class EntitlementStatusServiceImpl implements VindiciaService {

	/**
	 * Check whether an account is entitled using the merchantEntitlemntId that
	 * can be associated with a billing plan or a product
	 * 
	 * @param VID
	 *            id of an account
	 * @return true or false
	 */
	public static boolean checkStatus(String VID, String merchantEntitlementID) {
		try {
			Account acct = Account.fetchByVid(null, VID);
			return acct.isEntitled(null, merchantEntitlementID);
		} catch (VindiciaReturnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VindiciaServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Test for the checkStatus method
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		VindiciaClient.getInstance();
		String emailAddress = "catch22@test.com";
		SubscriptionServiceImpl subscriptionService = new SubscriptionServiceImpl();
		Account acct = subscriptionService.createAccount(emailAddress);

		// hacking here a little bit. This is the same one as the one used in the ProductServiceImpl and
		// assigned to the product object
		String merchantEntitlementId = "MonthlyAccess";
		System.out.println("Entitlement Status for entitlement =  "
							+ merchantEntitlementId + " before subscription is "
							+ checkStatus(acct.getVID(), merchantEntitlementId));

		// now create a subscription for this account using a payment method and
		// a product code that has MonthlyAccess entitlement
		Address address = subscriptionService.fillAddress("3130 Wilshire", "LA", "CA", "90021", "US");
		PaymentMethod pm = subscriptionService.fillPaymentMethod("test@this.hallmark", 
														"maryamistesting", address, PaymentMethodType.CreditCard, "4111111111111111", "201608", "CVN", "123");
		
		subscriptionService.createSubscriptionWithAccount(acct,
														  pm,
											   			  "test_product");	

		System.out.println("Entitlement Status for this account entitlement =  "
							+ merchantEntitlementId + " after subscription "
							+ checkStatus(acct.getVID(), merchantEntitlementId));
	}

}
