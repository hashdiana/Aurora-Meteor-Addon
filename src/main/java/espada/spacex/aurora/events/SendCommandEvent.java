/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package espada.spacex.aurora.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class SendCommandEvent extends Cancellable {
    private static final SendCommandEvent INSTANCE = new SendCommandEvent();

    public String message;

    public static SendCommandEvent get(String message) {
        INSTANCE.setCancelled(false);
        INSTANCE.message = message;
        return INSTANCE;
    }

}
