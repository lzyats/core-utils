package com.platform.common.web.vo;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true) // 链式调用
public class LabelVo {

    /**
     * 标签
     */
    private String label;

    /**
     * 数据
     */
    private String value;

    public LabelVo(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public LabelVo(String label, Number value) {
        this.label = label;
        this.value = NumberUtil.toStr(value);
    }
}
