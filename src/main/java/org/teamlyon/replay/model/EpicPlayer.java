package org.teamlyon.replay.model;

import java.util.HashMap;
import java.util.Map;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.domain.Account;

public class EpicPlayer extends Player {

    public static final Map<String, String> RESOLVE_CACHE = new HashMap<>();

    public String displayName;

    public static int indexAmnt = 1;

    public EpicPlayer(String accountId,
                      Fortnite epicApi) {
        super(accountId);
        try {
            if (RESOLVE_CACHE.containsKey(accountId)) {
                displayName = RESOLVE_CACHE.get(accountId);
            } else {
                for (Account account : epicApi.account().findAllByAccountIds(accountId).get()) {
                    displayName = account.displayName();
                    RESOLVE_CACHE.put(accountId, displayName);
                }
            }
            System.out.println("Discovered name of \"" + accountId + "\"! [" +indexAmnt +"]");
            indexAmnt++;
        } catch (Exception e) {
            System.out.println("Failed to find display name of \"" + accountId + "\"! (" + e.getClass().getSimpleName() + ")");
            e.printStackTrace();
            displayName = "???";
        }
    }
}
