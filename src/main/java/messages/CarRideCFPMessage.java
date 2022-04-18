package messages;

import graph.vertex.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

public record CarRideCFPMessage(@Getter Point start, @Getter Point end) implements Serializable {}
