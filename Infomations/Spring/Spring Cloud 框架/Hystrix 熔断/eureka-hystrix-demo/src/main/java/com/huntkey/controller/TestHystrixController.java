package com.huntkey.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 熔断降级测试
 * Created by liuwens on 2017/8/18.
 */
@RestController
public class TestHystrixController
{
    @RequestMapping("/test")
    @HystrixCommand(fallbackMethod = "testFallbackMethod2", threadPoolProperties = {
            @HystrixProperty(name = "coreSize", value = "30"), @HystrixProperty(name = "maxQueueSize", value = "100"),
            @HystrixProperty(name = "queueSizeRejectionThreshold", value = "20") }, commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "100"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "50")
    })
    public String test()
    {
        //生成随机超时时间，触发熔断机制, 执行降级处理
        int l = new Random().nextInt(200);
        try
        {
            TimeUnit.MILLISECONDS.sleep(l);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return "ok";
    }

    /**
     * 请求超时后，降级处理的方法
     * @return
     */
    public String testFallbackMethod()
    {
        return "My is fallbackMethod";
    }

    /**
     * 请求超时后，降级处理的方法
     * @return
     */
    public String testFallbackMethod2()
    {
        return "My is fallbackMethod 2";
    }
}
