package com.ericxxt.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Description: 提供手动获取被spring管理的bean对象
 */
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(this.applicationContext==null){
            this.applicationContext=applicationContext;
        }
    }

    // acquire the applicationcontext
    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    // get the bean by name
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }
    //get the bean by class
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //get the bean by name and class
    public static <T> T getBean(String name,Class<T> clazz){
        return getApplicationContext().getBean(name,clazz);
    }
}
