/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hello;

import static java.lang.Integer.parseInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GreetingControllerTests
{

    @Autowired
    private MockMvc mockMvc;

    private ArrayList<ResultActions> makeResponseArrayForEmptyRequests(int size) throws Exception
    {
        var returnArray = new ArrayList<ResultActions>(size);
        for (int i = 0; i < size; i++) {
            returnArray.add(this.mockMvc.perform(get("/greeting")));
        }
        return returnArray;
    }

    @Test
    public void noParamPostGreetingShouldReturnDefaultMessage() throws Exception
    {
        this.mockMvc.perform(post("/greeting")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, WorldPost!"));
    }

    @Test
    public void aaaaaa_sorry_noParamGetGreetingShouldReturnDefaultMessage() throws Exception
    {
        this.mockMvc.perform(get("/greeting")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, World!"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void noParamGreetingShouldIncreaseCounterForEachPostRequest() throws Exception
    {
        int requestBatchSize = 5;
        var batchResponseResults = makeResponseArrayForEmptyRequests(requestBatchSize);
        /* I really dislike this method, would prefer alternative. How to convert  */
        var stringGreetingId = JsonPath.parse(batchResponseResults.get(0).andReturn().getResponse().getContentAsString()).read("$.id").toString();
        int intGreetingId = Integer.parseInt(stringGreetingId);
        for (int i = 1; i < requestBatchSize - 1; ++i) {
            batchResponseResults.get(i).andExpect( jsonPath("$.id").value(intGreetingId+i));
        }
    }

    @Test
    public void objectMapper() throws Exception
    {
        var response = this.mockMvc.perform(get("/greeting")).andReturn().getResponse();
        var objectMapper = new ObjectMapper();
        Greeting returnedObject = objectMapper.readValue(response.getContentAsString(), Greeting.class);
        assert( returnedObject.getContent().equals("Hello, World!"));
    }

    @Test
    public void noParamGreetingCounterShouldPersistAcrossSessions() throws Exception
    {
        noParamGreetingShouldIncreaseCounterForEachPostRequest();
        /* Restart app??? */
    }

    @Test
    public void paramGreetingShouldReturnTailoredMessage() throws Exception
    {
        this.mockMvc.perform(get("/greeting").param("name", "Spring Community"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, Spring Community!"));
    }

}
