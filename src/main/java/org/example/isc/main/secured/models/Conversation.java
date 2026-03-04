package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.conversation.ConversationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ConversationType type;

    private String title;
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Conversation(ConversationType type, String title, String avatarUrl, User createdBy, LocalDateTime createdAt) {
        this.type = type;
        this.title = title;
        this.avatarUrl = avatarUrl;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Conversation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
/// Посмотрел проект.
///
///   Сейчас:
///
///   - Есть Chat как сущность 1‑на‑1: src/main/java/org/example/isc/main/secured/models/Chat.java.
///   - Нет сущности Message, нет репозитория/сервиса для чатов и сообщений.
///   - MessengerController отдает статические заглушки: src/main/java/org/example/isc/main/secured/messenger/MessengerController.java.
///   - schema.sql не содержит ничего для чатов/сообщений: src/main/resources/schema.sql.
///
///   Ответ на вопрос про Entity‑таблицу “для WebSocket”
///   Отдельная таблица “под WebSocket” не нужна. WebSocket — транспорт. Таблицы нужны для доменной модели чата и истории.
///
///   С учётом ваших требований (1‑на‑1, группы, каналы, история, поиск/архив), я бы заменил текущий Chat на более общую модель:
///
///   Минимальный набор таблиц/сущностей
///
///   - conversations (общая “комната/канал/диалог”)
///       - id, type (DIRECT, GROUP, CHANNEL), title, created_at, created_by
///   - conversation_members
///       - conversation_id, user_id, role (OWNER, ADMIN, MEMBER), joined_at, muted_until
///       - уникальный ключ (conversation_id, user_id)
///   - messages
///       - id, conversation_id, sender_id, body, type, created_at, edited_at, deleted_at, reply_to_id
///   - message_status
///       - message_id, user_id, delivered_at, read_at
///   - (опционально) message_attachments
///
///   Что с текущим Chat
///
///   - Ваша текущая схема Chat(me, target) подходит только для 1‑на‑1 и ломается при группах/каналах.
///   - Я бы заменил Chat на Conversation и ConversationMember.
///
///   Что нужно проработать перед WebSocket и конфигурацией
///
///   1. Доменная модель и связи (см. выше). Без этого вы не сможете хранить историю и делать поиск.
///   2. API для истории (REST):
///       - загрузка списка диалогов
///       - пагинация сообщений
///       - поиск по сообщениям
///   3. Правила доступа
///       - проверка членства в conversation_members
///       - роли для каналов (read‑only, write, admin)
///   4. Гарантии и порядок
///       - сервер генерирует created_at и id
///       - порядок сообщений по created_at + id
///   5. Поиск и архив
///       - Postgres Full‑Text: tsvector + GIN индекс по messages.body
///   6. Статусы доставки
///       - message_status для delivered/read
///   7. Масштабирование
///       - сейчас можно без брокера, но когда появится Redis — pub/sub для fan‑out.
///   8. Безопасность WebSocket
///       - аутентификация по session cookie (Spring Security) или токеном
///       - проверка прав на подписку и отправку
///   9. Формат сообщений
///       - DTO с версией (v1), чтобы не ломать клиентов
///
///   Если хотите, я могу:
///
///   1. Сформировать JPA‑модели + репозитории.
///   2. Поднять REST для истории и поиска.
///   3. Добавить WebSocket (STOMP или “сырые” WS).
///
///   Скажите, какой вариант хотите:
///
///   1. STOMP (проще для Spring + будущий Redis).
///   2. Raw WebSocket (меньше абстракций, но больше ручной логики).
///
///
/// › Improve documentation in @filename
///
///   gpt-5.2-codex medium · 97% left · ~\IdeaProjects\ISC
