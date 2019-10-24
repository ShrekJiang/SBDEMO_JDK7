package com.xqj.idea_sbdemo_jdk7.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import static com.xqj.idea_sbdemo_jdk7.utils.OSUtils.*;


@Controller
@RequestMapping(value = {"/hello"})
public class HelloController {

    @Value("${projectName}")
    private String projectName;

    @RequestMapping(value = {"/hello"}, method = RequestMethod.GET)
    public String hello() {
        return "aaa";
    }

    @RequestMapping(value = {"/info"})
    public String testBootstrap(Map<String, Object> paramMap) {//explain 默认Map的内容会放大请求域中，页面可以直接取值*/
        paramMap.put("projectName", projectName);
        //paramMap.put("projectName","正常");
        return "index";
    }

    @RequestMapping(value = {"/testHtmlRefresh"})
    public String testHtmlRefresh(Model model) {
        Map<String, String> cpuResult = cpuUsage();
        Map<String, String> memResult = memoryUsage();
        Map<String, String> networkResult = networkUsage();
        List<String> jvmInfoList = jvmInfo();
        List<Map<String, String>> diskInfoList = diskInfo();
        List<Map<String, String>> diskIOInfoList = diskIOInfo2();


        model.addAttribute("cpuResult", cpuResult.get("result"));
        model.addAttribute("memResult", memResult.get("result"));
        model.addAttribute("networkResult", networkResult.get("result"));
        model.addAttribute("jvmInfoList", jvmInfoList);
        model.addAttribute("diskInfoList", diskInfoList);
        model.addAttribute("diskIOInfoList", diskIOInfoList);
        // model.addAttribute("networkResult", new Date());
        return "index::div_systemInfo";
    }

}