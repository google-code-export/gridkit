package com.medx.test.model.order;

import com.medx.framework.annotation.DictType;
import com.medx.test.model.product.Product;

@DictType
public interface OrderItem {
	Product getProduct();
	void setProduct(Product product);
	
	Integer getQuantity();
	void getQuantity(Integer quantity);
}
