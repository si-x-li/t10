package com.ecse321.team10.riderz.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.ecse321.team10.riderz.model.User;
import com.ecse321.team10.riderz.sql.MySQLJDBC;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestParam;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestTripController {
    private static final Logger logger = LogManager.getLogger(TestTripController.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MySQLJDBC sql = new MySQLJDBC();

    @BeforeClass
    public static void setupClass() {
        logger.info("************** Starting Trip Controller Tests **************");
    }

    @AfterClass
    public static void teardownClass() {
        logger.info("************** Ending Trip Controller Tests **************");
    }

    @Before
    public void setup() throws Exception {
        sql.connect();
        sql.purgeDatabase();
    }

    @After
    public void tearDown() {
        sql.closeConnection();
    }

    @Test
    public void testInsertDeleteTrip() throws Exception {
        sql.insertUser(new User("testTripController", "1234", "test@test.com", "5149998888", "first", "last"));
        this.mockMvc.perform(put("/trip/insertTrip?operator=testTripController"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(containsString("true")));
        sql.deleteAllTrips("testTripController");
        sql.deleteUser("testTripController");
    }

    @Test
    public void testDeleteTrip() throws Exception {
        sql.insertUser(new User("testTripController", "1234", "test@test.com", "5149998888", "first", "last"));
        sql.insertTrip("testTripController");
        int tripID = sql.getLastTripByUsername("testTripController").getTripID();
        this.mockMvc.perform(put("/trip/deleteTrip?operator=testTripController&tripID=" + tripID))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(containsString("true")));
        sql.deleteAllTrips("testTripController");
        sql.deleteUser("testTripController");
    }

    @Test
    public void testDeleteAllTrips() throws Exception {
        sql.insertUser(new User("testTripController", "1234", "test@test.com", "5149998888", "first", "last"));
        sql.insertTrip("testTripController");
        sql.insertTrip("testTripController");
        this.mockMvc.perform(put("/trip/deleteAllTrips?operator=testTripController"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(containsString("true")));
        sql.deleteUser("testTripController");
    }

    @Test
    public void testGetTripsByUsername() throws Exception {
        sql.insertUser(new User("testTripController", "1234", "test@test.com", "5149998888", "first", "last"));
        sql.insertTrip("testTripController");
        sql.insertTrip("testTripController");

        MvcResult result = this.mockMvc.perform(get("/trip/username?operator=testTripController"))
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        logger.info(content);

        String expectedResult = "[{\"tripID\":1,\"operator\":\"testTripController\"},{\"tripID\":2,\"operator\":\"testTripController\"}]";

        assertEquals(content, expectedResult);
        sql.deleteAllTrips("testTripController");
        sql.deleteUser("testTripController");
    }

    @Test
    public void testGetLastTripByUsername() throws Exception{
        sql.insertUser(new User("testTripController", "1234", "test@test.com", "5149998888", "first", "last"));
        sql.insertTrip("testTripController");
        sql.insertTrip("testTripController");

        this.mockMvc.perform(get("/trip/last/?operator=testTripController"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operator").value("testTripController"))
                .andExpect(jsonPath("tripID").value("2"));
        sql.deleteAllTrips("testTripController");
        sql.deleteUser("testTripController");
    }

    @Test
    public void testGetAllTrips() throws Exception{
        sql.insertUser(new User("testTripController", "1234", "test@test.com", "5149998888", "first", "last"));
        sql.insertUser(new User("testTripController1", "1234", "test@test.com", "5149998888", "first", "last"));
        sql.insertTrip("testTripController");
        sql.insertTrip("testTripController");
        sql.insertTrip("testTripController1");

        MvcResult result = this.mockMvc.perform(get("/trip/all"))
                .andDo(print())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        logger.info(content);

        String expectedResult = "[{\"tripID\":1,\"operator\":\"testTripController\"},{\"tripID\":2,\"operator\":\"testTripController\"},{\"tripID\":3,\"operator\":\"testTripController1\"}]";

        assertEquals(content, expectedResult);
        sql.deleteAllTrips("testTripController");
        sql.deleteUser("testTripController");
    }
}