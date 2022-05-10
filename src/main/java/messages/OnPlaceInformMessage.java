package messages;

import graph.vertex.Point;
import lombok.Getter;

import java.io.Serializable;

public record OnPlaceInformMessage(@Getter Point place) implements Serializable {}
