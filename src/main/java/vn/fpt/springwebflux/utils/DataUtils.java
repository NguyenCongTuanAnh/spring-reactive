package vn.fpt.springwebflux.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.exception.BusinessException;

import java.util.Collection;
import java.util.Map;

import static vn.fpt.springwebflux.constant.CommonConstant.INPUT_NOT_VALID;
import static vn.fpt.springwebflux.constant.CommonConstant.PARSE_DATA_UNSUCCESSFULLY;
import static vn.fpt.springwebflux.constant.ErrorCodeConstant.ERROR_CODE_500;

public class DataUtils {
    public static <T> boolean isNullOrEmpty(T input) {
        if (input == null) {
            return true;
        }
        if (input instanceof String && ((String) input).isEmpty()) {
            return true;
        }
        if (input instanceof Collection && ((Collection<?>) input).isEmpty()) {
            return true;
        }
        return input instanceof Map && ((Map<?, ?>) input).isEmpty();
    }


    public static <T> Mono<T> parseStringToObject(String values, Class<T> clazz) {
        if (DataUtils.isNullOrEmpty(values)) {
            return Mono.error(new BusinessException(ERROR_CODE_500, INPUT_NOT_VALID, null));
        }
        try {
            T result = new ObjectMapper().readValue(values, clazz);
            return Mono.just(result);
        } catch (JsonProcessingException e) {
            return Mono.error(new BusinessException(ERROR_CODE_500, PARSE_DATA_UNSUCCESSFULLY, null));
        }
    }

}
