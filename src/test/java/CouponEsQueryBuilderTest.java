import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryan.es.coupon.CouponEsQueryBuilder;
import com.ryan.es.coupon.CouponSearchRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CouponEsQueryBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void buildSearchBody_shouldContainCommonCouponSearchDsl() throws Exception {
        CouponSearchRequest req = new CouponSearchRequest();
        req.setKeyword("星巴克 买一送一");
        req.setCityCode("310000");
        req.setMerchantId(2001L);
        req.setCategoryIds(java.util.Arrays.asList("food", "coffee"));
        req.setTags(java.util.Arrays.asList("咖啡"));
        req.setValidOnly(true);
        req.setSort("distance");
        req.setLat(31.2304);
        req.setLon(121.4737);
        req.setDistanceKm(5.0);
        req.setWithAggs(true);
        req.setHighlight(true);

        String json = new CouponEsQueryBuilder().buildSearchBody(req);
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.path("track_total_hits").asBoolean());
        assertEquals(0, root.path("from").asInt());
        assertEquals(10, root.path("size").asInt());

        JsonNode bool = root.path("query").path("function_score").path("query").path("bool");
        assertTrue(bool.path("must").isArray());
        assertTrue(bool.path("filter").isArray());

        // Geo sort should exist when sort=distance + (lat, lon) provided.
        assertTrue(root.path("sort").isArray());
        assertTrue(root.path("sort").get(0).has("_geo_distance"));

        // Highlight + aggs should exist by default.
        assertTrue(root.has("highlight"));
        assertTrue(root.has("aggs"));
        assertTrue(root.path("aggs").has("by_merchant"));
    }

    @Test
    public void buildSuggestBody_shouldBuildCompletionSuggest() throws Exception {
        String json = new CouponEsQueryBuilder().buildSuggestBody("星", 10);
        JsonNode root = objectMapper.readTree(json);
        assertEquals(0, root.path("size").asInt());
        assertTrue(root.path("suggest").has("title_suggest"));
        assertEquals("titleSuggest", root.path("suggest").path("title_suggest").path("completion").path("field").asText());
    }
}

