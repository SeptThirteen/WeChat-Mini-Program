package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.dto.BillQueryRequest;
import com.example.elderly.entity.BillQuery;
import com.example.elderly.mapper.BillQueryMapper;
import com.example.elderly.service.BillQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 账单查询服务。
 * 当前仍为模拟数据：未对接真实公用事业公司接口（需要专有协议/授权）。
 * 相比早期固定返回 ¥128.50，现在按 用户+户号+账单月 生成确定性的明细化账单，
 * 同一输入多次查询结果一致，贴近真实系统行为；接入真实数据源时仅需替换本类实现。
 */
@Service
@RequiredArgsConstructor
public class BillQueryServiceImpl implements BillQueryService {

    private final BillQueryMapper billQueryMapper;

    @Override
    public Map<String, Object> createQuery(BillQueryRequest request) {
        String type = request.getQueryType();
        String typeName = switch (type) {
            case "ELECTRICITY" -> "电费";
            case "WATER" -> "水费";
            case "TV" -> "有线电视";
            default -> type;
        };
        String account = request.getQueryParams() == null ? "" : request.getQueryParams().trim();
        String month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String dueDate = YearMonth.now().plusMonths(1).atDay(15).toString();

        // 同一 用户+户号+月份 使用固定随机种子，保证账单金额可复现
        long seed = Math.abs(String.format("%s|%s|%s|%s",
                request.getUserId(), type, account, month).hashCode());
        Random random = new Random(seed);

        List<Map<String, Object>> items = new ArrayList<>();
        double total;
        switch (type) {
            case "ELECTRICITY" -> {
                int usage = 80 + random.nextInt(241); // 80~320 度
                double energy = round2(usage * 0.55);
                items.add(item("电费（" + usage + " 度 × 0.55 元/度）", energy));
                items.add(item("供电服务费", 8.00));
                total = round2(energy + 8.00);
            }
            case "WATER" -> {
                int usage = 3 + random.nextInt(13); // 3~15 吨
                double water = round2(usage * 2.80);
                double sewage = round2(usage * 1.20);
                items.add(item("水费（" + usage + " 吨 × 2.80 元/吨）", water));
                items.add(item("污水处理费（" + usage + " 吨 × 1.20 元/吨）", sewage));
                total = round2(water + sewage);
            }
            case "TV" -> {
                items.add(item("基本收视维护费", 24.00));
                if (random.nextBoolean()) {
                    items.add(item("高清增值套餐", 15.00));
                    total = 39.00;
                } else {
                    total = 24.00;
                }
            }
            default -> {
                items.add(item(typeName + "费用", 128.50));
                total = 128.50;
            }
        }

        String accountMasked = account.length() > 4
                ? "****" + account.substring(account.length() - 4)
                : account;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("queryType", type);
        result.put("typeName", typeName);
        result.put("accountNo", accountMasked);
        result.put("month", month);
        result.put("amount", total);
        result.put("status", "待缴费");
        result.put("dueDate", dueDate);
        result.put("items", items);
        result.put("simulated", true);

        // 快照存可读摘要（VARCHAR 500），供 /api/bill/history 列表展示
        String summary = String.format("%s %s账单（户号%s）：合计 ¥%.2f，待缴费，缴费截止 %s",
                month, typeName, accountMasked, total, dueDate);

        BillQuery query = new BillQuery();
        query.setUserId(request.getUserId());
        query.setQueryType(type);
        query.setQueryParams(account);
        query.setResultSnapshot(summary);
        billQueryMapper.insert(query);

        return result;
    }

    private Map<String, Object> item(String name, double amount) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("amount", amount);
        return m;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Override
    public List<BillQuery> getHistory(Long userId) {
        QueryWrapper<BillQuery> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_time");
        return billQueryMapper.selectList(wrapper);
    }
}
