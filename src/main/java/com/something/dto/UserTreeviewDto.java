package com.something.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTreeviewDto {
    private String username;
    private double totalSales;
    private List<UserTreeviewDto> subUsers;
}
