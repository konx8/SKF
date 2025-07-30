package pl.skf.sws.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.skf.sws.model.DigiKatResponse;

@FeignClient(name = "DigiKatClient", url = "${digikat.url}")
public interface DigiKatClient {

    @GetMapping("/ranking")
    DigiKatResponse getRanking(@RequestParam("film") String title);

    @PostMapping("/ranking")
    DigiKatResponse postRanking(@RequestParam("film") String title,
                                @RequestParam("ocenaKrytykow") int rate);

}
