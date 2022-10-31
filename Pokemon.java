package com.ctrip.dcs.recommendation.query.interfaces.converter;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Pokemon {

    /**
     * 伤害倍数对应的分数
     * 站在防御方的受到伤害的倍数角度的分数
     * 攻击方的分数只需要将防御方分数乘-1即可
     */
    private final static Map<Double, Integer> DAMAGE_MAP = new HashMap<>();

    /**
     * 属性的坐标
     */
    private final static Map<Integer, String> INDEX_2_ATTRIBUTE_MAP = new HashMap<>();

    /**
     * 属性相克表，横轴为防御方，竖轴为攻击方
     * 为了方便展示，将原有的数值都做了乘2的操作
     * 从左到右，从上倒下，按照属性的坐标排序
     */
    private final static double[][] SZ = new double[][]{
            {2, 2, 2, 2, 2, 1, 2, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2},
            {4, 2, 1, 1, 2, 4, 1, 0, 4, 2, 2, 2, 2, 1, 4, 2, 4, 1},
            {2, 4, 2, 2, 2, 1, 4, 2, 1, 2, 2, 4, 1, 2, 2, 2, 2, 2},
            {2, 2, 2, 1, 1, 1, 2, 1, 0, 2, 2, 4, 2, 2, 2, 2, 2, 4},
            {2, 2, 0, 4, 2, 4, 1, 2, 4, 4, 2, 1, 4, 2, 2, 2, 2, 2},
            {2, 1, 4, 2, 1, 2, 4, 2, 1, 4, 2, 2, 2, 2, 4, 2, 2, 2},
            {2, 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 4, 2, 4, 2, 2, 4, 1},
            {0, 2, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 4, 2, 2, 1, 2},
            {2, 2, 2, 2, 2, 4, 2, 2, 1, 1, 1, 2, 1, 2, 4, 2, 2, 4},
            {2, 2, 2, 2, 2, 1, 4, 2, 4, 1, 1, 4, 2, 2, 4, 1, 2, 2},
            {2, 2, 2, 2, 4, 4, 2, 2, 2, 4, 1, 1, 2, 2, 2, 1, 2, 2},
            {2, 2, 1, 1, 4, 4, 1, 2, 1, 1, 4, 1, 2, 2, 2, 1, 2, 2},
            {2, 2, 4, 2, 0, 2, 2, 2, 2, 2, 4, 1, 1, 2, 2, 1, 2, 2},
            {2, 4, 2, 4, 2, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 0, 2},
            {2, 2, 4, 2, 4, 2, 2, 2, 1, 1, 1, 4, 2, 2, 1, 4, 2, 2},
            {2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 4, 2, 0},
            {2, 1, 2, 2, 2, 2, 2, 4, 2, 2, 2, 2, 2, 4, 2, 2, 1, 1},
            {2, 4, 2, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 4, 4, 2},
    };

    static {
        // 初始化伤害分数
        DAMAGE_MAP.put(4D, -2);
        DAMAGE_MAP.put(2D, -1);
        DAMAGE_MAP.put(1D, 0);
        DAMAGE_MAP.put(0.5D, 1);
        DAMAGE_MAP.put(0.25D, 2);
        DAMAGE_MAP.put(0D, 3);

        // 初始化属性坐标
        INDEX_2_ATTRIBUTE_MAP.put(0, "一般");
        INDEX_2_ATTRIBUTE_MAP.put(1, "格斗");
        INDEX_2_ATTRIBUTE_MAP.put(2, "飞行");
        INDEX_2_ATTRIBUTE_MAP.put(3, "毒");
        INDEX_2_ATTRIBUTE_MAP.put(4, "地面");
        INDEX_2_ATTRIBUTE_MAP.put(5, "岩石");
        INDEX_2_ATTRIBUTE_MAP.put(6, "虫");
        INDEX_2_ATTRIBUTE_MAP.put(7, "幽灵");
        INDEX_2_ATTRIBUTE_MAP.put(8, "钢");
        INDEX_2_ATTRIBUTE_MAP.put(9, "火");
        INDEX_2_ATTRIBUTE_MAP.put(10, "水");
        INDEX_2_ATTRIBUTE_MAP.put(11, "草");
        INDEX_2_ATTRIBUTE_MAP.put(12, "电");
        INDEX_2_ATTRIBUTE_MAP.put(13, "超能力");
        INDEX_2_ATTRIBUTE_MAP.put(14, "冰");
        INDEX_2_ATTRIBUTE_MAP.put(15, "龙");
        INDEX_2_ATTRIBUTE_MAP.put(16, "恶");
        INDEX_2_ATTRIBUTE_MAP.put(17, "妖精");

        // 恢复原始数据的值
        for (int i = 0; i < SZ.length; i++) {
            for (int j = 0; j < SZ[i].length; j++) {
                SZ[i][j] = SZ[i][j] / 2;
            }
        }
    }


    public static void main(String[] args) {
        // 计算防御优势
        /**
         * key：分数
         * value：此得分的key，多个属性之间用“-”连接
         */
        Map<Integer, List<String>> defenseScoreMap = new TreeMap<>();
        // 1.计算单属性分数
        IntStream.range(0, SZ.length).forEach(i ->
                putScoreMap(defenseScoreMap, Arrays.stream(SZ).map(s -> s[i]).map(DAMAGE_MAP::get).mapToInt(Integer::intValue).sum(), i)
        );
        // 2.计算双属性分数
        IntStream.range(0, SZ.length - 1).forEach(i -> IntStream.range(i + 1, SZ.length).forEach(j ->
                putScoreMap(defenseScoreMap, Arrays.stream(SZ).map(s -> s[i] * s[j]).map(DAMAGE_MAP::get).mapToInt(Integer::intValue).sum(), i, j))
        );
        // 3.输出防御优势得分
        printResult("防御属性优势", defenseScoreMap);

        // 计算攻击优势
        /**
         * key：分数
         * value：此得分的key，
         */
        Map<Integer, List<String>> attackScoreMap = new TreeMap<>();
        // 1.计算单属性分数
        IntStream.range(0, SZ.length).forEach(i ->
                putScoreMap(attackScoreMap, Arrays.stream(SZ[i]).map(DAMAGE_MAP::get).mapToInt(score -> (int) score).sum() * -1, i)
        );
        // 2.输出攻击优势得分
        printResult("攻击属性优势", attackScoreMap);
    }

    private static void putScoreMap(Map<Integer, List<String>> scoreMap, int score, int... indexes) {
        List<String> keys = scoreMap.get(score);
        if (CollectionUtils.isEmpty(keys)) {
            keys = new ArrayList<>();
            scoreMap.put(score, keys);
        }
        keys.add(Arrays.stream(indexes).mapToObj(INDEX_2_ATTRIBUTE_MAP::get).collect(Collectors.joining("-")));
    }

    private static void printResult(String title, Map<Integer, List<String>> scoreMap) {
        System.out.println("--------------------" + title + "--------------------");
        scoreMap.forEach((score, keys) -> System.out.println("得分：" + score + ",属性：" + keys));
    }
}
