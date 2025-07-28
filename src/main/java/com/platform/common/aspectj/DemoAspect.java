package com.platform.common.aspectj;

import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.exception.BaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DemoAspect {

    @Value("${platform.demo:N}")
    private String demo;

    @SneakyThrows
    @Around("execution(* com.platform..*Controller.*(..)) && @annotation(demoRepeat)")
    public Object around(ProceedingJoinPoint joinPoint, DemoRepeat demoRepeat) {
        if (YesOrNoEnum.YES.getCode().equals(demo)) {
            throw new BaseException(demoRepeat.value());
        }
        return joinPoint.proceed();
    }

}
