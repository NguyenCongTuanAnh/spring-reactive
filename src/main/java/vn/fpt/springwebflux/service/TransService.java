package vn.fpt.springwebflux.service;

import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.model.response.BaseResponse;


public interface TransService {
    Mono<BaseResponse> findAll();

}
