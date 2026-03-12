Что тебе надо доделать на бэкенде:

- Добавить в MessageRepository нормальный метод подсчёта unread по разговору: считать чужие сообщения deletedAt is null, созданные позже lastReadAt; если lastReadAt == null,     
  считать все чужие сообщения.
- В MessengerController.buildConversationView(...) подставлять реальный unread, а не 0.
- В ChatWebSocketController.buildConversationPayload(...) отправлять не 1, а актуальный unread для конкретного получателя.
- Добавить глобальный @ModelAttribute("unreadMessages"), как у уведомлений, чтобы правый сайдбар сразу на любой странице рисовался из сервера, а не только из фронтового кеша.    
   