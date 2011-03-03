package com.medx.test.model.order;

import com.medx.framework.annotation.ModelClass;
import com.medx.test.model.product.Product;

@ModelClass
public interface OrderItem {
	Product getProduct();
	void setProduct(Product product);
	
	Integer getQuantity();
	void setQuantity(Integer quantity);
}
