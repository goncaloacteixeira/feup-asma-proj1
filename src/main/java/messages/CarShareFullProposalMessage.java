package messages;

import lombok.Getter;

import java.io.Serializable;

public record CarShareFullProposalMessage(@Getter String srcPoint, @Getter String dstPoint,
                                          @Getter double percentage) implements Serializable {
    @Override
    public String toString() {
        return String.format("ROAD PATH FROM [%s] TO [%s]. Percentage [%.2f]", srcPoint, dstPoint, percentage);
    }
}
