package org.moskito.demo.burgershop.burgershopspring.service;

import org.moskito.demo.burgershop.burgershopspring.service.stats.SalesStats;
import org.moskito.demo.burgershop.burgershopspring.service.stats.SalesStatsFactory;
import org.moskito.demo.burgershop.burgershopspring.service.stats.ThresholdProducer;
import net.anotheria.moskito.aop.annotation.Monitor;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducer;
import net.anotheria.moskito.core.dynamic.OnDemandStatsProducerException;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the ShopService.
 *
 * @author lrosenberg
 * @since 16.11.13 22:47
 */
@Monitor(producerId="ShopService")
public class ShopServiceImpl implements ShopService {

	private LinkedList<ShopableItem> items;

	private static Logger log = LoggerFactory.getLogger(ShopServiceImpl.class);

	//step 2 -> counters
	private OrderCounter counter = new OrderCounter();
	private IngredientCounter ingredientCounter = new IngredientCounter();

	//step 3 - own producer
	private OnDemandStatsProducer<SalesStats> salesProducer;

	public ShopServiceImpl(){
		items = new LinkedList<ShopableItem>();
		items.add(new ShopableItem("wheat", 585, Category.BREAD));
		items.add(new ShopableItem("wholemeal", 285, Category.BREAD));
		items.add(new ShopableItem("brioche", 585, Category.BREAD));
		items.add(new ShopableItem("burned", 585, Category.BREAD));
		items.add(new ShopableItem("leibniz", 1085, Category.BREAD));

		items.add(new ShopableItem("cow", 1385, Category.MEAT));
		items.add(new ShopableItem("pork", 1185, Category.MEAT));
		items.add(new ShopableItem("lamb", 1584, Category.MEAT));
		items.add(new ShopableItem("dog", 585, Category.MEAT));
		items.add(new ShopableItem("rat", 10, Category.MEAT));

		items.add(new ShopableItem("mushrooms", 285, Category.EXTRAS));
		items.add(new ShopableItem("broccoli", 185, Category.EXTRAS));
		items.add(new ShopableItem("cheese", 85, Category.EXTRAS));
		items.add(new ShopableItem("sauce", 85, Category.EXTRAS));
		items.add(new ShopableItem("cockroach", 2085, Category.EXTRAS));

		salesProducer = new OnDemandStatsProducer<SalesStats>("sales", "business", "sales", new SalesStatsFactory());
		ProducerRegistryFactory.getProducerRegistryInstance().registerProducer(salesProducer);

		new ThresholdProducer();
	}

	@Override
	public List<ShopableItem> getShopableItems() {
		return items;
	}

	@Override
	public Order placeOrder(String... items) {
		//first find the order

		if (items==null)
			throw new IllegalArgumentException("No items for order");

		Order order = new Order();
		for (String item : items){
			order.addItem(findItemByName(item));
			ingredientCounter.ingredientUsed(item);
		}


		counter.orderPlaced();

		//now add sales counters...
		int priceInCents = order.getTotalPrice();
		salesProducer.getDefaultStats().addSale(priceInCents);
		for (String item : items){
			try{
				salesProducer.getStats(item).addSale(priceInCents);
			}catch(OnDemandStatsProducerException e){
				log.warn("Couldn't mark items as sold because we obviously sell more items than producer limit" , e);
			}
		}


		return order;

	}

	private ShopableItem findItemByName(String name){
		for (ShopableItem item : items){
			if (item.getName().equals(name))
				return item;
		}
		throw new IllegalArgumentException("No such shopable item: "+name);
	}
}
