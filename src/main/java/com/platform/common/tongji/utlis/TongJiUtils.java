package com.platform.common.tongji.utlis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.platform.common.tongji.vo.TongJiVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 统计方法
 */
public class TongJiUtils {

    /**
     * 站点id
     */
    public static String SITE_ID = "SITE_ID";
    /**
     * 刷新TOKEN
     */
    public static String REFRESH_TOKEN = "REFRESH_TOKEN";
    /**
     * 当前TOKEN
     */
    public static String ACCESS_TOKEN = "ACCESS_TOKEN";
    /**
     * ACCESS_KEY
     */
    public static String ACCESS_KEY = "ACCESS_KEY";
    /**
     * SECRET_KEY
     */
    public static String SECRET_KEY = "SECRET_KEY";
    /**
     * 开始时间
     */
    public static String START_DATE = "START_DATE";
    /**
     * 结束时间
     */
    public static String END_DATE = "END_DATE";
    /**
     * 基础url
     */
    private static String BASE_URL = "https://openapi.baidu.com/rest/2.0/tongji/report/getData?access_token=ACCESS_TOKEN&site_id=SITE_ID&method=";

    /**
     * 刷新token
     * 当accessToken变化时候refreshToken也会相应变化
     */
    public static String REFRESH_TOKEN_URL = "http://openapi.baidu.com/oauth/2.0/token?grant_type=refresh_token&refresh_token=REFRESH_TOKEN&client_id=ACCESS_KEY&client_secret=SECRET_KEY";

    /**
     * 今日流量
     */
    public static String OUT_LINE = BASE_URL + "overview/getOutline&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visitor_count,ip_count,bounce_ratio,avg_visit_time,trans_count";

    /**
     * 新老访客
     */
    public static String VISITOR_TYPE = BASE_URL + "overview/getVisitorType&start_date=START_DATE&end_date=END_DATE&metrics=pv_count";

    /**
     * 网站概况（趋势数据）
     */
    public static String TIME_TREND_RPT_URL = BASE_URL + "overview/getTimeTrendRpt&gran=6&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visitor_count,ip_count,bounce_ratio,avg_visit_time,trans_count";

    /**
     * 网站概况（地域分布）
     */
    public static String DISTRICT_RPT_URL = BASE_URL + "overview/getDistrictRpt&start_date=START_DATE&end_date=END_DATE";

    /**
     * 网站概况（来源网站、搜索词、入口页面、受访页面）
     */
    public static String COMMON_TRACK_RPT_URL = BASE_URL + "overview/getCommonTrackRpt&start_date=START_DATE&end_date=END_DATE";

    /**
     * 趋势分析
     */
    public static String TREND_TIME_URL_A = BASE_URL + "trend/time/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visitor_count,ip_count,bounce_ratio,avg_visit_time&order=simple_date_title,desc&area=china";

    /**
     * 实时访客（当前在线）
     */
    public static String TREND_LATEST_URL_F = BASE_URL + "trend/latest/f&metrics=pv_count,visitor_count&area=china";

    /**
     * 实时访客（访问明细）
     */
    public static String TREND_LATEST_URL_A = BASE_URL + "trend/latest/a&metrics=start_time,area,source,access_page,searchword,visitorId,ip,visit_time,visit_pages&order=start_time,desc&max_results=100&area=china";

    /**
     * 全部来源
     */
    public static String SOURCE_ALL_URL = BASE_URL + "source/all/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visitor_count,ip_count,bounce_ratio,avg_visit_time&order=pv_count,desc";

    /**
     * 搜索引擎
     */
    public static String SOURCE_ENGINE_URL = BASE_URL + "source/engine/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visit_count,visitor_count";

    /**
     * 搜索词
     */
    public static String SOURCE_SEARCH_WORD_URL = BASE_URL + "source/searchword/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visit_count,visitor_count";

    /**
     * 外部链接
     */
    public static String SOURCE_LINK_URL = BASE_URL + "source/link/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visit_count,visitor_count";

    /**
     * 受访页面
     */
    public static String VISIT_TOP_PAGE_URL = BASE_URL + "visit/toppage/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visitor_count,outward_count,exit_count,average_stay_time&order=pv_count,desc";

