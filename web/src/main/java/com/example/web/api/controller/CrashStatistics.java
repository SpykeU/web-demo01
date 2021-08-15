package com.example.web.api.controller;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
public class CrashStatistics {

    @NotNull(message = "months不能为空")
    @NotBlank(message = "months不能为空")
    private String months;

    @NotNull(message = "systemCode不能为空")
    @NotBlank(message = "systemCode不能为空")
    private String systemCode;
}
