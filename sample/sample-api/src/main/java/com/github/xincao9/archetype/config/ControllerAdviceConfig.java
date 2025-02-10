package com.github.xincao9.archetype.config;

import fun.golinks.core.consts.StatusEnums;
import fun.golinks.core.model.R;
import fun.golinks.core.utils.JsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ControllerAdviceConfig {

    /**
     * 针对jsr302 Java Bean Validator 抛出去的异常统一输出格式
     *
     * @param ex 异常
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        R r = new R(StatusEnums.BAD_REQUEST.getCode(), null, errors);
        return new ResponseEntity<>(JsonUtils.toJsonString(r), HttpStatus.OK);
    }
}
