package com.platform.common.web.handler;

import com.platform.common.aspectj.VersionRepeat;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class VersionHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestCondition<VersionHandlerCondition> getCustomMethodCondition(Method method) {
        VersionRepeat versionRepeat = AnnotationUtils.findAnnotation(method, VersionRepeat.class);
        return createCondition(versionRepeat);
    }

    // 实例化RequestCondition
    private RequestCondition<VersionHandlerCondition> createCondition(VersionRepeat versionRepeat) {
        if (versionRepeat == null) {
            return null;
        }
        return new VersionHandlerCondition(versionRepeat.value().getCode());
    }

}
