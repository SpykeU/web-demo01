package com.example.web.api.controller;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

@Getter
@Setter
public class CrashAppStats {
    @NotEmpty(message = "client不能为空")
    private String client;

    @NotEmpty(message = "months不能为空")
    @Min(value = 1, message = "months最小为1")
    @Max(value = 12, message = "months最大为12")
    private String months;
}
