package com.lzl.springmvc.service.impl;

import com.lzl.springmvc.annotation.LzlService;
import com.lzl.springmvc.domain.Order;
import com.lzl.springmvc.service.OrderService;
@LzlService
public class OrderServiceImpl implements OrderService
{

    @Override
    public String queryOrder(String id)
    {
        Order order=new Order();
        order.setOrderId(id);
        order.setAddress("杭州市文二西路");
        order.setCount(5);
        order.setOrderName("大麻花");
        order.setPrice("100");
        return order.toString();
    }
    
}
