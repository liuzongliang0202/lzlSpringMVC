package com.lzl.springmvc.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lzl.springmvc.annotation.LzlAutowired;
import com.lzl.springmvc.annotation.LzlController;
import com.lzl.springmvc.annotation.LzlRequestMapping;
import com.lzl.springmvc.annotation.LzlRequestParam;
import com.lzl.springmvc.service.OrderService;

@LzlController
@LzlRequestMapping("/lzl")
public class OrderController
{
    @LzlAutowired
    OrderService orderService;
    
    @LzlRequestMapping("/query")
    public void  queryOrder(HttpServletRequest request,HttpServletResponse response,@LzlRequestParam("orderId")String orderId) {
        String order=orderService.queryOrder(orderId);
        PrintWriter writer;
        try
        {
            writer = response.getWriter();
            writer.write(order);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
