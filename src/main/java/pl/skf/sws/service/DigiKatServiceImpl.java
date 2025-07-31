package pl.skf.sws.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.skf.sws.feign.DigiKatClient;
import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.service.impl.DigiKatService;

@Service
@AllArgsConstructor
public class DigiKatServiceImpl implements DigiKatService {

    private DigiKatClient digiKatClient;

    public DigiKatResponse getRankingByTitle(String title) {
        return digiKatClient.getRanking(title);
    }

}
