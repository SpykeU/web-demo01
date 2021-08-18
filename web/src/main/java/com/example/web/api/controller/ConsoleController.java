package com.example.web.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/crash")
@Slf4j
public class ConsoleController {
    @RequestMapping(value = "/index", produces = "application/json")
    public Object index() {
        Map<String, Object> result = new HashMap<>(8);
        result.put("test", 1);
        return result;
    }

    /**
     * 崩溃app信息统计
     *
     * @param crashAppStats
     */
    @RequestMapping(value = "/app/getCrashAppStatistics", produces = {"application/json;charset=UTF-8"})
    public Object getCrashAppStatistics(@Valid CrashAppStats crashAppStats, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return bindingResult.getAllErrors().get(0).getDefaultMessage();
        }
        App app = new App();
        Object stats = app.getCrashAppStatistics(crashAppStats);
        JSONObject dataObject = JSON.parseObject(JSON.toJSONString(stats));
        return dataObject;
    }
}
