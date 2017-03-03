import com.vindicia.client.BillingPlan;
import com.vindicia.client.VindiciaReturnException;
import com.vindicia.soap.v20_0.Vindicia.BillingPeriodType;
import com.vindicia.soap.v20_0.Vindicia.BillingPlanPeriod;
import com.vindicia.soap.v20_0.Vindicia.BillingPlanPrice;

/**
 * A service to interact with cashbox billing plans
 * @author mnaeini
 *
 */
public class BillingPlanServiceImpl implements VindiciaService {
	
	/**
	 *TODO: A lot of hardcoded to create a billing plan that needs to be fixed if we want to use this in production
	 */	
	public void createBilingPlan(String planId) {
		
		BillingPlan monthlyWithFreeTrialMonthPlan = new BillingPlan();
		
		monthlyWithFreeTrialMonthPlan.setMerchantBillingPlanId(planId); 
		monthlyWithFreeTrialMonthPlan.setDescription("Monthly recurring with free first month"); // Optional

		// The plan contains 2 periods, first period is free and lasts for 1 month
		BillingPlanPeriod freePeriod = new BillingPlanPeriod();
		
		freePeriod.setFree(true);
		// we want 1 cycle of 1 monthly
		freePeriod.setType(BillingPeriodType.Month);
		freePeriod.setQuantity(1); // a cycle is 1 Month
		freePeriod.setCycles(1); // 1 free cycle*/
		
		// second period is a paid period that contains infinite monthly cycles
		BillingPlanPeriod paidPeriod= new BillingPlanPeriod();

		paidPeriod.setType(BillingPeriodType.Month);
		paidPeriod.setQuantity(1); // a cycle is 1 month
		paidPeriod.setCycles(0); // indicates infinite number of cycles
		
		// define prices in various currencies
		// If your prices are going to be specified on Product objects only, you still have to set a price of 0 here
		BillingPlanPrice priceUSD = new BillingPlanPrice();
		priceUSD.setAmount(new java.math.BigDecimal(10.00));
		priceUSD.setCurrency("USD");

		BillingPlanPrice priceCAD = new BillingPlanPrice();
		priceCAD.setAmount(new java.math.BigDecimal(12.00));
		priceCAD.setCurrency("CAD");
		
		paidPeriod.setPrices(new BillingPlanPrice[] { priceUSD, priceCAD });
		
		monthlyWithFreeTrialMonthPlan.setPeriods(new BillingPlanPeriod[]{freePeriod, paidPeriod});
		// Now make the CashBox SOAP API call to create the billing plan in the Vindicia system
		try {
			boolean created = monthlyWithFreeTrialMonthPlan.update(null);
			if (created) {
				System.out.println("Billing plan created with Merchant Id " + monthlyWithFreeTrialMonthPlan.getMerchantBillingPlanId());
				System.out.println("Successfully created. Vindicia assigned ID is " + monthlyWithFreeTrialMonthPlan.getVID());
			}
			else {
				// The plan already existed in CashBox.
				// We should never reach here if the merchantBillingPlanId used above was all new and unique
				System.out.println("Billing plan updated with Merchant Id " + monthlyWithFreeTrialMonthPlan.getMerchantBillingPlanId());
				System.out.println("Billing plan updated with Vindicia assigned ID is " + monthlyWithFreeTrialMonthPlan.getVID());
			}
		}
		catch (VindiciaReturnException vre) {
					
			System.out.println("Billing plan creation failed, return code: " + vre.getReturnCode() + " return string: '" + vre.getMessage() + "'" + " Soap id: " + vre.getSoapId());
				
		}
		catch (Exception e) {
				System.out.println("Billing plan creation failed" );
				e.printStackTrace();				
		}
	} 

	public static void main(String[] args) {
		VindiciaClient.getInstance();
		BillingPlanServiceImpl factory = new BillingPlanServiceImpl();
		factory.createBilingPlan("test_product_billing3");
	}
}
