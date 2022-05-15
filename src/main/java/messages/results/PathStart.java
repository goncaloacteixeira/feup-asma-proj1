package messages.results;

import lombok.Getter;

import java.io.Serializable;

public record PathStart (@Getter String name, @Getter String path, @Getter double cost) implements Serializable {
}