    /**
     * 入口页面
     */
    public static String VISIT_LANDING_PAGE_URL = BASE_URL + "visit/landingpage/a&start_date=START_DATE&end_date=END_DATE&metrics=visit_count,visitor_count";

    /**
     * 受访域名
     */
    public static String VISIT_TOP_DOMAIN_URL = BASE_URL + "visit/topdomain/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visit_count,visitor_count";

    /**
     * 地域分布(按省)
     */
    public static String VISIT_DISTRICT_URL = BASE_URL + "visit/district/a&start_date=START_DATE&end_date=END_DATE&metrics=pv_count,visitor_count,ip_count,bounce_ratio,avg_visit_time";

    /**
     * 类型区分
     */
    public static String TYPE_1 = "1";

    /**
     * 类型区分
     */
    public static String TYPE_2 = "2";

    /**
     * 刷新token
     */
    public static TongJiVo refreshToken(TongJiVo vo) {
        Dict dict = Dict.create()
                .set(REFRESH_TOKEN, vo.getRefreshToken())
                .set(ACCESS_KEY, vo.getAccessKey())
                .set(SECRET_KEY, vo.getSecretKey());
        String result = HttpUtil.get(replaceUrl(REFRESH_TOKEN_URL, dict));
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String accessToken = jsonObject.getStr("access_token");
        String refreshToken = jsonObject.getStr("refresh_token");
        return new TongJiVo().setAccessToken(accessToken).setRefreshToken(refreshToken);
    }

    /**
     * 网站概况（趋势数据）
     */
    public static List<Dict> getTimeTrendRpt(TongJiVo vo, Date startDate, Date endDate) {
        Dict dict = format2(vo, DateUtil.format(startDate, DatePattern.PURE_DATE_PATTERN)
                , DateUtil.format(endDate, DatePattern.PURE_DATE_PATTERN));
        String jsonStr = HttpUtil.get(replaceUrl(TIME_TREND_RPT_URL, dict));
        long between = DateUtil.between(startDate, endDate, DateUnit.DAY);
        JSONObject result = JSONUtil.parseObj(jsonStr).getJSONObject("result");
        JSONArray fields = JSONUtil.parseArray(result.getStr("fields"));
        JSONArray items = JSONUtil.parseArray(result.getStr("items"));
        List<Dict> dataList = new ArrayList<>();
        for (int i = 0; i < between; i++) {
            dataList.add(formatTimeTrendRpt(i, fields, items));
        }
        return dataList;
    }

    /**
     * 格式化趋势图
     */
    private static Dict formatTimeTrendRpt(int index, JSONArray fields, JSONArray items) {
        JSONArray titleList = JSONUtil.parseArray(items).getJSONArray(0);
        JSONArray dataList = JSONUtil.parseArray(items).getJSONArray(1);
        Dict dict = Dict.create()
                .set("title", titleList.getJSONArray(index).getStr(0));
        for (int i = 0; i < 6; i++) {
            String value = dataList.getJSONArray(index).getStr(i);
            dict.put(StrUtil.toCamelCase(fields.getStr(i + 1)), "--".equals(value) ? "0" : value);
        }
        return dict;
    }

