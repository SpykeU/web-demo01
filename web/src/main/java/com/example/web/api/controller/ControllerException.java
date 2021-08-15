package com.example.web.api.controller;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 返回到controller的异常
 *
 * @author wangzheng26
 * @date 2019/8/28 18:12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ControllerException extends RuntimeException {
    /**
     * 异常类型
     */
    private ControllerStatus controllerStatus;
    /**
     * 异常信息
     */
    private String exceptionInfo;

//    public ControllerException(ControllerStatus controllerStatus) {
//        this.controllerStatus = controllerStatus;
//    }

    /**
     * 参数异常
     *
     * @param exceptionInfo
     */
    public ControllerException(String exceptionInfo) {
        controllerStatus = ControllerStatus.PARAM_WRONG;
        this.exceptionInfo = exceptionInfo;
    }

//    public static ControllerException of(ControllerStatus controllerStatus) {
//        return new ControllerException(controllerStatus);
//    }

    /**
     * @param exceptionInfo
     * @return
     */
    public static ControllerException ofEexception(String exceptionInfo) {
        return new ControllerException(exceptionInfo);
    }

    @Override
    public String getMessage() {
        return exceptionInfo;
    }

}
