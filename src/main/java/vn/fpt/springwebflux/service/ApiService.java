package vn.fpt.springwebflux.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vn.fpt.springwebflux.model.entity.Decl;

import static vn.fpt.springwebflux.constant.CommonConstant.GET_DECL_URL;

@Service
public class ApiService {

    private final WebClient webClient;

    @Autowired
    public ApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(GET_DECL_URL).build();
    }

    public Mono<Decl> getDecl(String id) {
        return webClient.get()
                .uri("/decl/1")  // GET delc with code = 1
                .retrieve()
                .bodyToMono(Decl.class);  // or specify the type of data you expect
    }
}
