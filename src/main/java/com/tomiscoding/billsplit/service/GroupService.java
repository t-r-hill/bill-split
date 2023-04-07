package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class GroupService {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberService groupMemberService;

    @Transactional
    public SplitGroup createGroup(SplitGroup splitGroup, User user) throws ValidationException{

        validateGroupProperties(splitGroup);

        while (true){
            String code = generateCode();
            if (groupRepository.findByInviteCode(code).isEmpty()){
                splitGroup.setInviteCode(code);
                break;
            }
        }

        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        splitGroup.setGroupMembers(Collections.singletonList(groupMember));

        return groupRepository.save(splitGroup);
    }

    public SplitGroup getGroupById(Long id) throws SplitGroupNotFoundException {
        return groupRepository.findById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );
    }

    public List<SplitGroup> getGroupsByUser(User user){
        return groupRepository.getByGroupMembers_User(user);
    }

    // Helper function to generate a random code to externally identify the group
    private String generateCode(){
        String chars = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ1234567890";
        int ub = chars.length() - 1;
        Random random = new Random();
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++){
            builder.append(chars.charAt(random.nextInt(ub)));
        }
        return builder.toString();
    }

    private void validateGroupProperties(SplitGroup splitGroup) throws ValidationException {
        if (splitGroup.getGroupName() == null || splitGroup.getGroupName().isBlank()){
            throw new ValidationException("Name must not be blank");
        } else if (splitGroup.getBaseCurrency() == null) {
            throw new ValidationException("A base currency must be selected");
        }
    }
}
