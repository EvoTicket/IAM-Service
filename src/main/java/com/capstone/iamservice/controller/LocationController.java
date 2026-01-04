package com.capstone.iamservice.controller;

import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/provinces")
    public List<Province> getAllProvinces() {
        return locationService.getAllProvinces();
    }

    @GetMapping("/wards")
    public List<Ward> getWardsByProvinceCode(@RequestParam(value = "provinceCode", required = false) Integer provinceCode) {
        return locationService.getWardsByProvinceCode(provinceCode);
    }
}
