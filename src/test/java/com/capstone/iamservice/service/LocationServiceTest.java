package com.capstone.iamservice.service;

import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.repository.ProvinceRepository;
import com.capstone.iamservice.repository.WardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private WardRepository wardRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    void getAllProvinces_ShouldReturnList() {
        List<Province> mockList = List.of(new Province(), new Province());
        when(provinceRepository.findAll()).thenReturn(mockList);

        List<Province> result = locationService.getAllProvinces();

        assertEquals(2, result.size());
        verify(provinceRepository, times(1)).findAll();
    }

    @Test
    void getWardsByProvinceCode_WithCode_ShouldReturnList() {
        List<Ward> mockList = List.of(new Ward(), new Ward());
        when(wardRepository.findByProvinceCode(1)).thenReturn(mockList);

        List<Ward> result = locationService.getWardsByProvinceCode(1);

        assertEquals(2, result.size());
        verify(wardRepository, times(1)).findByProvinceCode(1);
    }

    @Test
    void getWardsByProvinceCode_NullCode_ShouldReturnAllWards() {
        List<Ward> mockList = List.of(new Ward(), new Ward());
        when(wardRepository.findAll()).thenReturn(mockList);

        List<Ward> result = locationService.getWardsByProvinceCode(null);

        assertEquals(2, result.size());
        verify(wardRepository, times(1)).findAll();
    }
}
