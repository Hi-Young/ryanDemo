package com.ryan.experiment.test;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * 批量生成兑换口令工具类
 * </p>
 *
 * @version 1.0.0
 * @fileName: VerifyCodeUtil
 * @author: AD(陈德华)
 * @date: 2021/2/22 16:42
 */
public class VerifyCodeUtil {

    /**
     * 初始化6位随机数集合
     */
    private static String VERIFY_CODE_ARRAY = "0qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM123456789";

    private static int scale = 62;

    /**
     * <p>
     * 将数字转为62进制
     * </p>
     *
     * @return java.lang.String
     * @Param [num]
     * @author AD(陈德华)
     * @version 1.0.0
     * @date 2021/3/1 18:39
     */
    public static String encode(long num) {
        StringBuilder sb = new StringBuilder();
        int remainder = 0;
        while (num > scale - 1) {
            /**
             * 对 scale 进行求余，然后将余数追加至 sb 中，由于是从末位开始追加的，因此最后需要反转（reverse）字符串
             */
            remainder = Long.valueOf(num % scale).intValue();
            sb.append(VERIFY_CODE_ARRAY.charAt(remainder));
            num = num / scale;
        }
        sb.append(VERIFY_CODE_ARRAY.charAt(Long.valueOf(num).intValue()));
        String value = sb.reverse().toString();
        return StringUtils.leftPad(value, 6, '0');
    }

    /**
     * <p>
     * 62进制字符串转为数字
     * </p>
     *
     * @return long
     * @Param [str]
     * @author AD(陈德华)
     * @version 1.0.0
     * @date 2021/3/1 18:40
     */
    public static long decode(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        /**
         * 将 0 开头的字符串进行替换
         */
        str = str.replace("^0*", "");
        long num = 0;
        int index;
        for (int i = 0; i < str.length(); i++) {
            /**
             * 查找字符的索引位置
             */
            index = VERIFY_CODE_ARRAY.indexOf(str.charAt(i));
            /**
             * 索引位置代表字符的数值
             */
            num += (long) (index * (Math.pow(scale, str.length() - i - 1)));
        }
        return num;
    }

    /**
     * <p>
     * 批量生成口令
     * </p>
     *
     * @return java.util.Set<java.lang.String>
     * @Param [batchNum]
     * @author AD(陈德华)
     * @version 1.0.0
     * @date 2021/2/22 16:46
     */
    public static Set<String> batchGenerateVerifyCode(int batchNum, String keywordCode) {
        if (batchNum <= 0) {
            return new LinkedHashSet<>();
        }
        if (StringUtils.isBlank(keywordCode)) {
            return new LinkedHashSet<>();
        }
        Set<String> verifyCodeSets = new LinkedHashSet<>();
        char[] charArray = VERIFY_CODE_ARRAY.toCharArray();
        while (verifyCodeSets.size() < batchNum) {
            verifyCodeSets.add(verifyCode(ThreadLocalRandom.current(), charArray).concat(keywordCode));
        }
        return verifyCodeSets;
    }

    /**
     * <p>
     * 批量生成口令
     * </p>
     *
     * @return java.util.Set<java.lang.String>
     * @Param [batchNum]
     * @author AD(陈德华)
     * @version 1.0.0
     * @date 2021/2/22 16:46
     */
    public static Set<String> batchGenerateVerifyCode(int batchNum, Long keywordId) {
        if (batchNum <= 0) {
            return new LinkedHashSet<>();
        }
        if (keywordId <= 0) {
            return new LinkedHashSet<>();
        }
        String keywordCode = encode(keywordId);
        Set<String> verifyCodeSets = new LinkedHashSet<>();
        char[] charArray = VERIFY_CODE_ARRAY.toCharArray();
        while (verifyCodeSets.size() < batchNum) {
            verifyCodeSets.add(verifyCode(ThreadLocalRandom.current(), charArray).concat(keywordCode));
        }
        return verifyCodeSets;
    }


    /**
     * <p>
     * 生成单个口令
     * </p>
     *
     * @return java.lang.String
     * @Param []
     * @author AD(陈德华)
     * @version 1.0.0
     * @date 2021/2/22 16:47
     */
    public static String verifyCode(ThreadLocalRandom random, char[] c) {
        int length = c.length;
        String code = "";
        int k = 6;
        while (k > 0) {
            code += c[random.nextInt(length)];
            k--;
        }
        return code;
    }

    public static void main(String[] args) {
        BigDecimal.ONE.compareTo(BigDecimal.ZERO);
    }

}

