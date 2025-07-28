package com.platform.common.redis;

import lombok.Data;
import org.springframework.data.geo.Point;

import java.io.Serializable;

@Data
public class GeoVo implements Serializable {

    /**
     * 城市名称
     */
    private String name;
    /**
     * 城市坐标
     */
    private Point point;

}
