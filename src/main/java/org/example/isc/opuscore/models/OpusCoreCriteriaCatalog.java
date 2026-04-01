package org.example.isc.opuscore.models;

import org.example.isc.opuscore.dto.CriterionDTO;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpusCoreCriteriaCatalog {

    private final Map<ArtTypeEnum, List<CriterionDTO>> criteriaByType;
    private final Map<Long, CriterionDTO> criteriaById;

    public OpusCoreCriteriaCatalog() {
        criteriaByType = new EnumMap<>(ArtTypeEnum.class);
        criteriaById = new HashMap<>();
        putMusic();
        putGames();
        putMovies();
        putShows();
        putAnime();
        putManga();
        putBooks();
    }

    public void putMusic(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                1L,
                "Structure / Rhythm",
                "The melody and emotional contrasts of individual sections of the song, " +
                          "how well the different sections of the song sound harmoniously together throughout the entire length. " +
                          "In the case of an album, the concept and arrangement of songs within the album.",
                14,
                5
        ));
        criteria.add((new CriterionDTO(
                2L,
                "Rhymes / Figures",
                "Lyrics score takes into account the specific characteristics of the musical genre or subgenre. " +
                          "Different music has its own semantic and lyrical content.",
                14,
                5
        )));
        criteria.add((new CriterionDTO(
                3L,
                "Realisation",
                "Quality of vocals, recitative, and the ability to work with selected melodies and bars, as well as the quality of mixing the song and instrumental.",
                14,
                5
        )));
        criteria.add((new CriterionDTO(
                4L,
                "Individuality / Charisma",
                "The specificity of the artist's voice timbre and chosen performance style—how unique the artist is—and how well the artist immerses the listener in the emotions conveyed—whether I believe or don't believe the song.",
                14,
                5
        )));
        criteria.add((new CriterionDTO(
                5L,
                "Relevance",
                "Determines how relevant a given work was at the time of its release. " +
                          "Give extra points if the work itself introduced relevance to this type of music.",
                14,
                5
        )));
        criteria.add((new CriterionDTO(
                6L,
                "Vibe",
                "Your subjective perception and feelings about the release",
                30,
                5
        )));
        register(ArtTypeEnum.MUSIC, criteria);
    }

    // TODO
    private void putMovies(){
        register(ArtTypeEnum.MOVIE, new ArrayList<>());
    }

    // TODO
    private void putShows(){
        register(ArtTypeEnum.SHOW, new ArrayList<>());
    }

    private void putGames(){
        List<CriterionDTO> criteria = new ArrayList<>();

        criteria.add(new CriterionDTO(
                7L,
                "Narrative and Directing",
                "(not done yet)",
                19,
                5
        ));
        criteria.add(new CriterionDTO(
                8L,
                "Gameplay",
                "(not done yet)",
                19,
                5
        ));
        criteria.add(new CriterionDTO(
                9L,
                "Characters",
                "(not done yet)",
                14,
                5
        ));
        criteria.add(new CriterionDTO(
                10L,
                "Audiovisuals",
                "(not done yet)",
                14,
                5
        ));
        criteria.add(new CriterionDTO(
                11L,
                "Technical",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                12L,
                "Own experience",
                "(not done yet)",
                24,
                5
        ));
        register(ArtTypeEnum.GAME, criteria);
    }

    private void putAnime(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                13L,
                "Plot",
                "not done yet",
                20,
                5
        ));
        criteria.add(new CriterionDTO(
                14L,
                "Characters",
                "(not done yet)",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                15L,
                "World realism",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                16L,
                "Visual style",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                17L,
                "Ideologic",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                18L,
                "Emotional impact",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                19L,
                "Own experience",
                "(not done yet)",
                25,
                5
        ));
        register(ArtTypeEnum.ANIME, criteria);
    }

    private void putManga(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                20L,
                "Plot",
                "not done yet",
                20,
                5
        ));
        criteria.add(new CriterionDTO(
                21L,
                "Characters",
                "(not done yet)",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                22L,
                "World realism",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                23L,
                "Visual style",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                24L,
                "Ideologic",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                25L,
                "Emotional impact",
                "(not done yet)",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                26L,
                "Own experience",
                "(not done yet)",
                25,
                5
        ));
        register(ArtTypeEnum.MANGA, criteria);
    }

    private void putBooks(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                27L,
                "",
                "(not done yet)",
                0,
                5
        ));
    }

    private void register(ArtTypeEnum type, List<CriterionDTO> criteria) {
        criteriaByType.put(type, criteria);
        for (CriterionDTO criterion : criteria) {
            if (criterion.getId() == null) {
                throw new IllegalArgumentException("Criterion id is missing for " + type);
            }
            CriterionDTO previous = criteriaById.putIfAbsent(criterion.getId(), criterion);
            if (previous != null) {
                throw new IllegalStateException("Duplicate OpusCore criterion id: " + criterion.getId());
            }
        }
    }

    public Map<ArtTypeEnum, List<CriterionDTO>> getCriteriaByType() {
        return criteriaByType;
    }

    public CriterionDTO getById(Long id) {
        CriterionDTO criterion = criteriaById.get(id);
        if (criterion != null) {
            return criterion;
        }
        throw new IllegalArgumentException("Unknown criterion id: " + id);
    }

    public int getWeightById(Long id) {
        return getById(id).getWeight();
    }
}
