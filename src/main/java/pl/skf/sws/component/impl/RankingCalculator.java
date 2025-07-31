package pl.skf.sws.component.impl;

import pl.skf.sws.model.DigiKatResponse;
import pl.skf.sws.model.Movie;

public interface RankingCalculator {

    int calculateRanking(Movie movie, DigiKatResponse response);

}
