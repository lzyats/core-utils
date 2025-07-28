package com.platform.common.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.platform.common.enums.YesOrNoEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 交易工具类
 */
public class TradeUtils {

    /**
     * 拆单
     */
    public static List<String> splitAmount(Integer total, Integer count, YesOrNoEnum normal) {
        // 普通红包
        if (YesOrNoEnum.YES.equals(normal)) {
            return splitNormalAmount(total, count);
        }
        // 手气红包
        return splitRandomAmount(total, count);
    }

    /**
     * 拆单
     */
    public static List<String> splitNormalAmount(Integer total, Integer count) {
        List<String> dataList = new ArrayList<>();
        // 普通红包
        BigDecimal data = NumberUtil.div(total, count);
        for (int i = 0; i < count; i++) {
            dataList.add(NumberUtil.toStr(data));
        }
        return dataList;
    }


    /**
     * 拆单
     */
    public static List<String> splitRandomAmount(Integer total, Integer count) {
        // 拆分结果
        List<String> dataList = new ArrayList<>();
        // 最小红包
        Integer min = 1;
        // 平均红包
        Integer avg = total / count;
        // 剩余红包
        Integer balance = total;
        // 随机分配第1次
        for (int i = 0; i < count; i++) {
            // 计算红包
            Integer money = RandomUtil.randomInt(0, avg) + min;
            // 红包+最小红包
            dataList.add(NumberUtil.toStr(money));
            // 扣减余额
            balance = balance - money;
        }
        // 随机分配第2次
        while (balance > avg) {
            // 随机数
            Integer random = RandomUtil.randomInt(0, count);
            // 随机
            dataList.set(random, NumberUtil.toStr(avg + Integer.parseInt(dataList.get(random))));
            // 扣减余额
            balance = balance - avg;
        }
        // 随机分配第3次
        if (balance > 0) {
            // 随机数
            Integer random = RandomUtil.randomInt(0, count);
            // 随机
            dataList.set(random, NumberUtil.toStr(balance + Integer.parseInt(dataList.get(random))));
        }
        // 随机排序
        Collections.shuffle(dataList);
        return dataList;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            List<String> dataList = splitRandomAmount(100, 5);
            Console.log(dataList);
        }
    }

}
