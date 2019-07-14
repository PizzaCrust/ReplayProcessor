package org.teamlyon.replay.model;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.domain.Account;

public class EpicPlayer extends Player {

    public String displayName;

    public static int indexAmnt = 1;

    public EpicPlayer(String accountId,
                      Fortnite epicApi) {
        super(accountId);
        try {
            for (Account account : epicApi.account().findAllByAccountIds(accountId).get()) {
                displayName = account.displayName();
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
