package org.example.isc.main.secured.models.users;

import io.swagger.v3.core.util.Json;
import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "settings_json", nullable = false)
    private String settingsJson;

    // Сделал dropdown плотнее, он больше не такой прозрачный. Правка в src/main/resources/templates/private/blocks/private-header.html:22. Сборка проходит.
    //
    //  Теперь конкретно, что тебе делать по бэкенду для настроек.
    //
    //  1. Сделать отдельную сущность настроек пользователя.
    //     Не пихай это в User колонками.
    //     Сделай таблицу user_settings.
    //  2. Поля таблицы:
    //
    //  - id
    //  - user_id unique, fk на users.id
    //  - settings_json json/jsonb not null
    //  - created_at
    //  - updated_at
    //
    //  3. В settings_json храни сразу весь объект настроек.
    //     Структура:
    //
    //  {
    //    "scholarHub": {
    //      "preferredGradeSystem": "Numeric_Grading_1_to_5"
    //    },
    //    "appearance": {
    //      "theme": "system",
    //      "reduceMotion": false,
    //      "density": "comfortable"
    //    },
    //    "notifications": {
    //      "desktop": true,
    //      "sound": true
    //    }
    //  }
    //
    //  4. Не делай много таблиц под каждую настройку.
    //     Пока у тебя настроек мало, одна JSON-колонка лучше:
    //
    //  - проще менять схему
    //  - проще добавлять новые поля
    //  - проще отдавать фронту
    //
    //  5. Сделать один DTO для чтения и записи настроек.
    //     Например:
    //
    //  - UserSettingsDTO
    //    Внутри секции:
    //  - ScholarHubSettingsDTO
    //  - AppearanceSettingsDTO
    //  - NotificationSettingsDTO
    //
    //  6. Для preferredGradeSystem используй enum/string с жёстким списком значений:
    //
    //  - Numeric_Grading_1_to_5
    //  - Numeric_Grading_1_to_12
    //  - Numeric_Grading_1_to_10
    //  - Numeric_Grading_1_to_20
    //  - Numeric_Grading_5_to_1
    //  - Numeric_Grading_6_to_1
    //  - Percentage_Grading
    //  - Letter_Grading
    //  - GPA_4_Point_Scale
    //  - Pass_Fail
    //
    //  7. Сделать repository для user_settings.
    //     Нужен метод:
    //
    //  - findByUserId(...)
    //
    //  8. Сделать service.
    //     Логика service:
    //
    //  - getSettingsForUser(userId)
    //  - если записи нет, вернуть дефолтный объект
    //  - saveSettingsForUser(userId, dto)
    //  - если записи нет, создать
    //  - если есть, обновить settings_json
    //
    //  9. Сделать API:
    //
    //  - GET /api/settings/me
    //  - PUT /api/settings/me
    //
    //  10. Что делает GET /api/settings/me:
    //
    //  - берёт текущего пользователя из auth
    //  - ищет user_settings
    //  - если нет записи, возвращает дефолтные настройки
    //  - фронт не должен получать null
    //
    //  11. Что делает PUT /api/settings/me:
    //
    //  - берёт текущего пользователя
    //  - валидирует payload
    //  - сохраняет весь объект настроек целиком
    //  - возвращает уже сохранённый объект
    //
    //  12. Дефолтные значения задай на бэке явно.
    //     Например:
    //
    //  {
    //    "scholarHub": {
    //      "preferredGradeSystem": "Numeric_Grading_1_to_5"
    //    },
    //    "appearance": {
    //      "theme": "system",
    //      "reduceMotion": false,
    //      "density": "comfortable"
    //    },
    //    "notifications": {
    //      "desktop": true,
    //      "sound": true
    //    }
    //  }
    //
    //  13. Логику фронта потом делай так:
    //
    //  - при открытии settings modal: GET /api/settings/me
    //  - сохраняешь ответ в settingsSaved
    //  - копируешь в settingsDraft
    //  - пользователь редактирует settingsDraft
    //  - жмёт save
    //  - отправляешь PUT /api/settings/me
    //  - ответом обновляешь settingsSaved и settingsDraft
    //
    //  14. localStorage оставляй только как временный fallback.
    //     Правильно:
    //
    //  - основной источник истины: база
    //  - localStorage: кэш на случай, пока настройки не приехали
    //  - после GET /api/settings/me фронт должен синхронизироваться с сервером
    //
    //  15. Порядок внедрения у тебя должен быть такой:
    //  16. Таблица user_settings
    //  17. Entity + repository
    //  18. DTO с секциями
    //  19. Service с default settings
    //  20. GET /api/settings/me
    //  21. PUT /api/settings/me
    //  22. Подключение фронта к этим endpoint’ам
    //  23. Удаление зависимости от чистого localStorage
    //  24. Для Scholar Hub прямо сейчас тебе реально нужен только один backend-пункт:
    //
    //  - scholarHub.preferredGradeSystem
    //
    //  17. Остальные настройки можешь добавлять потом без новой схемы БД.
    //     Просто расширяешь settings_json.
    //
    //  Если хочешь, следующим сообщением я сделаю следующий фронтенд-шаг сам:
    //
    //  - внутри пустой Settings-модалки соберу секции слева
    //  - справа добавлю рабочий раздел Scholar Hub
    //  - с сохранением пока во фронтовый state/localStorage.
    //
    //
    //› Explain this codebase
    //
    //  gpt-5.4 high

}
