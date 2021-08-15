package com.example.web.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/crash")
@Slf4j
public class ConsoleController {


    @RequestMapping(value = "/index", produces = "application/json")
    public String index() {
        return "Hello World!";
    }

    /**
     * 崩溃app信息统计
     *
     * @param crashStatistics
     * @return
     */
    @GetMapping(value = "statistics", produces = {"application/json;charset=UTF-8"})
    public Object statistics(@Valid CrashStatistics crashStatistics, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw ControllerException.ofEexception(bindingResult.getFieldError().getDefaultMessage());
        }

        JSONObject dataObject = JSON.parseObject(JSON.toJSONString(crashStatistics));

        return dataObject;
    }
}
