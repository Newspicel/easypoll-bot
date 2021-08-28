package de.fbrettnich.easypoll.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;

public class Permissions {

    /**
     * Check if the member has the PollCreator role
     *
     * @return true if the member has the PollCreator role, otherwise false
     */
    public static boolean hasPollCreatorRole(Member member) {
        String[] groups = {"PollCreator"};

        for(Role role : member.getRoles()) {
            if(Arrays.stream(groups).parallel().allMatch(role.getName()::contains)) {
                return true;
            }
        }
        return false;
    }
}
