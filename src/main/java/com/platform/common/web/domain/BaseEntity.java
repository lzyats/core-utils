package com.platform.common.web.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.platform.common.enums.TimeUnitEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Entity基类
 */
@Data
@Accessors(chain = true) // 链式调用
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始时间
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private Date beginTime;

    /**
     * 结束时间
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private Date endTime;

    /**
     * 时间单位
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private TimeUnitEnum timeUnit;

    /**
     * 请求参数
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String param;

    /**
     * 请求参数
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, Object> params;

    /**
     * 返回参数
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String label;

    /**
     * 返回参数
     */
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long count;

}
