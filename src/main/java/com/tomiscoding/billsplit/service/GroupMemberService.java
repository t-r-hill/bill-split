package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberService {

    @Autowired
    GroupMemberRepository groupMemberRepository;

    public GroupMember createGroupMember(GroupMember groupMember) throws ValidationException {
        validateGroupMember(groupMember);
        return groupMemberRepository.save(groupMember);
    }

    private void validateGroupMember(GroupMember groupMember) throws ValidationException {
        if (groupMember.getSplitGroup() == null){
            throw new ValidationException("A group must be selected");
        } else if (groupMember.getUser() == null) {
            throw new ValidationException("A user currency must be selected");
        }
    }
}
