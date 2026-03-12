package org.example.isc.opuscore.models;

import org.example.isc.opuscore.dto.CriterionDTO;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpusCoreCriteriaCatalog {

    private final Map<ArtTypeEnum, List<CriterionDTO>>  criteriaByType;

    public OpusCoreCriteriaCatalog() {
        criteriaByType = new HashMap<>();
        putGames();
        putMovies();

    }

    public void putMusic(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add((new CriterionDTO(
                1L,
                "Structure / Rhythm",
                "not done yet",
                17,
                5
        )));
        criteria.add((new CriterionDTO(
                2L,
                "Rhymes / Figures",
                "not done yet",
                15,
                5
        )));
        criteria.add((new CriterionDTO(
                3L,
                "Realisation",
                "not done yet",
                15,
                5
        )));
        criteria.add((new CriterionDTO(
                4L,
                "Individuality / Charisma",
                "not done yet",
                15,
                5
        )));
        criteria.add((new CriterionDTO(
                5L,
                "Vibe",
                "not done yet",
                30,
                5
        )))

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
        criteriaByType.put(ArtTypeEnum.MOVIE, new ArrayList<>());
    }

    public Map<ArtTypeEnum, List<CriterionDTO>> getCriteriaByType() {
        return criteriaByType;
    }

    public CriterionDTO getById(Long id) {
        for (List<CriterionDTO> criteria : criteriaByType.values()) {
            for (CriterionDTO criterion : criteria) {
                if (criterion.getId() != null && criterion.getId().equals(id)) {
                    return criterion;
                }
            }
        }
        throw new IllegalArgumentException("Unknown criterion id: " + id);
    }

    public int getWeightById(Long id) {
        return getById(id).getWeight();
    }
}
