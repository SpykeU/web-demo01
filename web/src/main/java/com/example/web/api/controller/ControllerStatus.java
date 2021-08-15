package com.example.web.api.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;


/**
 * 常用异常枚举类
 *
 * @author wangzheng26
 * @date 2019/8/28 18:12
 */
@Getter
public enum ControllerStatus {
    NO_PARAM_ERP(HttpStatus.UNAUTHORIZED, "参数缺少用户信息", "参数缺少用户erp字段"),
    PARAM_WRONG(HttpStatus.BAD_REQUEST, "请求参数错误", "请求参数错误");

    private HttpStatus status;
    @Setter
    private String msg;
    private String desc;

    /**
     * 构造函数
     *
     * @param status
     * @param msg
     * @param desc
     */
    ControllerStatus(HttpStatus status, String msg, String desc) {
        this.status = status;
        this.msg = msg;
        this.desc = desc;
    }
}
