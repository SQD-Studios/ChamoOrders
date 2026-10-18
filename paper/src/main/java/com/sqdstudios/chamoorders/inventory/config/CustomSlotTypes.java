package com.sqdstudios.chamoorders.inventory.config;

import com.sqdstudios.sqdlib.paper.chamogui.config.SlotType;

public final class CustomSlotTypes {
    public record OrderItemSlot() implements SlotType {
    }

    public record SearchSlot() implements SlotType {
    }

    public record FilterSlot() implements SlotType {
    }

    public record BackSlot() implements SlotType {
    }

    public record DeliverItems() implements SlotType {
    }

    public record OpenDeliverUi() implements SlotType {
    }

    public record OpenYourOrderGui() implements SlotType {
    }
}