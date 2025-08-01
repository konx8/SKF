package pl.skf.sws.config;

import org.junit.jupiter.api.Test;
import org.modelmapper.Condition;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ModelMapperConfigTest {

    @Autowired
    private ModelMapper modelMapper;

    @Test
    void modelMapper_shouldHaveStrictMatchingStrategy() {
        assertEquals(MatchingStrategies.STRICT, modelMapper.getConfiguration().getMatchingStrategy());
    }

    @Test
    void modelMapper_shouldHaveIsNotNullPropertyCondition() {
        Condition<?, ?> condition = modelMapper.getConfiguration().getPropertyCondition();
        assertEquals(Conditions.isNotNull().getClass(), condition.getClass());
    }

}