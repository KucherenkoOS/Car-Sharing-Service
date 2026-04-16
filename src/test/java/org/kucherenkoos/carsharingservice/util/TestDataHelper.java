package org.kucherenkoos.carsharingservice.util;

import java.math.BigDecimal;
import java.util.Set;
import org.kucherenkoos.carsharingservice.dto.car.CarDto;
import org.kucherenkoos.carsharingservice.dto.car.CreateCarRequestDto;
import org.kucherenkoos.carsharingservice.dto.car.UpdateCarRequestDto;
import org.kucherenkoos.carsharingservice.model.Car;
import org.kucherenkoos.carsharingservice.model.CarType;
import org.kucherenkoos.carsharingservice.model.Role;
import org.kucherenkoos.carsharingservice.model.RoleName;
import org.kucherenkoos.carsharingservice.model.User;

public final class TestDataHelper {
    public static final Long CAR_ID = 1L;
    public static final String MODEL = "Model S";
    public static final String BRAND = "Tesla";
    public static final CarType TYPE = CarType.SEDAN;
    public static final int INVENTORY = 5;
    public static final BigDecimal DAILY_FEE = BigDecimal.valueOf(100.50);
    public static final String UPDATED_MODEL = "Model 3";
    public static final int UPDATED_INVENTORY = 10;
    public static final BigDecimal UPDATED_DAILY_FEE = BigDecimal.valueOf(80.00);
    public static final Long USER_ID = 1L;
    public static final String USER_EMAIL = "test_user@example.com";
    public static final Long MANAGER_ID = 2L;
    public static final String MANAGER_EMAIL = "test_manager@example.com";

    private TestDataHelper() {
    }

    public static Car createTestCar() {
        Car car = new Car();
        car.setId(CAR_ID);
        car.setModel(MODEL);
        car.setBrand(BRAND);
        car.setCarType(TYPE);
        car.setInventory(INVENTORY);
        car.setDailyFee(DAILY_FEE);
        return car;
    }

    public static CarDto createTestCarDto() {
        CarDto dto = new CarDto();
        dto.setId(CAR_ID);
        dto.setModel(MODEL);
        dto.setBrand(BRAND);
        dto.setCarType(TYPE.name());
        dto.setInventory(INVENTORY);
        dto.setDailyFee(DAILY_FEE);
        return dto;
    }

    public static CreateCarRequestDto createTestCreateCarRequestDto() {
        CreateCarRequestDto dto = new CreateCarRequestDto();
        dto.setModel(MODEL);
        dto.setBrand(BRAND);
        dto.setCarType(TYPE);
        dto.setInventory(INVENTORY);
        dto.setDailyFee(DAILY_FEE);
        return dto;
    }

    public static UpdateCarRequestDto createTestUpdateCarRequestDto() {
        UpdateCarRequestDto dto = new UpdateCarRequestDto();
        dto.setModel(UPDATED_MODEL);
        dto.setBrand(BRAND);
        dto.setCarType(TYPE);
        dto.setInventory(UPDATED_INVENTORY);
        dto.setDailyFee(UPDATED_DAILY_FEE);
        return dto;
    }

    public static User createTestUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setRoles(Set.of(createRole(RoleName.ROLE_USER)));
        return user;
    }

    public static User createTestManager() {
        User user = new User();
        user.setId(MANAGER_ID);
        user.setEmail(MANAGER_EMAIL);
        user.setRoles(Set.of(createRole(RoleName.ROLE_MANAGER)));
        return user;
    }

    private static Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}
