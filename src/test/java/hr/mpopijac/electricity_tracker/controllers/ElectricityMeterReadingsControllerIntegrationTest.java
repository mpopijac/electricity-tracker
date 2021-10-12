package hr.mpopijac.electricity_tracker.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.mpopijac.electricity_tracker.ElectricityTrackerApplication;
import hr.mpopijac.electricity_tracker.dto.ElectricityMeterReadingDto;
import hr.mpopijac.electricity_tracker.dto.YearlyEnergyConsumptionDto;
import hr.mpopijac.electricity_tracker.models.ElectricityMeterReadingModel;
import hr.mpopijac.electricity_tracker.repository.ElectricityMeterReadingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Optional;

import static hr.mpopijac.electricity_tracker.controllers.AbstractController.BASE_API_URL;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElectricityTrackerApplication.class)
@WebAppConfiguration
public class ElectricityMeterReadingsControllerIntegrationTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ElectricityMeterReadingRepository electricityMeterReadingRepository;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void getAggregateMeterReadingForYear() throws Exception {
        final String clientId = "1";
        final String year = "2020";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/aggregate";

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = mapFromJson(content, YearlyEnergyConsumptionDto.class);

        assertEquals(780, (int) yearlyEnergyConsumptionDto.getAggregateConsumedEnergy());
        assertEquals(2020, (int) yearlyEnergyConsumptionDto.getYear());
    }

    @Test
    public void getAggregateMeterReadingForYear_noReadingsFotThatYear() throws Exception {
        final String clientId = "1";
        final String year = "1900";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/aggregate";

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = mapFromJson(content, YearlyEnergyConsumptionDto.class);

        assertEquals(0, (int) yearlyEnergyConsumptionDto.getAggregateConsumedEnergy());
        assertEquals(1900, (int) yearlyEnergyConsumptionDto.getYear());
    }

    @Test
    public void getAggregateMeterReadingForYear_clientDoesntExist() throws Exception {
        final String clientId = "999";
        final String year = "2020";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/aggregate";

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(400, status);

        final Exception exception = mvcResult.getResolvedException();
        assertEquals("Client with id:" + clientId + " is not found.", exception.getMessage());
    }

    @Test
    public void getMeterReadingsMonthlyForYear() throws Exception {
        final String clientId = "1";
        final String year = "2020";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/monthly";

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = mapFromJson(content, YearlyEnergyConsumptionDto.class);

        assertEquals(2020, (int) yearlyEnergyConsumptionDto.getYear());
        assertEquals(12, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
        assertEquals(1, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getMonth());
        assertEquals(10, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getConsumedEnergy());
        assertEquals(12, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(11).getMonth());
        assertEquals(120, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(11).getConsumedEnergy());
    }

    @Test
    public void getMeterReadingsMonthlyForYear_yearWithNoReadingData() throws Exception {
        final String clientId = "1";
        final String year = "1900";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/monthly";

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = mapFromJson(content, YearlyEnergyConsumptionDto.class);

        assertEquals(1900, (int) yearlyEnergyConsumptionDto.getYear());
        assertEquals(0, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
    }

    @Test
    public void getMeterReadingsMonthlyForYear_forSpecificMonth() throws Exception {
        final String clientId = "1";
        final String year = "2020";
        final String month = "2";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/monthly?month=" + month;

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = mapFromJson(content, YearlyEnergyConsumptionDto.class);

        assertEquals(2020, (int) yearlyEnergyConsumptionDto.getYear());
        assertEquals(1, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
        assertEquals(2, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getMonth());
        assertEquals(20, (int) yearlyEnergyConsumptionDto.getConsumptionPerMonth().get(0).getConsumedEnergy());
    }

    @Test
    public void getMeterReadingsMonthlyForYear_forSpecificMonth_outOfRange() throws Exception {
        final String clientId = "1";
        final String year = "2020";
        final String month = "20";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings" + "/" + year + "/monthly?month=" + month;

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final YearlyEnergyConsumptionDto yearlyEnergyConsumptionDto = mapFromJson(content, YearlyEnergyConsumptionDto.class);

        assertEquals(2020, (int) yearlyEnergyConsumptionDto.getYear());
        assertEquals(0, yearlyEnergyConsumptionDto.getConsumptionPerMonth().size());
    }

    @Test
    public void addMeterReading() throws Exception {
        final String clientId = "1";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings/addMeterReading";
        final ElectricityMeterReadingDto requestData = new ElectricityMeterReadingDto();
        requestData.setYear(1800);
        requestData.setMonth(1);
        requestData.setConsumedEnergy(180);

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestData))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        final String content = mvcResult.getResponse().getContentAsString();
        final ElectricityMeterReadingDto electricityMeterReadingDto = mapFromJson(content, ElectricityMeterReadingDto.class);
        assertEquals(1800, (int) electricityMeterReadingDto.getYear());
        assertEquals(1, (int) electricityMeterReadingDto.getMonth());
        assertEquals(180, (int) electricityMeterReadingDto.getConsumedEnergy());
        assertNotNull(electricityMeterReadingDto.getId());

        final Optional<ElectricityMeterReadingModel> electricityMeterReading = electricityMeterReadingRepository.findById(electricityMeterReadingDto.getId());
        assertTrue(electricityMeterReading.isPresent());
        electricityMeterReadingRepository.deleteById(electricityMeterReading.get().getId());
    }

    @Test
    public void addMeterReading_readingAlreadyExist() throws Exception {
        final String clientId = "1";
        final String uri = BASE_API_URL + "/client/" + clientId + "/electricity-meter-readings/addMeterReading";
        final ElectricityMeterReadingDto requestData = new ElectricityMeterReadingDto();
        requestData.setYear(2020);
        requestData.setMonth(1);
        requestData.setConsumedEnergy(180);

        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapToJson(requestData))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final int status = mvcResult.getResponse().getStatus();
        assertEquals(400, status);

        final String exceptionMessage = mvcResult.getResolvedException().getMessage();
        assertEquals("ElectricityMeterReading for year:2020 and month:1 already exists.", exceptionMessage);
    }


    private <T> T mapFromJson(String json, Class<T> clazz)
            throws JsonParseException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

}
