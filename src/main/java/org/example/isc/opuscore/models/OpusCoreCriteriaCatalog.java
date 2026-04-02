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

    private void putMovies(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                7L,
                "Plot",
                "How well does the story come together, is there internal logic, " +
                          "is the pace good, is the ending appropriate and acceptable",
                18,
                5
        ));
        criteria.add(new CriterionDTO(
                8L,
                "Characters",
                "How well are the characters written, " +
                          "their backstories, motivations for certain actions and interactions between them?",
                13,
                5
        ));
        criteria.add(new CriterionDTO(
                9L,
                "Directing / Audiovisuals",
                "Composition, light, color, shots, scenes, editing, camera work, working with tension, accents, musical accompaniment.",
                18,
                5
        ));
        criteria.add(new CriterionDTO(
                10L,
                "World realism",
                "How well the lore of the universe is explained to us, " +
                          "how logical the events that take place or the fundamental \"laws\" are",
                12,
                5
        ));
        criteria.add(new CriterionDTO(
                11L,
                "Ideologic",
                "Is there something beneath the surface. An idea, a thought, a moral that the author wants to convey?",
                8,
                5
        ));
        criteria.add(new CriterionDTO(
                12L,
                "Emotional impact",
                "Does the film hit you? " +
                          "It doesn't matter how: with pain, delight, horror, warmth, emptiness.",
                8,
                5
        ));
        criteria.add(new CriterionDTO(
                13L,
                "Own experience",
                "Your subjective perception and feelings about. " +
                          "Does it leave a lasting impression, does it have soul, does it have magic?",
                23,
                5
        ));
        register(ArtTypeEnum.MOVIE, criteria);
    }

    private void putShows(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                14L,
                "Plot",
                "How well does the story come together, is there internal logic, " +
                          "is the pace good, is the ending appropriate and acceptable",
                16,
                5
        ));
        criteria.add(new CriterionDTO(
                15L,
                "Characters",
                "How well are the characters written, " +
                          "their backstories, motivations for certain actions and interactions between them.",
                13,
                5
        ));
        criteria.add(new CriterionDTO(
                16L,
                "Directing / Audiovisuals",
                "Composition, light, color, shots, scenes, editing, camera work, working with tension, accents, musical accompaniment.",
                16,
                5
        ));
        criteria.add(new CriterionDTO(
                17L,
                "Duration",
                "The length of the entire series is logical and the number of seasons is not artificially stretched out.",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                18L,
                "World realism",
                "How well the lore of the universe is explained to us, " +
                          "how logical the events that take place or the fundamental \"laws\" are",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                19L,
                "Ideologic",
                "Is there something beneath the surface. An idea, a thought, a moral that the author wants to convey?",
                8,
                5
        ));
        criteria.add(new CriterionDTO(
                20L,
                "Emotional impact",
                "Does the series hit you? " +
                          "It doesn't matter how: with pain, delight, horror, warmth, emptiness.",
                7,
                5
        ));
        criteria.add(new CriterionDTO(
                21L,
                "Own experience",
                "Your subjective perception and feelings about. " +
                          "Does it leave a lasting impression, does it have soul, does it have magic?",
                20,
                5
        ));
        register(ArtTypeEnum.SHOW, criteria);
    }

    private void putGames(){
        List<CriterionDTO> criteria = new ArrayList<>();

        criteria.add(new CriterionDTO(
                22L,
                "Narrative and Directing",
                "Composition, lighting, color, camera angles, scenes, editing, camera work, tension, accents. " +
                          "Constructing dialogue between the player and NPCs.",
                19,
                5
        ));
        criteria.add(new CriterionDTO(
                23L,
                "Gameplay",
                "How interesting and varied is every minute of the game." +
                          "How interesting are the mechanics, quests, and side quests (not just fetch and serve).",
                19,
                5
        ));
        criteria.add(new CriterionDTO(
                24L,
                "Characters",
                "How well are the characters written, " +
                          "their backstories, motivations for certain actions and interactions between them.",
                14,
                5
        ));
        criteria.add(new CriterionDTO(
                25L,
                "Audiovisuals",
                "The quality of graphics (not necessarily photorealism, the \"beauty\" itself is important), " +
                          "as well as the musical accompaniment.",
                14,
                5
        ));
        criteria.add(new CriterionDTO(
                26L,
                "Technical",
                "Optimization, crashes, bugs, lags, and other technical issues.",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                27L,
                "Own experience",
                "Your subjective perception and feelings about.",
                24,
                5
        ));
        register(ArtTypeEnum.GAME, criteria);
    }

    private void putAnime(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                28L,
                "Plot",
                "How well does the story come together, is there internal logic, " +
                          "is the pace good, is the ending appropriate and acceptable",
                20,
                5
        ));
        criteria.add(new CriterionDTO(
                29L,
                "Characters",
                "How well are the characters written, " +
                          "their backstories, motivations for certain actions and interactions between them.",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                30L,
                "World realism",
                "How well the lore of the universe is explained to us, " +
                          "how logical the events that take place or the fundamental \"laws\" are",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                31L,
                "Visual/Audio style",
                "The quality of the drawing, animation and musical accompaniment",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                32L,
                "Ideologic",
                "Is there something beneath the surface. An idea, a thought, a moral that the author wants to convey?",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                33L,
                "Emotional impact",
                "Does it hit you? " +
                        "It doesn't matter how: with pain, delight, horror, warmth, emptiness.",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                34L,
                "Own experience",
                "Your subjective perception and feelings about. " +
                        "Does it leave a lasting impression, does it have soul, does it have magic?",
                25,
                5
        ));
        register(ArtTypeEnum.ANIME, criteria);
    }

    private void putManga(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                35L,
                "Plot",
                "How well does the story come together, is there internal logic, " +
                        "is the pace good, is the ending appropriate and acceptable",
                20,
                5
        ));
        criteria.add(new CriterionDTO(
                36L,
                "Characters",
                "How well are the characters written, " +
                        "their backstories, motivations for certain actions and interactions between them.",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                37L,
                "World realism",
                "How well the lore of the universe is explained to us, " +
                        "how logical the events that take place or the fundamental \"laws\" are",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                38L,
                "Visual style",
                "The quality of the drawing, animation and musical accompaniment",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                39L,
                "Ideologic",
                "Is there something beneath the surface. An idea, a thought, a moral that the author wants to convey?",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                40L,
                "Emotional impact",
                "Does it hit you? " +
                        "It doesn't matter how: with pain, delight, horror, warmth, emptiness.",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                41L,
                "Own experience",
                "Your subjective perception and feelings about. " +
                        "Does it leave a lasting impression, does it have soul, does it have magic?",
                25,
                5
        ));
        register(ArtTypeEnum.MANGA, criteria);
    }

    private void putBooks(){
        List<CriterionDTO> criteria = new ArrayList<>();
        criteria.add(new CriterionDTO(
                42L,
                "Plot",
                "How well does the story come together, is there internal logic, " +
                          "is the pace good, is the ending appropriate and acceptable",
                18,
                5
        ));
        criteria.add(new CriterionDTO(
                43L,
                "Characters",
                "How well are the characters written, " +
                        "their backstories, motivations for certain actions and interactions between them.",
                15,
                5
        ));
        criteria.add(new CriterionDTO(
                44L,
                "Writing style / Language",
                "Language, prose quality, readability, uniqueness of author voice",
                15,
                5
                ));
        criteria.add(new CriterionDTO(
                45L,
                "Structure / Pacing",
                "Flow of the narrative, balance, absence of dragging or rushing",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                46L,
                "World realism",
                "How well the lore of the universe is explained to us, " +
                        "how logical the events that take place or the fundamental \"laws\" are",
                10,
                5
        ));

        criteria.add(new CriterionDTO(
                47L,
                "Ideologic",
                "Is there something beneath the surface. An idea, a thought, a moral that the author wants to convey?",
                10,
                5
        ));
        criteria.add(new CriterionDTO(
                48L,
                "Emotional impact",
                "Does it hit you? " +
                        "It doesn't matter how: with pain, delight, horror, warmth, emptiness.",
                7,
                5
        ));
        criteria.add(new CriterionDTO(
                49L,
                "Own experience",
                "Your subjective perception and feelings about. " +
                          "Does it leave a lasting impression, does it have soul, does it have magic?",
                15,
                5
        ));
        register(ArtTypeEnum.BOOK, criteria);
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
