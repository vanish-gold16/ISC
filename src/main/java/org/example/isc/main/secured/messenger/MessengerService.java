package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.messenger.ConversationDTO;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.enums.conversation.ConversationRole;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.enums.conversation.MessageType;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.example.isc.main.secured.models.messenger.Message;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.profile.service.ActivityService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationMemberRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.example.isc.main.secured.repositories.conversation.MessageRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessengerService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;
    private final ActivityService activityService;

    public MessengerService(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, UserRepository userRepository, NotificationService notificationService, MessageRepository messageRepository, ActivityService activityService) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messageRepository = messageRepository;
        this.activityService = activityService;
    }

    public List<ConversationDTO> getConversations(User me){
        List<Conversation> conversations = conversationRepository.findByMember(me);
        return conversations.stream().map(this::toDTO).toList();
    }

    public ConversationDTO getOrCreateDirect(User me, User target){
        List<Conversation> allConversations = conversationRepository.findByMember(me);
        Conversation currentConversation;

        for (Conversation c : allConversations) {
                if(c.getType() ==  ConversationType.DIRECT
                && conversationMemberRepository.existsByConversationAndUser(c,target)) {
                    return toDTO(c);
                }
            }

        currentConversation = new Conversation(
                ConversationType.DIRECT,
                null,
                null,
                me,
                LocalDateTime.now()
        );

        conversationRepository.save(currentConversation);
        conversationMemberRepository.save(new ConversationMember(
                currentConversation, me, ConversationRole.MEMBER, LocalDateTime.now(), null
        ));
        conversationMemberRepository.save(new  ConversationMember(
                currentConversation, target, ConversationRole.MEMBER, LocalDateTime.now(), null
        ));

        ConversationMember memberMe = conversationMemberRepository.findByConversationAndUser(currentConversation, me)
                .orElseThrow(() -> new IllegalStateException("Conversation member not found: " + me.getUsername()));
        ConversationMember memberTarget = conversationMemberRepository.findByConversationAndUser(currentConversation, target)
                .orElseThrow(() -> new IllegalStateException("Conversation member not found: " + target.getUsername()));

        memberMe.setLastReadAt(LocalDateTime.now());
        memberTarget.setLastReadAt(LocalDateTime.now());

        conversationMemberRepository.save(memberMe);
        conversationMemberRepository.save(memberTarget);

        return toDTO(currentConversation);
    }

    public ConversationDTO getOrCreateGroup(User me, Conversation conversation){
        if(!conversationRepository.existsByTypeAndId(ConversationType.GROUP, conversation.getId())) {
            if (!conversationMemberRepository.existsByConversationAndUser(conversation, me)) {
                conversationRepository.save(conversation);
                conversationMemberRepository.save(new ConversationMember(
                        conversation, me, ConversationRole.MEMBER, LocalDateTime.now(), null
                ));
            }
        }

        return toDTO(conversation);
    }

    public void rename(
            Conversation conversation,
            @RequestParam String newName,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ConversationMember member = conversationMemberRepository.findByConversationAndUser(conversation, me)
                .orElseThrow(() -> new IllegalStateException("User is not in this conversation: " + conversation.getTitle()));
        String oldName = conversation.getTitle();

        if(conversation.getType().equals(ConversationType.GROUP)){
            conversation.setTitle(newName);
            conversationRepository.save(conversation);
            for(ConversationMember m : conversationMemberRepository.findByConversation(conversation)) {
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        m.getUser(),
                        me,
                        oldName,
                        " has renamed the conversation",
                        conversationIdData(conversation)
                        );
            }
        }
        else if(
                conversation.getType().equals(ConversationType.CHANNEL)
                && (member.getConversationRole().equals(ConversationRole.OWNER)
                ||  member.getConversationRole().equals(ConversationRole.ADMIN))
        ){
            conversation.setTitle(newName);
            conversationRepository.save(conversation);
            for(ConversationMember m :conversationMemberRepository.findByConversation(conversation)){
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        m.getUser(),
                        me,
                        oldName,
                        " has renamed the channel",
                        conversationIdData(conversation)
                );
            }
        }
    }

    public ConversationMember addUser(
            Conversation conversation,
            @RequestParam User user,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ConversationMember member = new ConversationMember(
                conversation, user, ConversationRole.MEMBER, LocalDateTime.now(), null
        );
        conversationMemberRepository.save(member);
        for(ConversationMember m : conversationMemberRepository.findByConversation(conversation)) {
            notificationService.create(
                    NotificationEnum.MESSAGE,
                    m.getUser(),
                    me,
                    conversation.getTitle(),
                    " is the new member",
                    conversationIdData(conversation)
            );
        }
        return member;
    }

    public String deleteUser(Conversation conversation, User user, Authentication authentication){
        ConversationMember me = conversationMemberRepository.findByConversationAndUser(
                conversation,
                userRepository.findByUsernameIgnoreCase(authentication.getName()).orElseThrow(() ->
                        new IllegalStateException("Logged-in user not found: " + authentication.getName()))
        ).orElseThrow(() -> new IllegalStateException("User is not in this conversation: " + conversation.getTitle()));

        ConversationMember member = conversationMemberRepository.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getUsername()));

        String status = "User has not been deleted";
        if(me.getConversationRole().equals(ConversationRole.ADMIN)
        || me.getConversationRole().equals(ConversationRole.OWNER)){
            conversationMemberRepository.delete(member);
            for(ConversationMember m : conversationMemberRepository.findByConversation(conversation)){
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        m.getUser(),
                        me.getUser(),
                        conversation.getTitle(),
                        " has deleted " + member.getUser().getUsername(),
                        conversationIdData(conversation)
                );
            }
            status = "User has been deleted";
        }

        return status;
    }

    public void assertMember(User user, Conversation conversation){
        if(!conversationMemberRepository.existsByConversationAndUser(conversation, user))
            throw new IllegalStateException("User is not in this conversation: " + conversation.getTitle());
    }

    private ConversationDTO toDTO(Conversation conversation){
        return new ConversationDTO(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                conversation.getAvatarUrl()
        );
    }

    public Message saveMessage(Conversation conversation, User sender, String text, MessageType type){
        List<User> receivers;
        User receiver;

        if(conversation.getType().equals(ConversationType.DIRECT)){
            receiver = conversationMemberRepository.findOtherUserByConversationDirect(conversation, sender);
            if(!isOnline(receiver))
            notificationService.create(
                    NotificationEnum.MESSAGE,
                    receiver,
                    sender,
                    "New message",
                    ": " + text,
                    conversationIdData(conversation)
            );
        }
        else if (conversation.getType().equals(ConversationType.CHANNEL)
        || conversation.getType().equals(ConversationType.GROUP)){
            receivers = conversationMemberRepository.findOtherUsersByConversation(conversation, sender);
            for (User user : receivers) {
                if(!isOnline(user))
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        user,
                        sender,
                        "New message",
                        ": " + text,
                        conversationIdData(conversation)
                );
            }
        }
        Message message = new Message(conversation, sender, text, type, LocalDateTime.now());

        messageRepository.save(message);

        return message;
    }

    public void markConversationRead(Conversation conversation, User user){
        ConversationMember member = conversationMemberRepository.findByConversationAndUser(conversation, user)
                .orElseThrow(() -> new IllegalStateException("Conversation member not found: " + user.getUsername()));

        member.setLastReadAt(LocalDateTime.now());
        conversationMemberRepository.save(member);
    }

    private String conversationIdData(Conversation conversation) {
        return conversation != null && conversation.getId() != null ? conversation.getId().toString() : null;
    }

    private boolean isOnline(User target){
        return activityService.online(target.getId());
    }

    public Long countUnread(Conversation conversation, User recipient) {
        ConversationMember member = conversationMemberRepository.findByConversationAndUser(conversation, recipient)
                .orElseThrow(() -> new IllegalStateException("User is not in this conversation: " + conversation.getTitle()));
        if(member.getLastReadAt() == null) return messageRepository.countUnreadMessagesNoReadAt(conversation, recipient.getId());
        return messageRepository.countUnreadMessagesAfter(conversation, recipient.getId(), member.getLastReadAt());
    }
}
