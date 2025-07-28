package com.platform.common.utils.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 过滤工具类
 */
public class FilterUtils {

    public static WordTree WORD_TREE = new WordTree();

    private static String STAR_STR = "********************************";

    /**
     * 空参构造
     */
    public FilterUtils() {
        String path = "config/filter_rules.json";
        Resource resource = ResourceUtil.getResourceObj(path);
        String json = resource.readStr(Charset.defaultCharset());
        JSONArray jsonArray = JSONUtil.parseArray(json);
        List<FilterVo> dataList = JSONUtil.toList(jsonArray, FilterVo.class);
        dataList.forEach(data -> {
            if (data.isEnabled()) {
                List<String> keywords = data.getKeywords();
                if (!CollectionUtils.isEmpty(keywords)) {
                    WORD_TREE.addWords(keywords);
                }
            }
        });
    }

    /**
     * 过滤
     */
    public static String filter(String content, boolean activate) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        if (!activate) {
            return content;
        }
        Singleton.get(FilterUtils.class);
        List<String> matchAll = WORD_TREE.matchAll(content, -1, true, true);
        CollUtil.sort(matchAll, (o1, o2) -> o2.length() - o1.length());
        for (String match : matchAll) {
            content = StrUtil.replace(content, match, StrUtil.sub(STAR_STR, 0, match.length()));
        }
        return content;
    }

}