    /**
     * 网站概况（地域分布）
     */
    public static String getDistrictRpt(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(DISTRICT_RPT_URL, dict));
    }

    /**
     * 网站概况（来源网站、搜索词、入口页面、受访页面）
     */
    public static String getCommonTrackRpt(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(COMMON_TRACK_RPT_URL, dict));
    }

    /**
     * 趋势分析
     */
    public static Dict getTrendTime(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        String jsonStr = HttpUtil.get(replaceUrl(TREND_TIME_URL_A, dict));
        return getSum(jsonStr, TYPE_2);
    }

    /**
     * 格式化统计数据
     */
    private static Dict formatSum(JSONArray array) {
        return Dict.create()
                .set("pv", formatNum(array.get(0)))
                .set("uv", formatNum(array.get(1)))
                .set("ip", formatNum(array.get(2)))
                .set("bounce", formatNum(array.get(3)))
                .set("avgTime", formatNum(array.get(4)));
    }

    /**
     * 实时访客（当前在线）
     */
    public static Dict getTrendLatestF(TongJiVo vo) {
        Dict dict = format1(vo);
        String jsonStr = HttpUtil.get(replaceUrl(TREND_LATEST_URL_F, dict));
        JSONObject result = JSONUtil.parseObj(jsonStr).getJSONObject("result");
        JSONArray items = result.getJSONArray("items");
        return Dict.create()
                .set("time", DateUtil.date())
                .set("online", result.get("onlineNumber"))
                .set("data", formatTrendLatestF(items));
    }

    /**
     * 格式化实时访客（当前在线）
     */
    private static List<Dict> formatTrendLatestF(JSONArray items) {
        JSONArray items1 = JSONUtil.parseArray(items.get(0));
        JSONArray items2 = JSONUtil.parseArray(items.get(1));
        List<Dict> dataList = new ArrayList<>();
        for (int i = 0; i < items1.size(); i++) {
            JSONArray dataArr = JSONUtil.parseArray(items2.get(i));
            Dict data = Dict.create()
                    .set("name", JSONUtil.parseArray(items1.get(i)).get(0))
                    .set("pv", dataArr.get(0))
                    .set("uv", dataArr.get(1));
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 实时访客（访问明细）
     */
    public static PageInfo getTrendLatestA(TongJiVo vo, Integer pageStart, Integer pageSize) {
        Dict dict = format1(vo);
        String jsonStr = HttpUtil.get(replaceUrl(TREND_LATEST_URL_A, dict));
        JSONObject result = JSONUtil.parseObj(jsonStr).getJSONObject("result");
        Integer total = result.getInt("total");
        JSONArray array = result.getJSONArray("items").getJSONArray(1);
        List<Dict> dataList = new ArrayList<>(pageSize);
        array.forEach(e -> {
            JSONArray fields = JSONUtil.parseArray(e);
            Dict data = Dict.create()
                    .set("datetime", fields.get(0))
                    .set("regional", fields.get(1))
                    .set("source", JSONUtil.parseObj(fields.get(2)).get("fromType"))
                    .set("url", fields.get(3))
                    .set("keyword", fields.get(4))
                    .set("ip", fields.get(5))
                    .set("mark", fields.get(6))
                    .set("duration", fields.get(7))
                    .set("page", fields.get(8));
            dataList.add(data);
        });
        Integer pageEnd = pageStart + pageSize;
        return getPageInfo(CollUtil.sub(dataList, pageStart, pageEnd), total);
    }

    /**
     * 全部来源
     */
    public static Dict getSourceAll(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        String jsonStr = HttpUtil.get(replaceUrl(SOURCE_ALL_URL, dict));
        return getSum(jsonStr, TYPE_1);
    }

    /**
     * 搜索引擎
     */
    public static String getSourceEngine(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(SOURCE_ENGINE_URL, dict));
    }

    /**
     * 搜索词
     */
    public static String getSourceSearchWord(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(SOURCE_SEARCH_WORD_URL, dict));
    }

    /**
     * 外部链接
     */
    public static String getSourceLink(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(SOURCE_LINK_URL, dict));
    }

    protected static Dict getSum(String jsonStr, String type) {
        JSONObject result = JSONUtil.parseObj(jsonStr).getJSONObject("result");
        JSONArray items = JSONUtil.parseArray(result.getStr("items"));
        JSONArray pageSum = JSONUtil.parseArray(JSONUtil.parseArray(result.get("pageSum")).get(0));
        JSONArray dataArray = JSONUtil.parseArray(items.get(1));
        JSONArray titleArray = JSONUtil.parseArray(items.get(0));
        List<Dict> dataList = new ArrayList<>(titleArray.size());
        for (int i = 0; i < titleArray.size(); i++) {
            Dict data = formatSum(JSONUtil.parseArray(dataArray.get(i)));
            if (TYPE_1.equals(type)) {
                data.set("name", JSONUtil.parseArray(titleArray.get(i)).getJSONObject(0).get("name"));
            } else if (TYPE_2.equals(type)) {
                String name = JSONUtil.parseArray(titleArray.get(i)).get(0).toString();
                data.set("name", ReUtil.replaceAll(name, "\\s", ""));
            }
            dataList.add(data);
        }
        return Dict.create()
                .set("sum", formatSum(pageSum))
                .set("items", dataList);
    }

    /**
     * 受访页面
     */
    public static Dict getVisitTop(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        String jsonStr = HttpUtil.get(replaceUrl(VISIT_TOP_PAGE_URL, dict));
        return getSum(jsonStr, TYPE_1);
    }

    /**
     * 入口页面
     */
    public static String getVisitLanding(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(VISIT_LANDING_PAGE_URL, dict));
    }

    /**
     * 受访域名
     */
    public static String getVisitTopDomain(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        return HttpUtil.get(replaceUrl(VISIT_TOP_DOMAIN_URL, dict));
    }

    /**
     * 地域分布(按省)
     */
    public static Dict getVisitDistrict(TongJiVo vo, String startDate, String endDate) {
        Dict dict = format2(vo, startDate, endDate);
        String jsonStr = HttpUtil.get(replaceUrl(VISIT_DISTRICT_URL, dict));
        return getSum(jsonStr, TYPE_1);
    }

    /**
     * 今日流量
     */
    public static Dict getOutline(TongJiVo vo) {
        String startDate = DateUtil.format(DateUtil.yesterday(), DatePattern.PURE_DATE_PATTERN);
        String endDate = DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN);
        Dict dict = format2(vo, startDate, endDate);
        String jsonStr = HttpUtil.get(replaceUrl(OUT_LINE, dict));
        JSONObject result = JSONUtil.parseObj(jsonStr).getJSONObject("result");
        JSONArray fields = JSONUtil.parseArray(result.getStr("fields"));
        JSONArray items = JSONUtil.parseArray(result.getStr("items"));
        return Dict.create()
                .set("yesterday", formatOutline(1, "昨日", fields, items))
                .set("today", formatOutline(0, "今日", fields, items));
    }

    /**
     * 格式化今日流量
     */
    private static Dict formatOutline(int index, String title, JSONArray fields, JSONArray items) {
        JSONArray dataList = items.getJSONArray(index);
        Dict dict = Dict.create().set("title", title);
        for (int i = 1; i < 7; i++) {
            String count = dataList.getStr(i);
            dict.put(StrUtil.toCamelCase(fields.getStr(i)), formatNum(count));
        }
        return dict;
    }

    /**
     * 新老访客
     */
    public static Dict getVisitorType(TongJiVo vo, Date startDate, Date endDate) {
        String start = DateUtil.format(startDate, DatePattern.PURE_DATE_PATTERN);
        String end = DateUtil.format(endDate, DatePattern.PURE_DATE_PATTERN);
        String jsonStr = HttpUtil.get(replaceUrl(VISITOR_TYPE, format2(vo, start, end)));
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr).getJSONObject("result");
        return Dict.create()
                .set("old", formatVisitor(jsonObject, "oldVisitor"))
                .set("new", formatVisitor(jsonObject, "newVisitor"));
    }

    /**
     * 格式化新老访客
     */
    private static Dict formatVisitor(JSONObject jsonObject, String key) {
        Dict visitor = JSONUtil.toBean(jsonObject.getJSONObject(key), Dict.class);
        Dict dict = Dict.create();
        visitor.forEach((x, y) -> {
            dict.put(StrUtil.toCamelCase(x), "--".equals(y) ? 0 : y);
        });
        return dict;
    }

    private static Dict format1(TongJiVo vo) {
        return Dict.create()
                .set(ACCESS_TOKEN, vo.getAccessToken())
                .set(SITE_ID, vo.getSiteId());
    }

    private static Dict format2(TongJiVo vo, String startDate, String endDate) {
        return Dict.create()
                .set(ACCESS_TOKEN, vo.getAccessToken())
                .set(SITE_ID, vo.getSiteId())
                .set(START_DATE, startDate)
                .set(END_DATE, endDate);
    }

    /**
     * 替换url
     */
    protected static String replaceUrl(String url, Dict dict) {
        for (String key : dict.keySet()) {
            url = url.replaceAll(key, dict.getStr(key));
        }
        return url;
    }

    /**
     * 格式化数字
     */
    private static String formatNum(Object obj) {
        String str = obj.toString();
        return str.contains("--") ? "0" : str;
    }

    /**
     * 组装分页对象
     */
    protected static PageInfo getPageInfo(List<?> list, long total) {
        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setTotal(total);
        return pageInfo;
    }
}
