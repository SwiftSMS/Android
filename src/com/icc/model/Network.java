package com.icc.model;

/**
 * Simple enum to hold the current Network operators ICC will support.
 * 
 * @author Rob Powell
 * @version 1.1
 */
public enum Network {
	O2("O2"), METEOR("Meteor"), VODAFONE("Vodafone"), TESCO("Tesco"), EMOBILE("EMobile"), THREE("Three");

    private final String network;

    private Network(String network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return this.network;
    }
}