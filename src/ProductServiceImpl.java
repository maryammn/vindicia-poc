import com.vindicia.client.*;
import com.vindicia.soap.v20_0.Vindicia.DuplicateBehavior;
import com.vindicia.soap.v20_0.Vindicia.MerchantEntitlementId;
import com.vindicia.soap.v20_0.Vindicia.ProductDescription;

/**
 * A service that allows you to interact with vincidica products
 * @author mnaeini
 *
 */
public class ProductServiceImpl implements VindiciaService {
	
	
	/**
	 * Create a product given the product code
	 * @param productId
	 * @param billingPlanId
	 * @return
	 */
	public Product createProduct(String productId, String billingPlanId) {
		// Use a default entitlement when creating this product
		com.vindicia.soap.v20_0.Vindicia.MerchantEntitlementId entitlementId = new com.vindicia.soap.v20_0.Vindicia.MerchantEntitlementId();
		entitlementId.setId("MonthlyAccess");
		entitlementId.setDescription("Customer gets access to regular monthly subscription");
		return createProduct(productId, billingPlanId, entitlementId);
	}
	
	public Product createProduct(String productId, String billingPlanId, MerchantEntitlementId entitlementId) { 

		Product product = new Product();
		
		product.setMerchantProductId(productId);  // specify a unique product ID i.e. SKU
		
		// Product description is mandatory - this becomes line item description in the transaction processed
		// when a customer is billed for this Product
		com.vindicia.soap.v20_0.Vindicia.ProductDescription descr = new com.vindicia.soap.v20_0.Vindicia.ProductDescription();
		descr.setLanguage("EN"); // ISO language code
		descr.setDescription("Monthly service");
		
		product.setDescriptions(new ProductDescription[] {descr});
		
		// Specify Product's price in various currencies
		// If you are going to specify prices exclusively on BillingPlans, you should specify price 0
		com.vindicia.soap.v20_0.Vindicia.ProductPrice priceUSD = new com.vindicia.soap.v20_0.Vindicia.ProductPrice();
		priceUSD.setAmount(new java.math.BigDecimal(20.00));
		priceUSD.setCurrency("USD");
		
		// Specify Product's price in various currencies
		com.vindicia.soap.v20_0.Vindicia.ProductPrice priceCAD = new com.vindicia.soap.v20_0.Vindicia.ProductPrice();
		priceCAD.setAmount(new java.math.BigDecimal(22.00));
		priceCAD.setCurrency("CAD");
		
		product.setPrices(new com.vindicia.soap.v20_0.Vindicia.ProductPrice[] { priceUSD, priceCAD});
		
		
		product.setMerchantEntitlementIds(new MerchantEntitlementId[] {entitlementId} );
		
		// Set status to active - optional - this is for information purposes only
		product.setStatus(com.vindicia.soap.v20_0.Vindicia.ProductStatus.Active);
		
		// Assign a billing plan to product
		BillingPlan plan = new BillingPlan();
		plan.setMerchantBillingPlanId(billingPlanId);
		product.setDefaultBillingPlan(plan);
		try {
			boolean created = product.update(null, DuplicateBehavior.Fail); //duplicateBehavior input parameter is no op - hence set to null
			// if we are here, we got a 200-OK response from the server
			if (created) {
				System.out.println("Successfully created product, Merchant Id " + product.getMerchantProductId());
				System.out.println("Successfully created product, Vindicia assigned Product ID " + product.getVID());
			}
			else {
				// This product already existed in CashBox and was only updated by this call
				System.out.println("Successfully updated product, Merchant Id " + product.getMerchantProductId());
				System.out.println("Successfully updated product, Vindicia assigned Product ID " + product.getVID());
			}
		}

		catch (VindiciaReturnException vre) {
			// VindiciaReturnException indicates a non-200 response code from the server
			
			System.out.println("Product creation/update failed, return code: " + vre.getReturnCode() + " return string: '" + vre.getMessage() + "'" + " Soap id: " + vre.getSoapId());
			
		}
		catch (Exception e) {
			// Handle more serious exception such as timeout or network drop here
			System.out.println("Product creation/update failed" );
			e.printStackTrace();
		}
	return product;
	}
	
	/**
	 * 
	 * @return all the products currently in the system
	 */
	public Product[] fetchAllProducts() {
		com.vindicia.client.Product[] prods = null;
		int page = 0;
		int pageSize = 10; // adjust based on how many products you have, too large of a page size may cause time outs
		boolean hasMore = true;
		int totalProductCount = 0;
		
		// Loop through the pages until the server has no more data to return
		
		while (hasMore) {
			try {
				prods = com.vindicia.client.Product.fetchAll(null, page, pageSize);
				// if we are here we did not get a bad (non-200) response code
				if (prods != null) {
					for (int j=0; j<prods.length; j++) {
						
						// process fetched Products here
						// In this sample we will simply print Product's ID and description to the command line
						
						System.out.print("Product ID: " + prods[j].getMerchantProductId());
						if (prods[j].getDescriptions() != null) {
							
							System.out.println(", Description: " + prods[j].getDescriptions(0).getDescription());
						}
						totalProductCount++;
						
					}
					page++;
				}
				else {
					// we should never reach here since no products means 404 return code and a VindiciaReturnException
					// but to be full proof
					System.out.print("No products returned on page " + page);
					hasMore = false;
					break;
					
				}
			}
			catch (VindiciaReturnException vre) {
				if (vre.getReturnCode().equals("404")) {
					// no more pages;
					System.out.print("No products returned on page " + page);
					hasMore = false;
					break;
				}
			}
			catch (Exception e) {
				// a more serious exception such as time out or network drop out
				System.out.println("Product fetch failed on page " + page );
				e.printStackTrace();
				hasMore = false;
				break;
				
			}
					
		} // end while
		System.out.println("\n\nFetched total " + totalProductCount + " products");
		return prods;
	}
	
	public static void main(String[] args) { 
		VindiciaClient.getInstance();
		ProductServiceImpl prodService = new ProductServiceImpl();
		//fetchAllProducts();
		String productCode = "test_product";
		String billingPlanId = "test_product_billing_plan";
		
		// If you are using CashBox's entitlement management ...
		// Create ID meaningful to you in your application describing customer's entitlement when he/she purchases this product
		com.vindicia.soap.v20_0.Vindicia.MerchantEntitlementId entitlementId = new com.vindicia.soap.v20_0.Vindicia.MerchantEntitlementId();
		entitlementId.setId("MonthlyAccess");
		entitlementId.setDescription("Customer gets access to regular monthly subscription");
		
		prodService.fetchAllProducts();
		prodService.createProduct(productCode, billingPlanId, entitlementId);
		
	}
}
