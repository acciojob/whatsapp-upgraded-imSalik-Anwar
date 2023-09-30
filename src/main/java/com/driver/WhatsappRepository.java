package com.driver;

import com.driver.exception.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class WhatsappRepository {
    Map<String, User> userDB = new HashMap<>();
    Map<Group, List<User>> groupParticipantDB = new HashMap<>();
    Map<Group, List<Message>> groupMessageDB = new HashMap<>();
    Map<Integer, Message> messageDB = new HashMap<>();

    Map<User, List<Message>> userMessageDB = new HashMap<>();

    public String createUser(String name, String mobile) {
        if(userDB.containsKey(mobile)){
            throw new UserAlreadyExistsException("User already exists.");
        }
        User user = new User();
        user.setMobile(mobile);
        user.setName(name);
        userDB.put(mobile, user);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        // get total count of users
        int count = users.size();
        // create a new group
        Group group = new Group();
        if(count == 2){
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(1);
        } else {
            group.setName("Group "+String.valueOf(count-1));
            group.setNumberOfParticipants(count);
        }
        // save group in groupDB
        groupParticipantDB.put(group, users);
        return group;
    }

    public int createMessage(String content) {
        int size = messageDB.size();
        int id = size + 1;
        Date date = new Date();
        Message message = new Message();
        message.setId(id);
        message.setContent(content);
        message.setTimestamp(date);
        messageDB.put(id, message);
        return id;
    }

    public int sendMessage(Message message, User sender, Group group) {
        // check if group exists
        if(!groupParticipantDB.containsKey(group)){
            throw new GroupDoesNotExistsException("Group does not exist");
        }
        // check if user exists
        if(!userDB.containsKey(sender.getMobile())){
            throw new YouAreNotAllowedToSendMessageException("You are not allowed to send message");
        }
        // add message to corresponding group
        if(!groupMessageDB.containsKey(group)){
            List<Message> newList = new ArrayList<>();
            newList.add(message);
            groupMessageDB.put(group, newList);
        } else {
            List<Message> oldList = groupMessageDB.get(group);
            oldList.add(message);
        }
        // add message to corresponding user
        if(!userMessageDB.containsKey(sender)){
            List<Message> newList = new ArrayList<>();
            newList.add(message);
           userMessageDB.put(sender, newList);
        } else {
            List<Message> oldList = userMessageDB.get(sender);
            oldList.add(message);
        }
        return groupMessageDB.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) {
        // check if group exists
        if(!groupParticipantDB.containsKey(group)){
            throw new GroupDoesNotExistsException("Group does not exist");
        }
        // check if approver is admin or not
        User admin = groupParticipantDB.get(group).get(0);
        if(approver != admin){
            throw new UserNotFoundException("Approver does not have rights");
        }
        // check if user is a participant of the group or not
        User member = null;
        int indexOfMember = -1;
        List<User> userList = groupParticipantDB.get(group);
        for(int i = 1; i < userList.size(); i++){
            if(userList.get(i) == user){
                member = userList.get(i);
                indexOfMember = i;
                break;
            }
        }
        if(member == null){
            throw new UserNotFoundException("User is not a participant");
        }
        // switch roles
        User ADMIN = userList.get(0);
        userList.set(0, member);
        userList.set(indexOfMember, ADMIN);

        return "SUCCESS";
    }

    public int removeUser(User user) {
        // check if user is present and it is not an admin
        boolean removed = false;
        Group targetGroup = null;
        for(Group group : groupParticipantDB.keySet()){
            for(int i = 0; i < groupParticipantDB.get(group).size(); i++){
                if(i == 0 && groupParticipantDB.get(group).get(i) == user){
                    throw new AdminCanNotBeRemovedException("Cannot remove admin");
                } else if(groupParticipantDB.get(group).get(i) == user){
                    groupParticipantDB.get(group).remove(i);
                    group.setNumberOfParticipants(group.getNumberOfParticipants() - 1);
                    group.setName("Group "+String.valueOf(group.getNumberOfParticipants() + 1));
                    targetGroup = group;
                    removed = true;
                    break;
                }
            }
            if(removed) break;
        }
        if(!removed){
            throw new UserNotFoundException("User not found");
        }
        // if user is removed from group, remove its messages from that group and messageDB as well
        List<Message> messageList = userMessageDB.remove(user);
        for(Message message : groupMessageDB.get(targetGroup)){
            if(messageList.contains(message)){
                groupMessageDB.get(targetGroup).remove(message);
                messageDB.remove(message);
            }
        }
        return groupParticipantDB.get(targetGroup).size() + groupMessageDB.get(targetGroup).size() + messageDB.size();
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messageList = new ArrayList<>();
        for(Message message : messageDB.values()){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                messageList.add(message);
            }
        }
        if(messageList.size() < k){
            throw new Exception("K is greater than the number of messages");
        }
        Map<Long, Message> map = new TreeMap<>();
        String response = "";
        for(Message message : messageList){
            long val = message.getTimestamp().getTime();
            map.put(val, message);
        }
        for(Message message : map.values()){
            k--;
            if(k == 0){
                response = message.getContent();
                break;
            }
        }
        return response;
    }
}
