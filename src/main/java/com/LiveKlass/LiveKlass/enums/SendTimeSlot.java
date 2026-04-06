package com.LiveKlass.LiveKlass.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SendTimeSlot {
    IMMEDIATE("즉시"),
    MORNING("10:00"),
    AFTERNOON("16:00"),
    EVENING("20:00");

    private final String time;
}