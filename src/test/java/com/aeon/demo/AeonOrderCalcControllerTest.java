package com.aeon.demo;

import com.aeon.demo.dto.AeonOrderCalcRequest;
import com.aeon.demo.dto.AeonOrderCalcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口冒烟：确保 Postman 调用路径可用。
 *
 * @author codex
 */
@ActiveProfiles("aeon-demo")
@SpringBootTest(classes = com.aeon.demo.AeonDemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"spring.config.name=aeon-demo"})
@AutoConfigureMockMvc
public class AeonOrderCalcControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_sample_then_calc_ok() throws Exception {
        String sampleJson = mockMvc.perform(get("/aeon-demo/order/sample"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AeonOrderCalcRequest req = objectMapper.readValue(sampleJson, AeonOrderCalcRequest.class);
        String reqJson = objectMapper.writeValueAsString(req);

        String respJson = mockMvc.perform(post("/aeon-demo/order/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AeonOrderCalcResponse resp = objectMapper.readValue(respJson, AeonOrderCalcResponse.class);
        assertEquals("56.00", resp.getAmountSummary().getFinalPayAmount().toPlainString());
    }
}

