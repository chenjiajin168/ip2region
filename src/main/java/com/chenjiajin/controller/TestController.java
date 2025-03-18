package com.chenjiajin.controller;

import com.chenjiajin.service.impl.TestService;
import com.chenjiajin.utils.Ip2regionUtil;
import com.chenjiajin.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TestController {


    @Autowired
    private TestService service;

    @RequestMapping("/aop")
    public String aop(String test) {
        System.out.println("这是执行方法");
        return "success";
    }

    // http://localhost/demo
    @RequestMapping("/demo")
    public String demo(HttpServletRequest request) {
        String ipAddress = RequestUtil.getIPAddressByRequest(request);
        System.err.println("请求ip: " + ipAddress);

        String homeLocation = Ip2regionUtil.getIpRegionByCacheDetail(ipAddress);
        System.err.println("请求ip归属地: " + homeLocation);

        System.err.println("请求ip归属地: " + Ip2regionUtil.getIpRegionByCacheDetail("116.21.13.145"));

        return "demo";
    }


}