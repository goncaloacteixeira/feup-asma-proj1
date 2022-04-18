package messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

public record CarRideProposeMessage(@Getter float price, @Getter float distance, @Getter int capacity) implements Serializable {}
