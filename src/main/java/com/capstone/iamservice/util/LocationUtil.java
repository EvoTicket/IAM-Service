package com.capstone.iamservice.util;

import com.capstone.iamservice.dto.request.AddressInfo;
import com.capstone.iamservice.entity.Province;
import com.capstone.iamservice.entity.Ward;
import com.capstone.iamservice.exception.AppException;
import com.capstone.iamservice.exception.ErrorCode;
import com.capstone.iamservice.repository.ProvinceRepository;
import com.capstone.iamservice.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationUtil {
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;

    public Province getProvinceByCode(Integer provinceCode) {
        return provinceRepository.findByCode(provinceCode)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "không tìm thấy tỉnh"));
    }

    public Ward getWardByCode(Integer wardCode) {
        return wardRepository.findByCode(wardCode)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "không tìm thấy phường/xã"));
    }

    public AddressInfo getAddressInfo(Province province, Ward ward, String fullAddress) {
        return AddressInfo.builder()
                .wardCode(ward != null ? ward.getCode() : null)
                .wardName(ward != null ? ward.getName() : null)
                .provinceCode(province != null ? province.getCode() : null)
                .provinceName(province != null ? province.getName() : null)
                .fullAddress(fullAddress)
                .build();
    }
}
