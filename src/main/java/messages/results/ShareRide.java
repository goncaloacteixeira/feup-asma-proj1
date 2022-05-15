package messages.results;

import lombok.Getter;

import java.io.Serializable;

public record ShareRide(@Getter String name, @Getter String path, @Getter boolean initiator) implements Serializable {
}
