package com.simo333.driver.payload.answer;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AnswerUpdateRequest {
    @NotNull
    @Size(min = 3, max = 255)
    private String text;
}
