package messages.results;

import lombok.Getter;

import java.io.Serializable;

public record PathEnd(@Getter String name, @Getter double finalCost) implements Serializable {
}
