package messages;

import graph.vertex.Point;
import lombok.Getter;

import java.io.Serializable;

public record OnArrivalMessage(@Getter Point place) implements Serializable {}
