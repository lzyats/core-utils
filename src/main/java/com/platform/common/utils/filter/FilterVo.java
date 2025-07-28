package com.platform.common.utils.filter;

import lombok.Data;

import java.util.List;

/**
 * 过滤工具类
 */
@Data
public class FilterVo {

    /**
     * 分类
     */
    private String category;
    /**
     * 开关
     */
    private boolean enabled;
    /**
     * 关键词
     */
    private List<String> keywords;

}
