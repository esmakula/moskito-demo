package org.moskito.demo.burgershop.burgershopspring.service;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moskito.testing.junit.runner.MoskitoJunitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Shop service test example with MoskitoJunitRunner
 *
 * @author esmakula
 */
@RunWith(MoskitoJunitRunner.class)
public class ShopServiceTest {

	private static ShopService shopService;

	@BeforeClass
	public static void init(){
		shopService = new ShopServiceImpl();
	}

	@Test
	public void placeOrderTest(){
		String[] itemsToOrderArray = new String[]{"brioche", "rat", "cheese"};
		List itemsToOrder = new ArrayList<>(Arrays.asList(itemsToOrderArray));
		Order order = shopService.placeOrder(itemsToOrderArray);
		int orderPrice = 0;
		for (ShopableItem item: order.getItems()) {
			assertTrue(itemsToOrder.contains(item.getName()));
			orderPrice += item.getPrice();
		}
		assertEquals(orderPrice, order.getTotalPrice());
	}

}
