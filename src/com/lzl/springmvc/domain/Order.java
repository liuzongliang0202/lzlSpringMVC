package com.lzl.springmvc.domain;

public class Order
{
    private String orderId;
    private String orderName;
    private String price;
    private Integer count;
    private String address;
    public String getOrderId()
    {
        return orderId;
    }
    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }
    public String getOrderName()
    {
        return orderName;
    }
    public void setOrderName(String orderName)
    {
        this.orderName = orderName;
    }
    public String getPrice()
    {
        return price;
    }
    public void setPrice(String price)
    {
        this.price = price;
    }
    public Integer getCount()
    {
        return count;
    }
    public void setCount(Integer count)
    {
        this.count = count;
    }
    public String getAddress()
    {
        return address;
    }
    public void setAddress(String address)
    {
        this.address = address;
    }
    @Override
    public String toString()
    {
        return "Order [orderId=" + orderId + ", orderName=" + orderName + ", price=" + price + ", count=" + count
            + ", address=" + address + "]";
    }
}
