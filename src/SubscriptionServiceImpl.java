import com.vindicia.client.*;
import com.vindicia.soap.v20_0.Vindicia.AutoBillItem;
import com.vindicia.soap.v20_0.Vindicia.CreditCard;
import com.vindicia.soap.v20_0.Vindicia.PaymentMethodType;

/**
 * 
 * Service that allows creating AutoBill aka subscriptions in Vindicia. 
 * An Autobill or subscription in Vindicia is associated with a payment method, an account and an AutobillItem that holds the product
 * @author mnaeini 
 */
public class SubscriptionServiceImpl implements VindiciaService {
	
	/**
	 * Create an autobill given the email address, payment information and product Id for the exisitng user
	 * @param emailAddress
	 * @param method
	 * @param productId
	 * @rerturn a Vindicia ID of the autobill created
	 */
	public String createSubscription(String emailAddress, PaymentMethod pm, String productCode) {
		
		// Create an account and a payment for this autobill
		Account acct = fillAccount(emailAddress, emailAddress);
		return createSubscriptionWithAccount(acct, pm, productCode);
	}
	
	/**
	 * Create an autobill given an account, a payment method and a product code
	 * @param emailAddress
	 * @param acct
	 * @param pm
	 * @param productCode
	 * @return
	 */
	public String createSubscriptionWithAccount(Account acct, PaymentMethod pm, String productCode) {
		
		String VID = null; 
	
		// Construct the AutoBill object given the payment method and the account object
		AutoBill abill = new AutoBill();
		abill.setMerchantAutoBillId(VindiciaUtil.createUniqueId("autobill"));	//Unique subscription ID, required
		abill.setAccount(acct);  // customer who is purchasing this subscription
		abill.setPaymentMethod(pm); 
		abill.setSourceIp("233.56.67.23"); // customer's IP address - necessary for fraud screening
		abill.setCurrency("USD");  // price in this currency must be specified on Product or BillingPlan
		
		// Set the name to the email address so that we can look this up in prodtest
		abill.setCustomerAutoBillName(acct.getEmailAddress());
		
		// This subscription uses a single Product. Create an AutoBillItem for it in order to assign it to an autobill
		AutoBillItem item = new AutoBillItem();
		// Assign a product to the AutoBillItem
		Product prod = new Product();
		prod.setMerchantProductId(productCode);
		item.setProduct(prod);

		// Unique ID for the AutoBill item
		item.setMerchantAutoBillItemId(VindiciaUtil.createUniqueId("autobill_item"));	
		abill.setItems(new AutoBillItem[] {item});
		
		// Now make the CashBox SOAP API call to create the AutoBill in CashBox
		try {
			
				System.out.println("Creating autobill...");

				AutoBillUpdateReturn abur = abill.update(
						null, 
						com.vindicia.soap.v20_0.Vindicia.ImmediateAuthFailurePolicy.doNotSaveAutoBill, 
						false, // validate PaymentMethod, if Full Amount Auth setting for merchant is turned on 
						      // this results into first cycle's bill being fully authorized
						100, // fraud score tolerance - scoring turned off here
						false, // do not ignore avs policy evaluation
						false, // do not ignore cvv policy evaluation
						null, // promo or coupon code
						false // no dry run
						, new com.vindicia.soap.v20_0.Vindicia.CancelReason().getReason_code());
				
				// If we are here, we got a 200 response back from the server - meaning
				// payment method validation, avs check, and cvv check succeeded
				// 
				if (abur.getReturnObject().getReturnCode().getValue() == 200) {
					VID = abill.getVID();
					System.out.println("Successfully created autobill with Vindicia id " + VID);					
				}
				else {
					// We should not reach here
					System.out.println("AutoBill creation failed, return code: " + abur.getReturnObject().getReturnCode() + " return string: '" + abur.getReturnObject().getReturnString() + "'");
					System.out.println("Soap id " + abur.getReturnObject().getSoapId());
				}
			
		}
		catch (VindiciaReturnException vre) {
			// If we are here, we should assume that the AutoBill creation failed
			// Example below shows various return codes from CashBox and what they mean
			
			if (vre.getReturnCode().equals("408")) {
				//CVV check failed
				System.out.println("Vindicia response string: " + vre.getMessage() + " , Call SOAP ID: " + vre.getSoapId());
					
			}
			else if (vre.getReturnCode().equals("407")) {

				//AVS Check Failed
				System.out.println("Vindicia response string: " + vre.getMessage() + " , Call SOAP ID: " + vre.getSoapId());
			}
			else if (vre.getReturnCode().equals("409")){

				//AVS and CVV Check Failed	
				System.out.println("Vindicia response string: " + vre.getMessage() + " , Call SOAP ID: " + vre.getSoapId());
			}
			else if (vre.getReturnCode().equals("410")) {
				//AVS and CVV check could not be performed
				System.out.println("Vindicia response string: " + vre.getMessage() + " , Call SOAP ID: " + vre.getSoapId());
					
			}
			else if (vre.getReturnCode().equals("402")) {
				// Card authorization failed
				System.out.println("Vindicia response string: " + vre.getMessage() + " , Call SOAP ID: " + vre.getSoapId());
			}
			else if (vre.getReturnCode().equals("400")) {
				// Other failure
				System.out.println("Vindicia response string: " + vre.getMessage() + " , Call SOAP ID: " + vre.getSoapId());
				
			}
		}
		catch (Exception e) {
			// System exception such as time out or network connectivity issue 
			// Here we have not received any response or unexpected response from our request
			e.printStackTrace();
		}
		
		// return the VID of the subscription to the caller
		return VID;
	}
	
