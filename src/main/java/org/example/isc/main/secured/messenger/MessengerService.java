package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.ConversationDTO;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.enums.conversation.ConversationRole;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationMemberRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessengerService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final NotificationsRepository notificationsRepository;
    private final NotificationService notificationService;

    public MessengerService(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, ConversationMemberRepository conversationMemberRepository1, UserRepository userRepository, NotificationsRepository notificationsRepository, NotificationService notificationService) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository1;
        this.userRepository = userRepository;
        this.notificationsRepository = notificationsRepository;
        this.notificationService = notificationService;
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

    public void rename(Conversation conversation, String newName, Authentication authentication){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ConversationMember member = conversationMemberRepository.findByConversationAndUser(conversation, me)
                .orElseThrow(() -> new IllegalStateException("User is not in this conversation: " + conversation.getTitle()));
        String oldName = conversation.getTitle();

        if(conversation.getType().equals(ConversationType.GROUP)){
            conversation.setTitle(newName);
            conversationRepository.save(conversation);
            for(ConversationMember m : conversationMemberRepository.countByConversation(conversation)) {
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        m.getUser(),
                        me,
                        oldName,
                        me.getUsername() + " has renamed the conversation",
                        null
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
            for(ConversationMember m :conversationMemberRepository.countByConversation(conversation)){
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        m.getUser(),
                        me,
                        oldName,
                        me.getUsername() + " has renamed the channel",
                        null
                );
            }
        }
    }

    public ConversationMember addUser(Conversation conversation, User user, Authentication authentication){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ConversationMember member = new ConversationMember(
                conversation, user, ConversationRole.MEMBER, LocalDateTime.now(), null
        );
        conversationMemberRepository.save(member);
        for(ConversationMember m : conversationMemberRepository.countByConversation(conversation)) {
            notificationService.create(
                    NotificationEnum.MESSAGE,
                    m.getUser(),
                    me,
                    conversation.getTitle(),
                    member.getUser().getUsername() + " is the new member",
                    null
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

        ConversationMember member = new ConversationMember(
                conversation, user, ConversationRole.MEMBER, LocalDateTime.now(), null
        );

        String status = "User has not been deleted";
        if(me.getConversationRole().equals(ConversationRole.ADMIN)
        || me.getConversationRole().equals(ConversationRole.OWNER)){
            conversationMemberRepository.delete(member);
            for(ConversationMember m : conversationMemberRepository.countByConversation(conversation)){
                notificationService.create(
                        NotificationEnum.MESSAGE,
                        m.getUser(),
                        me.getUser(),
                        conversation.getTitle(),
                        me.getUser().getUsername() + " has deleted " + member.getUser().getUsername(),
                        null
                );
            }
            status = "User has been deleted";
        }

        return status;
    }

    private ConversationDTO toDTO(Conversation conversation){
        return new ConversationDTO(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                conversation.getAvatarUrl()
        );
    }

}
