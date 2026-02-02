package com.ryan.es.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.regex.Pattern;

/**
 * Chinese keyword search strategy inspired by AEON coupon search (BaseChineseQuery).
 *
 * <p>Uses a {@code dis_max} query that dynamically picks sub-queries based on:
 * <ul>
 *   <li>Keyword length (1 / 2-4 / 5+)</li>
 *   <li>Character type (all-Chinese / no-Chinese / mixed)</li>
 * </ul>
 *
 * <p>Sub-query types used:
 * <ul>
 *   <li>{@code prefix} on {@code field.lowercase} — fast left-prefix match</li>
 *   <li>{@code match} on {@code field} (standard ik_max_word) — broad recall</li>
 *   <li>{@code match} on {@code field.smart_word} (ik_smart) — precision</li>
 *   <li>{@code match} on {@code field.max_word} (ik_max_word) — recall for long keywords</li>
 *   <li>{@code match_phrase} for phrase-level boost</li>
 *   <li>Dynamic {@code fuzziness} based on keyword length</li>
 * </ul>
 */
public class ChineseSearchStrategy {

    private static final Pattern ALL_CHINESE = Pattern.compile("^[\\u4e00-\\u9fa5]+$");
    private static final Pattern HAS_CHINESE = Pattern.compile("[\\u4e00-\\u9fa5]");

    private final ObjectMapper om;

    public ChineseSearchStrategy(ObjectMapper om) {
        this.om = om;
    }

    /**
     * Build a dis_max query node for the given field and keyword.
     *
     * @param field   base field name (e.g. "title")
     * @param keyword user search keyword
     * @return an ObjectNode representing the dis_max query
     */
    public ObjectNode buildDisMaxQuery(String field, String keyword) {
        String trimmed = keyword.trim();
        int len = trimmed.length();

        KeywordType type = classifyKeyword(trimmed);
        String fuzziness = computeFuzziness(len);

        ObjectNode disMax = om.createObjectNode();
        ObjectNode disMaxInner = disMax.putObject("dis_max");
        disMaxInner.put("tie_breaker", 0.3);
        ArrayNode queries = disMaxInner.putArray("queries");

        if (len == 1) {
            buildSingleCharStrategy(queries, field, trimmed, type);
        } else if (len <= 4) {
            buildShortKeywordStrategy(queries, field, trimmed, type, fuzziness);
        } else {
            buildLongKeywordStrategy(queries, field, trimmed, type, fuzziness);
        }

        return disMax;
    }

    /**
     * 1-char keyword: prefix + standard match (no fuzzy for single char).
     */
    private void buildSingleCharStrategy(ArrayNode queries, String field, String keyword, KeywordType type) {
        // prefix on lowercase sub-field (boost 3.0)
        addPrefixQuery(queries, field + ".lowercase", keyword, 3.0);

        // standard match on base field (boost 1.0)
        addMatchQuery(queries, field, keyword, null, 1.0);

        if (type != KeywordType.ALL_CHINESE) {
            // For non-Chinese single char, also match on lowercase
            addMatchQuery(queries, field + ".lowercase", keyword, null, 2.0);
        }
    }

    /**
     * 2-4 chars keyword: prefix + smart_word match + optional fuzzy.
     */
    private void buildShortKeywordStrategy(ArrayNode queries, String field, String keyword,
                                           KeywordType type, String fuzziness) {
        // prefix (boost 4.0)
        addPrefixQuery(queries, field + ".lowercase", keyword, 4.0);

        // smart_word match (boost 3.0)
        addMatchQuery(queries, field + ".smart_word", keyword, null, 3.0);

        // standard match (boost 1.5)
        addMatchQuery(queries, field, keyword, null, 1.5);

        // fuzzy match on standard field if fuzziness > 0
        if (!"0".equals(fuzziness)) {
            addMatchQuery(queries, field, keyword, fuzziness, 0.8);
        }

        if (type == KeywordType.MIXED || type == KeywordType.NO_CHINESE) {
            addMatchQuery(queries, field + ".lowercase", keyword, null, 2.0);
        }
    }

    /**
     * 5+ chars keyword: prefix + phrase + smart_word + max_word + fuzzy.
     */
    private void buildLongKeywordStrategy(ArrayNode queries, String field, String keyword,
                                          KeywordType type, String fuzziness) {
        // prefix (boost 5.0)
        addPrefixQuery(queries, field + ".lowercase", keyword, 5.0);

        // match_phrase for exact sequence boost (boost 4.0)
        addMatchPhraseQuery(queries, field, keyword, 4.0);

        // smart_word match (boost 3.0)
        addMatchQuery(queries, field + ".smart_word", keyword, null, 3.0);

        // max_word match — broader recall (boost 2.0)
        addMatchQuery(queries, field + ".max_word", keyword, null, 2.0);

        // standard match (boost 1.0)
        addMatchQuery(queries, field, keyword, null, 1.0);

        // fuzzy match
        if (!"0".equals(fuzziness)) {
            addMatchQuery(queries, field, keyword, fuzziness, 0.5);
        }

        if (type == KeywordType.MIXED || type == KeywordType.NO_CHINESE) {
            addMatchQuery(queries, field + ".lowercase", keyword, null, 2.5);
        }
    }

    private void addPrefixQuery(ArrayNode queries, String field, String value, double boost) {
        ObjectNode q = queries.addObject();
        ObjectNode prefix = q.putObject("prefix");
        ObjectNode fieldNode = prefix.putObject(field);
        fieldNode.put("value", value.toLowerCase());
        fieldNode.put("boost", boost);
    }

    private void addMatchQuery(ArrayNode queries, String field, String value, String fuzziness, double boost) {
        ObjectNode q = queries.addObject();
        ObjectNode match = q.putObject("match");
        ObjectNode fieldNode = match.putObject(field);
        fieldNode.put("query", value);
        fieldNode.put("boost", boost);
        if (fuzziness != null) {
            fieldNode.put("fuzziness", fuzziness);
        }
    }

    private void addMatchPhraseQuery(ArrayNode queries, String field, String value, double boost) {
        ObjectNode q = queries.addObject();
        ObjectNode mp = q.putObject("match_phrase");
        ObjectNode fieldNode = mp.putObject(field);
        fieldNode.put("query", value);
        fieldNode.put("boost", boost);
    }

    /**
     * Compute dynamic fuzziness based on keyword length.
     * <ul>
     *   <li>length &lt; 4 → "0" (no fuzzy)</li>
     *   <li>4 &le; length &le; 6 → "1"</li>
     *   <li>length &ge; 7 → "2"</li>
     * </ul>
     */
    static String computeFuzziness(int len) {
        if (len < 4) {
            return "0";
        } else if (len <= 6) {
            return "1";
        } else {
            return "2";
        }
    }

    static KeywordType classifyKeyword(String keyword) {
        if (ALL_CHINESE.matcher(keyword).matches()) {
            return KeywordType.ALL_CHINESE;
        }
        if (HAS_CHINESE.matcher(keyword).find()) {
            return KeywordType.MIXED;
        }
        return KeywordType.NO_CHINESE;
    }

    enum KeywordType {
        ALL_CHINESE,
        NO_CHINESE,
        MIXED
    }
}
