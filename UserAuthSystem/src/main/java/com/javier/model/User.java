package com.javier.model;

/**
 * Immutable data model for a row in the users table.
 */
public record User(int id, String username, String passwordHash, Role role, String status) {
}
