package org.example.isc.opuscore.models;

import org.example.isc.opuscore.dto.CriterionDTO;
import org.example.isc.opuscore.enums.ArtTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpusCoreCriteriaCatalog {

    static Map<ArtTypeEnum, List<CriterionDTO>>  criteriaByType;

    public OpusCoreCriteriaCatalog() {
        criteriaByType = new HashMap<>();
        putGames();

    }

    private void putGames(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                1L,
                "Own experience",
                "(not done yet)",
                25,
                5
        ));
        criteria.add(new CriterionDTO(
                2L,
                "Narrative and Directing",
                "(not done yet)",
                20,
                5
        ));
        criteria.add(new CriterionDTO(
                3L,
                "Gameplay",
                "(not done yet)",
                20,
                5
        ));
        criteria.add(new CriterionDTO(
                4L,
                "Characters",
                "(not done yet)",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                5L,
                "Audiovisuals",
                "(not done yet)",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                6L,
                "Technical",
                "(not done yet)",
                5,
                5
        ));

        criteriaByType.put(ArtTypeEnum.GAME, criteria);
    }
    //TODO
    private void putMovies(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(

        ));

        criteriaByType.put(ArtTypeEnum.GAME, criteria);
    }
}
