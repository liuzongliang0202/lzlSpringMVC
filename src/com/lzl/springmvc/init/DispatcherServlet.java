package com.lzl.springmvc.init;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lzl.springmvc.annotation.LzlAutowired;
import com.lzl.springmvc.annotation.LzlController;
import com.lzl.springmvc.annotation.LzlRequestMapping;
import com.lzl.springmvc.annotation.LzlRequestParam;
import com.lzl.springmvc.annotation.LzlService;

public class DispatcherServlet extends HttpServlet
{
    /**
     * IOC容器，存放实例bean
     */
    private Map<String, Object> beans=new HashMap<>();
    /**
     * @RequestMapping和@Autowired的绑定关系
     */
    private Map<String, Method> handlerMap=new HashMap<>();
    /**
     * URL和controller的绑定关系,这里的object对象必须是IOC容器的对象，必须是单实例的
     */
    private Map<String, Object> controllerMap=new HashMap<>();
    /**
     * 项目中的所有类
     */
    private List<String> classNames=new ArrayList<>();
    private String basePackage;
    
    @Override
    public void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);
        doInit(config);
        doScan(basePackage);
        doInstance();
        doHandler();
    }
    
    /**
     * 
     * 加载配置
     *
     * @author lzl
     * @param config
     */
    public void doInit(ServletConfig config) {
        basePackage=config.getInitParameter("contextConfigLocation");
    }
    
    /**
     * 
     * 扫描包中的所有类
     *
     * @author lzl
     * @param location
     */
    public void doScan(String location) {
        String path="/"+location.replaceAll("\\.", "/");
        //这里需要优化，觉得可以用相对路径来取
        File dir=new File("D:\\SpaceOfSTS\\lzlSpringMVC\\src"+path);
        for(File file:dir.listFiles()) {
            if(file.isDirectory()) {
                doScan(location+"."+file.getName());
            }else {
                String className=location+"."+file.getName().replace(".java", "");
                classNames.add(className);
            }
        }
    }
    
    /**
     * 
     * 实例化所有的Javabean
     *
     * @author lzl
     */
    public void doInstance() {
        if(classNames.isEmpty()) {
            return;
        }
        for(String className:classNames) {
            try
            {
                Class<?> clazz=Class.forName(className);
                if(clazz.isAnnotationPresent(LzlController.class)) {
                    Object object=clazz.newInstance();
                    beans.put(toLowerFirstWord(clazz.getSimpleName()), object);
                }else if(clazz.isAnnotationPresent(LzlService.class)) {
                    Object object=clazz.newInstance();
                    beans.put(toLowerFirstWord(clazz.getSimpleName()), object);
                }else {
                    continue;
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }
        }
    }
    
    /**
     * 
     * 绑定url和对应的方法
     *
     * @author lzl
     */
    public void doHandler() {
        String classPath="";
        String methodPatrh="";
        if(beans.isEmpty()) {
            return;
        }
        for(Entry<String, Object> entry:beans.entrySet()) {
            Object object=entry.getValue();
            Class<?> clazz=object.getClass();
            if(!clazz.isAnnotationPresent(LzlController.class)) {
                continue;
            }
            //OrderService属性的注入，遇到的坑
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields) {
                if(field.isAnnotationPresent(LzlAutowired.class)) {
                    field.setAccessible(true);
                    String value=field.getAnnotation(LzlAutowired.class).value();
                    if(value==null||value.length()==0) {
                        value=field.getName()+"Impl";
                    }
                    try
                    {
                        field.set(object, beans.get(value));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
            if(clazz.isAnnotationPresent(LzlRequestMapping.class)) {
                classPath=clazz.getAnnotation(LzlRequestMapping.class).value();
            }
            Method[] methods=clazz.getMethods();
            for(Method method:methods) {
                if(!method.isAnnotationPresent(LzlRequestMapping.class)) {
                    continue;
                }
                methodPatrh=method.getAnnotation(LzlRequestMapping.class).value();
                handlerMap.put(classPath+methodPatrh, method);
                try
                {
                    controllerMap.put(classPath+methodPatrh, object);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String url=req.getRequestURI();
        String contextPath=req.getContextPath();
        url=url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.handlerMap.containsKey(url)){
            try
            {
                resp.getWriter().write("404 NOT FOUND!");
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        Method method=handlerMap.get(url);
        Class<?>[] paramTypes=method.getParameterTypes();
        Parameter[] parameters=method.getParameters();
        int count=parameters.length;
        Object[] params=new Object[count];
        for(int i=0;i<count;i++) {
            if(paramTypes[i].getSimpleName().equalsIgnoreCase("HttpServletRequest")) {
                params[i]=req;
                continue;
            }
            if(paramTypes[i].getSimpleName().equalsIgnoreCase("HttpServletResponse")) {
                params[i]=resp;
                continue;
            }
            if(parameters[i].isAnnotationPresent(LzlRequestParam.class)) {
                String value=parameters[i].getAnnotation(LzlRequestParam.class).value();
                params[i]=req.getParameter(value);
                continue;
            }
        }
        try
        {
            method.invoke(this.controllerMap.get(url),params);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * 把首字母改为小写
     *
     * @author lzl
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name){
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}
