package com.capstone.iamservice.service;

import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.repository.ProvinceRepository;
import com.capstone.iamservice.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;

    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    public List<Ward> getWardsByProvinceCode(Integer provinceCode) {
        if(provinceCode != null ) return wardRepository.findByProvinceCode(provinceCode);
        else return wardRepository.findAll();
    }
}