	/**
	 * Creates a payment method given a credit card number
	 * @param emailAddress
	 * @return a payment method for now we only support credit card
	 *
	 */
	public PaymentMethod fillPaymentMethod(String emailAddress, 
										   String name, 
										   Address address, 
										   PaymentMethodType type, 
										   String creditCardNumber, 
										   String expiration,
										   String cvvName,
										   String cvvValue) {
	
		PaymentMethod pm = new PaymentMethod();

		pm.setAccountHolderName(name);
		pm.setActive(true);
		pm.setType(type);
		pm.setBillingAddress(address);
		// Creates the unique ID
		pm.setMerchantPaymentMethodId(VindiciaUtil.createUniqueId("paymentMethod"));
		
		fillCreditCardForPayment(pm, creditCardNumber, expiration, cvvName, cvvValue);
		
		return pm;
	}
	
	/**
	 * Creates an address object
	 * @param addresss1
	 * @param city
	 * @param District
	 * @param postalCode
	 * @param country
	 * @return
	 */
	public Address fillAddress(String street, String city, 
								String district, String postalCode, 
								String country) {
		// Billing address for the payment method - necessary for AVS checking
		Address addr = new Address();
		addr.setAddr1(street);
		addr.setCity(city);
		addr.setDistrict(district);
		addr.setPostalCode(postalCode);
		addr.setCountry(country);
		return addr;
	}
	
	/**
	 * Create a credit card object and assigns it to the payment method
	 * @param pm
	 * @param creditCardNumber
	 * @param expiration
	 * @param cvv
	 * @param code
	 */
	public void fillCreditCardForPayment(PaymentMethod pm, String creditCardNumber, String expiration, String cvvName, String cvvValue) {
		CreditCard cc = new CreditCard();
		cc.setAccount(creditCardNumber); // credit card number
		cc.setExpirationDate(expiration); // expiration date (YYYYMM format)
		pm.setCreditCard(cc);
		
		NameValuePair cvvNvp = new NameValuePair();
		cvvNvp.setName(cvvName);
		cvvNvp.setValue(cvvValue); // card security code (CVV code) value provided by the customer
		
		pm.setNameValues(new NameValuePair[] {cvvNvp});
	}
	
	/**
	 * This doesn't make a server call it fills in an Account object - no DB transaction
	 * @param emailAddress
	 * @param name
	 * @return an account object to be stored in Vindicia along with autobill
	 */
	public Account fillAccount(String emailAddress, String name) {
		Account  acct = new Account();
		// Set the unique Id on our side that we pass when we are creating this account
		acct.setMerchantAccountId(VindiciaUtil.createUniqueId("account")); 

		// Specify customer's email address here . This the address where customer
		// will receive CashBox generated emails
		acct.setEmailAddress(emailAddress);
		acct.setName(name);
		
		return acct;
	}
	
	/**
	 * Create an account entity and stores in the Vindicia 
	 * @param emailAddress
	 * @return
	 */
	public Account createAccount(String emailAddress) {
		Account acct = fillAccount(emailAddress, emailAddress);
		
		try {
			boolean created =  acct.update(null);
			if (created) 
				System.out.println("Account created");
			else
				System.out.println("Account updated");
		} catch (VindiciaReturnException e) {
			System.out.println("Vindicia response string: " + e.getMessage() + " , Call SOAP ID: " + e.getSoapId());
			e.printStackTrace();
		} catch (VindiciaServiceException e) {
			System.out.println("Vindicia response string: " + e.getMessage());
			e.printStackTrace();
		}
		return acct;
	}
	
	/**
	 * Test subscription creation flow
	 * @param args
	 */
	public static void main (String[] args) {
		VindiciaClient.getInstance();
		// Create a billing plan
		BillingPlanServiceImpl bpService = new BillingPlanServiceImpl();
		bpService.createBilingPlan("test_product_billing_plan");
		// Create a product and assign the billing plan to it 
		ProductServiceImpl prodService = new ProductServiceImpl();
		prodService.createProduct("test_product", "test_product_billing_plan");
		
		// Move on to creating a subscription
		SubscriptionServiceImpl subscriptionService = new SubscriptionServiceImpl();
		String emailAddress = "test@hallmarklabs.com";
		// Credit card payment method
		Address address = subscriptionService.fillAddress("3130 Wilshire", "LA", "CA", "90021", "US");
		PaymentMethod pm = subscriptionService.fillPaymentMethod(emailAddress, 
														"maryamistesting", address, PaymentMethodType.CreditCard, "4111111111111111", "201608", "CVN", "123");
		
		// Create a subscription given the email address, payment method and a test product
		subscriptionService.createSubscription(emailAddress, pm, "test_product");
	}
}
