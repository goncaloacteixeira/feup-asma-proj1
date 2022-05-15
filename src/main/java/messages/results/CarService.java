package messages.results;

import lombok.Getter;

import java.io.Serializable;

public record CarService(@Getter String name,
                         @Getter String path,
                         @Getter double fare,
                         @Getter double expectedCost) implements Serializable {
}
